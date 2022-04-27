<%
    ui.decorateWith("kenyaemr", "standardPage", [ layout: "sidebar" ])
    def menuItems = [
            [label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to IIT home", href: ui.pageLink("kenyaemrml", "mlRiskScoreHome")]
    ]

    ui.includeJavascript("kenyaemrml", "bootstrap/bootstrap.bundle.min.js")
    ui.includeCss("kenyaemrml", "bootstrap/bootstrap-iso.css")
%>
<style>
.simple-table {
    border: solid 1px #DDEEEE;
    border-collapse: collapse;
    border-spacing: 0;
    font: normal 13px Arial, sans-serif;
}
.simple-table thead th {

    border: solid 1px #DDEEEE;
    color: #336B6B;
    padding: 10px;
    text-align: left;
    text-shadow: 1px 1px 1px #fff;
}
.simple-table td {
    border: solid 1px #DDEEEE;
    color: #333;
    padding: 5px;
    text-shadow: 1px 1px 1px #fff;
}
table {
    width: 95%;
}
th, td {
    padding: 5px;
    text-align: left;
    height: 30px;
    border-bottom: 1px solid #ddd;
}
tr:nth-child(even) {background-color: #f2f2f2;}
#pager li{
    display: inline-block;
}
.mainBox {
    float: left;
}
.msg {
    width: 90%
}
.boxStyle {
    color: black;
    font-size: 1.2em;
    line-height: 1.5;
    border: 2px darkslategray solid;
    border-radius: 10px;
    padding: 10px 5px 5px 10px;
    margin: 5px 2px;
    width: 48%;
}
.alignHorizontal {
    display: flex;
    flex-direction: row;
    justify-content: flex-start; 
    align-items: flex-start;
}
.prog-bar {
    width: 500px;
}
.wait-loading {
    margin-right: 5px;
    margin-left: 5px;
    display: none;
}
#fetchRiskScores {
    margin-right: 5px;
    margin-left: 5px;
}
#updateSummary {
    margin-right: 5px;
    margin-left: 5px;
}
#stopPull {
    display: none;
}
</style>

<div class="ke-page-sidebar">

    <div class="ke-panel-frame">
        ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Navigation", items: menuItems])}
    </div>
</div>

<div class="ke-page-content">

    <div>
        <fieldset>
            <legend>ML Summary</legend>
            <div>
                <table class="simple-table" width="100%">
                    <thead>
                    </thead>
                    <tbody>
                    <tr>
                        <td width="30%">Total Patients</td>
                        <td id="strTotalCount">${totalCount}</td>
                    </tr>
                    <tr>
                        <td width="30%">High Risk</td>
                        <td id="strHighRiskCount">${highRiskCount}</td>
                    </tr>
                    <tr>
                        <td width="30%">Low Risk</td>
                        <td id="strLowRiskCount">${lowRiskCount}</td>
                    </tr>
                    <tr>
                        <td width="30%">Risk Threshold </td>
                        <td id="strRiskThreshhold">${riskThreshhold}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </fieldset>
    </div>
    <div>
        <fieldset>
            <legend>Fetch IIT risk scores from Data Warehouse (NDWH)</legend>
            <br/>
            <div id="message"><span id="lblText" style="color: Red; top: 50px;"></span></div>
            <br/>
            <div class="alignHorizontal">
                <button id="updateSummary">Update Summary</button>
                <button id="fetchRiskScores">Pull Patient scores</button>
                <div class="bootstrap-iso">
                    <div class="wait-loading prog-bar">
                        <div class="progress">
                            <div class="progress-bar progress-bar-striped" role="progressbar" style="width: 25%" aria-valuenow="25" aria-valuemin="0" aria-valuemax="100">
                                <span class="prog-percentage"></span>
                            </div>
                        </div>
                        <div class="prog-status"></div>
                    </div>
                </div>
                <button id="stopPull">Stop the pull</button>
            </div>
        </fieldset>
    </div>

</div>

