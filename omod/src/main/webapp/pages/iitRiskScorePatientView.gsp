<%
    ui.decorateWith("kenyaemr", "standardPage", [ patient: currentPatient ])
%>

<div class="ke-page-content">
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <td width="30%" valign="top">
                ${ ui.includeFragment("kenyaemr", "patient/patientSummary", [ patient: currentPatient ]) }
            </td>
            <td width="55%" valign="top" style="padding-left: 5px">
                ${ ui.includeFragment("kenyaemrml", "iitRiskScoreHistory", [ patientId: currentPatient ]) }
            </td>
        </tr>
    </table>

</div>