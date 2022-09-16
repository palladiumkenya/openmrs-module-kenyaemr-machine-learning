<%
	ui.decorateWith("kenyaui", "panel", [ heading: "" ])
	ui.includeJavascript("kenyaemr", "highcharts.js")
	ui.includeJavascript("kenyaemr", "highcharts-grouped-categories.js")
%>
<style>
.highcharts-figure, .highcharts-data-table table {
	min-width: 360px;
	max-width: 800px;
	margin: 1em auto;
}
.highcharts-data-table table {
	font-family: Verdana, sans-serif;
	border-collapse: collapse;
	border: 1px solid #EBEBEB;
	margin: 10px auto;
	text-align: center;
	width: 100%;
	max-width: 500px;
}
.highcharts-data-table caption {
	padding: 1em 0;
	font-size: 1.2em;
	color: #555;
}
.highcharts-data-table th {
	font-weight: 600;
	padding: 0.5em;
}
.highcharts-data-table td, .highcharts-data-table th, .highcharts-data-table caption {
	padding: 0.5em;
}
.highcharts-data-table thead tr, .highcharts-data-table tr:nth-child(even) {
	background: #f8f8f8;
}
.highcharts-data-table tr:hover {
	background: #f1f7ff;
}
</style>

<div>

	<fieldset>
		<legend></legend>

		<table width="98%">
			<tr>
				<td colspan="4">
					<div id="prediction_score_chart" style="min-width: 450px; height: 300px; margin: 0 auto"></div>
				</td>
			</tr>
		</table>

	</fieldset>

</div>
<script>
    jQuery(function () {
        jQuery('#prediction_score_chart').highcharts({
            chart: {
                type: 'line'
            },
            rangeSelector: {
                allButtonsEnabled: true
            },
            title: {
                text: 'IIT Risk score'
            },
            subtitle: {
                text: 'Source: KeHMIS ML Model'
            },
            xAxis: {
                accessibility: {
                    rangeDescription: ''
                },
				type: 'datetime'
            },
            yAxis: {
                title: {
                    text: 'Risk score (%)'
                }
            },
            legend: {
                layout: 'vertical',
                align: 'right',
                verticalAlign: 'middle'
            },
            plotOptions: {
                series: {
                    label: {
                        connectorAllowed: false
                    },
                    pointStart: 2010
                }
            },
            tooltip: {
                headerFormat: '<span style="font-size:11px">{series.name}</span><br>',
                pointFormat: '<span style="color:{point.color}">{point.name}</span>: <b>{point.y:.0f}</b><br/>'
            },
            series: [{
                name: 'Score',
                data: ${ riskTrend }
            }],
            responsive: {
                rules: [{
                    condition: {
                        maxWidth: 700
                    },
                    chartOptions: {
                        legend: {
                            layout: 'horizontal',
                            align: 'center',
                            verticalAlign: 'bottom'
                        }
                    }
                }]
            }
        });
	});
</script>