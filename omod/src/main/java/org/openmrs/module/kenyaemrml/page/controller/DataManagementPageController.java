package org.openmrs.module.kenyaemrml.page.controller;

import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.module.kenyaemrml.ModuleConstants;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.util.PrivilegeConstants;
import java.text.SimpleDateFormat;
import org.openmrs.Person;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Home pages controller
 */
@AppPage(ModuleConstants.APP_ML_PREDICTIONS)
public class DataManagementPageController {
	
	public void get(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui, PageModel model) {
		System.err.println("IIT ML - Getting Risk Scores");
		String strRiskThreshold = "kenyaemrml.palantir.high.iit.risk.threshold";
		GlobalProperty globalRiskThreshold = Context.getAdministrationService().getGlobalPropertyObject(strRiskThreshold);
		String riskThreshold = globalRiskThreshold.getPropertyValue();
		if (riskThreshold == null) {
			System.err.println("ML get data: Please set credentials for risk threshold");
			return;
		}
		
		Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		DbSessionFactory sf = Context.getRegisteredComponents(DbSessionFactory.class).get(0);
		
		String strTotalCount = "select count(*) from kenyaemr_ml_patient_risk_score;";
		String strHighRiskCount = "select count(*) from kenyaemr_ml_patient_risk_score where risk_score > " + riskThreshold
		        + ";";
		Long totalCount = (Long) Context.getAdministrationService().executeSQL(strTotalCount, true).get(0).get(0);
		Long highRiskCount = (Long) Context.getAdministrationService().executeSQL(strHighRiskCount, true).get(0).get(0);
		Long lowRiskCount = totalCount - highRiskCount;
		
		model.put("totalCount", totalCount.intValue());
		model.put("highRiskCount", highRiskCount.intValue());
		model.put("lowRiskCount", lowRiskCount.intValue());
		model.put("riskThreshhold", riskThreshold);
		
		Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
	}
}
