package org.openmrs.module.kenyaemrml.page.controller;

import org.openmrs.Patient;
import org.openmrs.module.kenyaemrml.ModuleConstants;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;

/**
 * Home pages controller
 */
@AppPage(ModuleConstants.APP_ML_PREDICTIONS)
public class MlRiskScoreHomePageController {
	
	public static final String MODEL_ATTR_CURRENT_PATIENT = "currentPatient";
	
	public String controller(PageModel model, UiUtils ui) {
		
		Patient patient = (Patient) model.getAttribute(MODEL_ATTR_CURRENT_PATIENT);
		
		if (patient != null) {
			return "redirect:"
			        + ui.pageLink("kenyaemrml", "iitRiskScorePatientView", SimpleObject.create("patientId", patient.getId()));
		} else {
			return null;
		}
	}
}
