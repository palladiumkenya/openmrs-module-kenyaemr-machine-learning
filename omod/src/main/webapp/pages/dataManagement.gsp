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
.genscores-wait-loading {
    margin-right: 5px;
    margin-left: 5px;
    display: none;
}
#fetchRiskScores {
    margin-right: 5px;
    margin-left: 5px;
}
#generateRiskScores {
    margin-right: 5px;
    margin-left: 5px;
}
#updateSummary {
    margin-right: 5px;
    margin-left: 5px;
}
#updateLocalSummary {
    margin-right: 5px;
    margin-left: 5px;
}
#stopPull {
    display: none;
}
#message { 
    min-height: 20px;
}
.progress-container {
    min-height: 30px;
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
                        <td width="30%">Medium Risk</td>
                        <td id="strMediumRiskCount">${mediumRiskCount}</td>
                    </tr>
                    <tr>
                        <td width="30%">Low Risk</td>
                        <td id="strLowRiskCount">${lowRiskCount}</td>
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
            <div id="message"><span id="lblText" style="color: Red; top: 50px;">Ready</span></div>
            <br/>
            <div class="progress-container">
                <div class="alignHorizontal">
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
            </div>
            <br/>
            <div class="alignHorizontal">
                <button id="updateSummary">Update Summary</button>
                <button id="fetchRiskScores">Pull Patient scores</button>
            </div>
        </fieldset>
    </div>
    <div>
        <fieldset>
            <legend>Generate IIT Risk Scores</legend>
            <br/>
            <div id="generateMessage"><span id="lblGenScoresText" style="color: Red; top: 50px;">Ready</span></div>
            <br/>
            <div class="generate-progress-container">
                <div class="alignHorizontal">
                    <div class="bootstrap-iso">
                        <div class="genscores-wait-loading prog-bar">
                            <div class="progress">
                                <div class="genscores-progress-bar progress-bar-striped" role="progressbar" style="width: 25%" aria-valuenow="25" aria-valuemin="0" aria-valuemax="100">
                                    <span class="genscores-prog-percentage"></span>
                                </div>
                            </div>
                            <div class="genscores-prog-status"></div>
                        </div>
                    </div>
                    <button id="stopGen">Stop Generating</button>
                </div>
            </div>
            <br/>
            <div class="alignHorizontal">
                <button id="updateLocalSummary">Update Summary</button>
                <button id="generateRiskScores">Generate Patient Scores</button>
            </div>
        </fieldset>
    </div>

</div>

<script type="text/javascript">
    jq = jQuery;
    jq(function() {

        var loadingImageURL = ui.resourceLink("kenyaemrml", "images/loading.gif");
        var isPullingData = false;
        var isGeneratingScores = false;

        //show data pull message
        function display_message(msg) {
            jq("#lblText").html(msg);
            //Show message for 3 seconds
            //jq('#message').fadeIn('slow').delay(3000).fadeOut('slow');
            setTimeout(function() {
                jq("#lblText").html(" ");
            }, 3000);
        }

        //show generate scores message
        function display_genscores_message(msg) {
            jq("#lblGenScoresText").html(msg);
            //Show message for 3 seconds
            //jq('#message').fadeIn('slow').delay(3000).fadeOut('slow');
            setTimeout(function() {
                jq("#lblGenScoresText").html(" ");
            }, 3000);
        }

        // display or hide the data pull progress indicator
        function display_loading(status) {
            if(status) {
                jq('.wait-loading').show();
            } else {
                jq('.wait-loading').hide();
            }
        }

        // display or hide the generate scores progress indicator
        function display_genscores_loading(status) {
            if(status) {
                jq('.genscores-wait-loading').show();
            } else {
                jq('.genscores-wait-loading').hide();
            }
        }

        //resets the data pull progress bar to begin from zero
        function resetProgressBar() {
            jq(".progress-bar").attr('aria-valuenow', 0).css('width', 0+'%');
            jq(".prog-status").html(0 + "/" + 0);
            jq(".prog-percentage").html(0+'%');
        }

        //resets the generate scores progress bar to begin from zero
        function resetGenScoresProgressBar() {
            jq(".genscores-progress-bar").attr('aria-valuenow', 0).css('width', 0+'%');
            jq(".genscores-prog-status").html(0 + "/" + 0);
            jq(".genscores-prog-percentage").html(0+'%');
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

        // handle click event of the stop gen button
        jq(document).on('click','#stopGen',function () {
            console.log('Stoping the generation task!');
            display_genscores_message('Stoping the generation task!');
            ui.getFragmentActionAsJson('kenyaemrml', 'iitRiskScoreGenerator', 'stopScoreGen', {}, function (result) {
                //stop generating IIT scores
            });
            display_genscores_loading(false);
            jq('#generateRiskScores').attr('disabled', false);
            jq('#stopGen').hide();  
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
            resetProgressBar();
            display_loading(true);
            jq('#fetchRiskScores').attr('disabled', true);
            jq('#stopPull').show();

            fetchDataAsync().done(function(){
                console.log('Finished the fetch task!');
                display_message('Finished the fetch task!');
                display_loading(false);
                jq('#fetchRiskScores').attr('disabled', false);
                jq('#stopPull').hide();
                updateSummaryTable(false);
                isPullingData = false;
                resetProgressBar();
            });

            fetchStatus();
        });

        // handle click event of the generate scores button
        jq(document).on('click','#generateRiskScores',function () {
            //Run the generate scores task
            console.log('Starting the generate scores task!');
            display_genscores_message('Starting the generate scores task!');
            resetGenScoresProgressBar();
            display_genscores_loading(true);
            jq('#generateRiskScores').attr('disabled', true);
            jq('#stopGen').show();

            generateScoresAsync().done(function(){
                console.log('Finished the generate scores task!');
                display_genscores_message('Finished the generate scores task!');
                display_genscores_loading(false);
                jq('#generateRiskScores').attr('disabled', false);
                jq('#stopGen').hide();
                updateSummaryTable(false);
                isGeneratingScores = false;
                resetGenScoresProgressBar();
            });

            generateScoresStatus();
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

        // generate scores
        function generateScoresAsync() {
            let dfrd = jq.Deferred();
            isGeneratingScores = true;
            //The Task
            ui.getFragmentActionAsJson('kenyaemrml', 'iitRiskScoreGenerator', 'generateIITScores', {}, function (result) {
                if(result) {
                    console.log('Success generating scores!');
                } else {
                    console.log('Failed to generate scores!');
                }
                dfrd.resolve();
            });

            return jq.when(dfrd).done(function(){
                console.log('Finished the generate scores task!');
            }).promise();
        }

        // Check the status of the data pull (one second cyclic async check)
        function fetchStatus() {
            getFetchStatusAsync().done(function(){
                if(isPullingData) {
                    setTimeout(function() {
                        updateSummaryTable(false);
                        fetchStatus();
                    },1000);
                }
            });
        }

        // Check the status of the generate scores task (one second cyclic async check)
        function generateScoresStatus() {
            getGenerateScoresStatusAsync().done(function(){
                if(isGeneratingScores) {
                    setTimeout(function() {
                        updateSummaryTable(false);
                        generateScoresStatus();
                    },1000);
                }
            });
        }

        // fetch the status of data pull asynchronously
        function getFetchStatusAsync() {
            let dfrd = jq.Deferred();
            //The Task
            ui.getFragmentActionAsJson('kenyaemrml', 'iitRiskScoreHistory', 'getStatusOfDataPull', {}, function (result) {
                if(result) {
                    let statusDone = result.done;
                    let statusTotal = result.total;
                    let statusPercent = result.percent;
                    jq(".progress-bar").attr('aria-valuenow', statusPercent).css('width', statusPercent+'%');
                    jq(".prog-status").html(statusDone + "/" + statusTotal);
                    jq(".prog-percentage").html(statusPercent+'%');
                    //console.log('Got done: ' + statusDone + ' Got total: ' + statusTotal + ' Got percent: ' + statusPercent);
                } else {
                    console.log('Failed to fetch pull status!');
                }
                dfrd.resolve();
            });

            return jq.when(dfrd).done(function(){
                console.log('Finished the fetch status check!');
            }).promise();
        }

        // fetch the status of generate scores asynchronously
        function getGenerateScoresStatusAsync() {
            let dfrd = jq.Deferred();
            //The Task
            ui.getFragmentActionAsJson('kenyaemrml', 'iitRiskScoreGenerator', 'getStatusOfGenerateScores', {}, function (result) {
                if(result) {
                    let statusDone = result.done;
                    let statusTotal = result.total;
                    let statusPercent = result.percent;
                    jq(".genscores-progress-bar").attr('aria-valuenow', statusPercent).css('width', statusPercent+'%');
                    jq(".genscores-prog-status").html(statusDone + "/" + statusTotal);
                    jq(".genscores-prog-percentage").html(statusPercent+'%');
                    console.log('Got done: ' + statusDone + ' Got total: ' + statusTotal + ' Got percent: ' + statusPercent);
                } else {
                    console.log('Failed to fetch generate scores status!');
                }
                dfrd.resolve();
            });

            return jq.when(dfrd).done(function(){
                console.log('Finished the generate scores status check!');
            }).promise();
        }

        // update the summary table
        function updateSummaryTable(notify) {
            notify = typeof notify !== "undefined" ? notify : true;
            ui.getFragmentActionAsJson('kenyaemrml', 'iitRiskScoreHistory', 'fetchLocalSummary', {}, function (result) {
                if(result) {
                    if(notify) display_message('Success fetching summary!');
                    if(notify) display_genscores_message('Success fetching summary!');
                    console.log('Success fetching summary!');
                    jq("#strTotalCount").html(result.totalCount);
                    jq("#strHighRiskCount").html(result.highRiskCount);
                    jq("#strMediumRiskCount").html(result.mediumRiskCount);
                    jq("#strLowRiskCount").html(result.lowRiskCount);
                } else {
                    if(notify) display_message('Failed to fetch summary!');
                    if(notify) display_genscores_message('Failed to fetch summary!');
                    console.log('Failed to fetch summary!');
                }
            });
        }

    });
</script>