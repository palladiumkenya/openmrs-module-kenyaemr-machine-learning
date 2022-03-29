package org.openmrs.module.kenyaemrml.fragment.controller;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for getting a history of risk score and grouped by the date of evaluation
 */
public class IitRiskScoreHistoryFragmentController {
	
	private static final Logger log = LoggerFactory.getLogger(IitRiskScoreHistoryFragmentController.class);
	
	public void controller(@RequestParam("patientId") Patient patient, PageModel model, UiUtils ui) {
		List<PatientRiskScore> riskScoreList = Context.getService(MLinKenyaEMRService.class).getPatientRiskScoreByPatient(
		    patient);
		List<List<Double>> riskTrend = new ArrayList<List<Double>>();
		if (riskScoreList != null && !riskScoreList.isEmpty()) {
			for (PatientRiskScore vObs : riskScoreList) {
				List<Double> dailyScore = new ArrayList<Double>();
				dailyScore.add((double) vObs.getEvaluationDate().getTime());
				dailyScore.add(vObs.getRiskScore());
				riskTrend.add(dailyScore);
			}
		}
		model.put("riskTrend", ui.toJson(riskTrend));
	}
	
}
