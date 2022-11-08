package org.openmrs.module.kenyaemrml.page.controller;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrml.ModuleConstants;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Home pages controller
 */
@AppPage(ModuleConstants.APP_ML_PREDICTIONS)
public class IitRiskScorePatientViewPageController {
	
	public void controller(@RequestParam("patientId") Patient patient, PageModel model) {
		List<PatientRiskScore> riskScoreList = Context.getService(MLinKenyaEMRService.class).getPatientRiskScoreByPatient(
		    patient);
		model.put("hasData", riskScoreList.isEmpty() ? false : true);
	}
}
