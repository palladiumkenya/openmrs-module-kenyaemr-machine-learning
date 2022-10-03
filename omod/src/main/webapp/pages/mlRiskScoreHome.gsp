<%
    ui.decorateWith("kenyaemr", "standardPage", [ layout: "sidebar" ])
    def menuItems = [
            [label: "Synchronize data", iconProvider: "vdot", icon: "icons/Nimeconfirm_sync_38_3.png", label: "Synchronize data", href: ui.pageLink("kenyaemrml", "dataManagement")]
    ]
%>

<div class="ke-page-sidebar">
    ${ ui.includeFragment("kenyaemr", "patient/patientSearchForm", [ defaultWhich: "all" ]) }

    <div class="ke-panel-frame">
        ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Data Management Actions", items: menuItems])}
    </div>
</div>

<div class="ke-page-content">
    ${ ui.includeFragment("kenyaemr", "patient/patientSearchResults", [ pageProvider: "kenyaemrml", page: "iitRiskScorePatientView" ]) }
</div>

<script type="text/javascript">
    jQuery(function() {
        jQuery('input[name="query"]').focus();
    });
</script>