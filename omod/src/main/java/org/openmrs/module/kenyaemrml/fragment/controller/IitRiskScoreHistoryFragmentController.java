package org.openmrs.module.kenyaemrml.fragment.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.kenyaemrml.util.MLDataExchange;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppAction;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
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
		List<List<Long>> riskTrend = new ArrayList<List<Long>>();
		if (riskScoreList != null && !riskScoreList.isEmpty()) {
			for (PatientRiskScore vObs : riskScoreList) {
				List<Long> dailyScore = new ArrayList<Long>();
				dailyScore.add(vObs.getEvaluationDate().getTime());
				Double riskScore = vObs.getRiskScore() * 100;
				dailyScore.add(riskScore.longValue());
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
			user.setUserProperty("stopIITMLPull", "0"); //stop process = 1, continue process = 0
			user.setUserProperty("IITMLPullDone", "0"); //number of received records
			user.setUserProperty("IITMLPullTotal", "0"); //number of available records on remote
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
	 * Get current status of Fetching data from Data Warehouse
	 * 
	 */
	@AppAction("kenyaemrml.predictions")
	public SimpleObject getStatusOfDataPull(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui) {
		User user = Context.getUserContext().getAuthenticatedUser();
		SimpleObject summary = new SimpleObject();
		if(user != null) {
			long done = 0;
			long total = 0;
			String strDone = user.getUserProperty("IITMLPullDone");
			String strTotal = user.getUserProperty("IITMLPullTotal");
			try {
				done = Long.parseLong(strDone);
				total = Long.parseLong(strTotal);
			} catch(Exception ex) {}
			double percent = 0.00;
			if(total > 0.00 && done > 0.00) {
				percent = ((done * 1.00/total * 1.00) * 100.0);
			}
			summary.put("done", done);
			summary.put("total", total);
			summary.put("percent", (int)Math.floor(percent));
		}
		return(summary);
	}
	
	/**
	 * Fetch local summary
	 * 
	 * @return true on success and false on failure
	 */
	@AppAction("kenyaemrml.predictions")
	public SimpleObject fetchLocalSummary(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui) {
		Collection<Integer> high = Context.getService(MLinKenyaEMRService.class).getAllPatientsWithHighRiskScores();
		Collection<Integer> medium = Context.getService(MLinKenyaEMRService.class).getAllPatientsWithMediumRiskScores();
		Collection<Integer> low = Context.getService(MLinKenyaEMRService.class).getAllPatientsWithLowRiskScores();
		Collection<Integer> all = Context.getService(MLinKenyaEMRService.class).getAllPatients();
		
		//prepare result
		SimpleObject summary = new SimpleObject();
		summary.put("totalCount", all.size());
		summary.put("highRiskCount", high.size());
		summary.put("mediumRiskCount", medium.size());
		summary.put("lowRiskCount", low.size());

		return summary;
	}
	
}
