<%
    ui.decorateWith("kenyaui", "panel", [ heading: "IIT Risk Score", frameOnly: true ])
%>
<div class="ke-panel-content">

    <table>
        <tr>
            <tr>
                <td> Risk Score </td> <td> <div id="riskScore">-</div> </td>
            </tr>
            <tr>
                <td>Evaluation Date </td><td> <div id="evaluationDate">-</div> </td>
            </tr>
            <tr>
                <td>Description </td><td> <div id="description">-</div> </td>
            </tr>
            <tr>
                <td>Risk Factors </td><td> <div id="riskFactors">-</div> </td>
            </tr>
        </tr>
    </table>

</div>

<script type="text/javascript">
    jq = jQuery;
    jq(function() {
        var patientId = ${ patientId };
        var riskScore = "";
        var evaluationDate = "";
        var description = "";
        var riskFactors = "";
    
        // generate patient score
        function generateScoreAsync() {
            let dfrd = jq.Deferred();
            //The Task
            ui.getFragmentActionAsJson('kenyaemrml', 'iitRiskScoreGenerator', 'getCurrentIITRiskScore', {patientId : patientId}, function (result) {
                if(result) {
                    console.log('Success generating patient IIT Risk score!: ' + JSON.stringify(result));
                    riskScore = result.riskScore;
                    evaluationDate = result.evaluationDate;
                    description = result.description;
                    riskFactors = result.riskFactors;
                } else {
                    console.log('Failed to generate patient IIT Risk score!');
                }
                dfrd.resolve();
            });

            return jq.when(dfrd).done(function(){
                console.log('Finished generating patient IIT score!');
            }).promise();
        }

        console.log('Getting IIT Risk score asynchronously for patient: ' + patientId);
        generateScoreAsync().done(function(){
            console.log('Updating the IIT Risk score table');
            // Update the table
            jq("#riskScore").html(riskScore);
            jq("#evaluationDate").html(evaluationDate);
            jq("#description").html(description);
            jq("#riskFactors").html(riskFactors);
        });
    });
</script>


