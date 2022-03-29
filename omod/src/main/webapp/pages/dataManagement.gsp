<%
    ui.decorateWith("kenyaemr", "standardPage", [ layout: "sidebar" ])
    def menuItems = [
            [label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to IIT home", href: ui.pageLink("kenyaemrml", "mlRiskScoreHome")]
    ]
%>
<style>
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
</style>

<div class="ke-page-sidebar">

    <div class="ke-panel-frame">
        ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Navigation", items: menuItems])}
    </div>
</div>

<div class="ke-page-content">

    <fieldset>
        <legend>Fetch IIT risk from NDWH</lenged>

        <br/>
        <span id="msgBox2" style="color: green"></span><br/>
        <button  id="fetchRiskScores">Pull Patient scores</button>
    </fieldset>

</div>

<script type="text/javascript">
    jq = jQuery;
    jQuery(function() {
        jq('#fetchRiskScoress').click(function() {
            jq.getJSON('${ ui.actionLink("vdot", "vdotPatientData", "getNimeConfirmVideoObs") }')
                .success(function(data) {
                    jq('#msgBox2').html("Vdot data pulled successfully");
                })
                .error(function(xhr, status, err) {
                    jq('#msgBox2').html("There was an error pulling Vdot messages");
                })
        });
    });
</script>