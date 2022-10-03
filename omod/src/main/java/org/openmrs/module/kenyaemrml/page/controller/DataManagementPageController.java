package org.openmrs.module.kenyaemrml.page.controller;

import java.util.Collection;

import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrml.ModuleConstants;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.util.PrivilegeConstants;

/**
 * Home pages controller
 */
@AppPage(ModuleConstants.APP_ML_PREDICTIONS)
public class DataManagementPageController {
	
	public void get(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui, PageModel model) {
		Collection<Integer> high = Context.getService(MLinKenyaEMRService.class).getAllPatientsWithHighRiskScores();
		Collection<Integer> medium = Context.getService(MLinKenyaEMRService.class).getAllPatientsWithMediumRiskScores();
		Collection<Integer> low = Context.getService(MLinKenyaEMRService.class).getAllPatientsWithLowRiskScores();
		Collection<Integer> all = Context.getService(MLinKenyaEMRService.class).getAllPatients();
		
		model.put("totalCount", all.size());
		model.put("highRiskCount", high.size());
		model.put("mediumRiskCount", medium.size());
		model.put("lowRiskCount", low.size());
		
		Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
	}
}
