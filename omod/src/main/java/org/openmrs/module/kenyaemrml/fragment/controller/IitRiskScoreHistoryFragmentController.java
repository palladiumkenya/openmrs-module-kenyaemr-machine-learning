package org.openmrs.module.kenyaemrml.fragment.controller;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.kenyaemrml.util.MLDataExchange;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppAction;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

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
	
	/**
	 * Fetch data from Data Warehouse
	 * 
	 * @return true on success and false on failure
	 */
	@AppAction("kenyaemrml.predictions")
	public boolean fetchDataFromDWH(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui) {
		//Get global params
		//Auth
		//get total count by fetching only one record from remote
		//if remote total is bigger than local total, fetch and save the last N records
		//Fetch data 50 records at a time
		//Extract data from JSON
		//Save in the local DB
		User user = Context.getUserContext().getAuthenticatedUser();
		if(user != null) {
			user.setUserProperty("stopIITMLPull", "0");
		}

		MLDataExchange mlDataExchange = new MLDataExchange();
		boolean gotData = mlDataExchange.fetchDataFromDWH();

		return (gotData);
	}

	/**
	 * Stop Fetching data from Data Warehouse
	 * 
	 */
	@AppAction("kenyaemrml.predictions")
	public void stopDataPull(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui) {
		User user = Context.getUserContext().getAuthenticatedUser();
		if(user != null) {
			user.setUserProperty("stopIITMLPull", "1");
		}
	}
	
	/**
	 * Fetch local summary
	 * 
	 * @return true on success and false on failure
	 */
	@AppAction("kenyaemrml.predictions")
	public SimpleObject fetchLocalSummary(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui) {
		String strRiskThreshold = "kenyaemrml.palantir.high.iit.risk.threshold";
		GlobalProperty globalRiskThreshold = Context.getAdministrationService().getGlobalPropertyObject(strRiskThreshold);
		String riskThreshold = globalRiskThreshold.getPropertyValue();
		if (riskThreshold == null) {
			System.err.println("IIT ML - get data: Please set credentials for risk threshold");
			return (null);
		}
		
		Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		DbSessionFactory sf = Context.getRegisteredComponents(DbSessionFactory.class).get(0);
		
		String strTotalCount = "select count(*) from kenyaemr_ml_patient_risk_score;";
		String strHighRiskCount = "select count(*) from kenyaemr_ml_patient_risk_score where risk_score > " + riskThreshold
		        + ";";
		Long totalCount = (Long) Context.getAdministrationService().executeSQL(strTotalCount, true).get(0).get(0);
		Long highRiskCount = (Long) Context.getAdministrationService().executeSQL(strHighRiskCount, true).get(0).get(0);
		Long lowRiskCount = totalCount - highRiskCount;
		
		//prepare result
		SimpleObject summary = new SimpleObject();
		summary.put("totalCount", totalCount.intValue());
		summary.put("highRiskCount", highRiskCount.intValue());
		summary.put("lowRiskCount", lowRiskCount.intValue());
		summary.put("riskThreshhold", riskThreshold);
		
		Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		return summary;
	}
	
}