<script type="text/javascript">
    jq = jQuery;
    jq(function() {

        var loadingImageURL = ui.resourceLink("kenyaemrml", "images/loading.gif");
        var isPullingData = false;
        //var showLoadingImage = '<span style="padding:2px; display:inline-block;"> <img src="' + loadingImageURL + '" /> </span>';

        //show message
        function display_message(msg) {
            jq("#lblText").html(msg);
            jq('#message').fadeIn('slow').delay(3000).fadeOut('slow');
        }

        // display or hide the data pull progress indicator
        function display_loading(status) {
            if(status) {
                //jq('.wait-loading').append(showLoadingImage);
                jq('.wait-loading').show();
            } else {
                //jq('.wait-loading').empty();
                jq('.wait-loading').hide();
            }
        }

        // handle click event of the stop pull button
        jq(document).on('click','#stopPull',function () {
            console.log('Stoping the fetch task!');
            display_message('Stoping the fetch task!');
            ui.getFragmentActionAsJson('kenyaemrml', 'iitRiskScoreHistory', 'stopDataPull', {}, function (result) {
                //stop pulling data from DWH
            });
            display_loading(false);
            jq('#fetchRiskScores').attr('disabled', false);
            jq('#stopPull').hide();  
        });

        // handle click event of the update summary button
        jq(document).on('click','#updateSummary',function () {
            updateSummaryTable();
        });

        // handle click event of the fetch data button
        jq(document).on('click','#fetchRiskScores',function () {
            //Run the fetch task
            console.log('Starting the fetch task!');
            display_message('Starting the fetch task!');
            display_loading(true);
            jq('#fetchRiskScores').attr('disabled', true);
            jq('#stopPull').show();

            fetchDataAsync().done(function(){
                console.log('Finished the fetch task!');
                display_message('Finished the fetch task!');
                display_loading(false);
                jq('#fetchRiskScores').attr('disabled', false);
                jq('#stopPull').hide();
                updateSummaryTable();
                isPullingData = false;
            });

            fetchStatus();
        });

        // fetch the data from DWH asynchronously
        function fetchDataAsync() {
            let dfrd = jq.Deferred();
            isPullingData = true;
            //The Task
            ui.getFragmentActionAsJson('kenyaemrml', 'iitRiskScoreHistory', 'fetchDataFromDWH', {}, function (result) {
                if(result) {
                    console.log('Success fetching data!');
                } else {
                    console.log('Failed to fetch data!');
                }
                dfrd.resolve();
            });

            return jq.when(dfrd).done(function(){
                console.log('Finished the fetch task!');
            }).promise();
        }

        // Check the status of the data pull
        function fetchStatus() {
            console.log('Checking fetch status!');
            getFetchStatusAsync().done(function(){
                if(isPullingData) {
                    setTimeout(function() {
                        fetchStatus();
                    },3000);
                }
            });
        }

        // fetch the status of data pull asynchronously
        function getFetchStatusAsync() {
            let dfrd = jq.Deferred();
            //The Task
            ui.getFragmentActionAsJson('kenyaemrml', 'iitRiskScoreHistory', 'getStatusOfDataPull', {}, function (result) {
                if(result) {
                    //console.log('Success fetching pull status!');
                    let statusDone = result.done;
                    let statusTotal = result.total;
                    let statusPercent = result.percent;
                    //console.log('Got done as: ' + statusDone);
                    //console.log('Got total as: ' + statusTotal);
                    //console.log('Got percent as: ' + statusPercent);
                    //jq(".progress-value").html(statusPercent + "%");
                    jq(".progress-bar").attr('aria-valuenow', statusPercent).css('width', statusPercent+'%');
                    jq(".prog-status").html(statusDone + "/" + statusTotal);
                    jq(".prog-percentage").html(statusPercent+'%');
                } else {
                    console.log('Failed to fetch pull status!');
                }
                dfrd.resolve();
            });

            return jq.when(dfrd).done(function(){
                console.log('Finished the fetch status check!');
            }).promise();
        }

        // update the summary table
        function updateSummaryTable() {
            ui.getFragmentActionAsJson('kenyaemrml', 'iitRiskScoreHistory', 'fetchLocalSummary', {}, function (result) {
                if(result) {
                    display_message('Success fetching summary!');
                    console.log('Success fetching summary!');
                    jq("#strTotalCount").html(result.totalCount);
                    jq("#strHighRiskCount").html(result.highRiskCount);
                    jq("#strLowRiskCount").html(result.lowRiskCount);
                    jq("#strRiskThreshhold").html(result.riskThreshhold);
                } else {
                    display_message('Failed to fetch summary!');
                    console.log('Failed to fetch summary!');
                }
            });
        }

    });
</script>