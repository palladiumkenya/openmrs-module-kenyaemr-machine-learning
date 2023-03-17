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
import java.util.Date;

/**
 * Controller for getting a history of risk score and grouped by the date of evaluation
 */
public class IitRiskScoreGeneratorFragmentController {
	
	private static final Logger log = LoggerFactory.getLogger(IitRiskScoreGeneratorFragmentController.class);
	
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
	 * Generate IIT risk scores
	 * 
	 * @return true on success and false on failure
	 */
	@AppAction("kenyaemrml.predictions")
	public boolean generateIITScores(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui) {
		//Get global params
		//Auth
		//get total count by fetching only one record from remote
		//if remote total is bigger than local total, fetch and save the last N records
		//Fetch data 50 records at a time
		//Extract data from JSON
		//Save in the local DB
		User user = Context.getUserContext().getAuthenticatedUser();
		if(user != null) {
			user.setUserProperty("stopIITMLGen", "0"); //stop process = 1, continue process = 0
			user.setUserProperty("IITMLGenDone", "0"); //number of available records
			user.setUserProperty("IITMLGenTotal", "0"); //number of available records not processed
		}

		MLDataExchange mlDataExchange = new MLDataExchange();
		boolean gotData = mlDataExchange.generateIITScores();

		return (gotData);
	}

	/**
	 * Stop generating IIT scores
	 * 
	 */
	@AppAction("kenyaemrml.predictions")
	public void stopScoreGen(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui) {
		User user = Context.getUserContext().getAuthenticatedUser();
		if(user != null) {
			user.setUserProperty("stopIITMLGen", "1");
			user.setUserProperty("IITMLGenRunning", "0");
		}
	}

	/**
	 * Get started/stopped status of the IIT scores generation task
	 * 
	 * @return true if still running and false if stopped
	 */
	@AppAction("kenyaemrml.predictions")
	public Boolean getStartStopStatusOfScoreGen(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui) {
		User user = Context.getUserContext().getAuthenticatedUser();
		Boolean ret = false;
		if(user != null) {
			String status = user.getUserProperty("IITMLGenRunning");
			if(status != null && status.trim().equalsIgnoreCase("1")) {
				ret = true;
			}
		}
		return(ret);
	}

	/**
	 * Get progress status of the IIT scores generation task
	 * 
	 * @return SimpleObject done - number of records done, total - total number of records, percent - percentage of work done
	 */
	@AppAction("kenyaemrml.predictions")
	public SimpleObject getStatusOfGenerateScores(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui) {
		User user = Context.getUserContext().getAuthenticatedUser();
		SimpleObject summary = new SimpleObject();
		if(user != null) {
			long done = 0;
			long total = 0;
			String strDone = user.getUserProperty("IITMLGenDone");
			String strTotal = user.getUserProperty("IITMLGenTotal");
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
		
		SimpleObject summary = Context.getService(MLinKenyaEMRService.class).getIITRiskScoresSummary();

		return summary;
	}

	/**
	 * Gets the facility name given the facility code
	 * @return
	 */
	@AppAction("kenyaemrml.predictions")
	public SimpleObject getCurrentIITRiskScore(@RequestParam("patientId") Integer patientId, @SpringBean KenyaUiUtils kenyaUi, UiUtils ui) {
		SimpleObject ret = new SimpleObject();

		PatientRiskScore patientRiskScore = Context.getService(MLinKenyaEMRService.class).getLatestPatientRiskScoreByPatientRealTime(Context.getPatientService().getPatient(patientId));
		
		Date evaluationDate = patientRiskScore.getEvaluationDate();
		Double riskScore = patientRiskScore.getRiskScore();
		String description = patientRiskScore.getDescription();
		String riskFactors = patientRiskScore.getRiskFactors();

		ret.put("riskScore", riskScore > 0.00 ? (int) Math.rint((riskScore * 100)) + " %" : "-");
		ret.put("evaluationDate", evaluationDate != null ? kenyaUi.formatDate(evaluationDate) : "-");
		ret.put("description", description != null ? description : "-");
		ret.put("riskFactors", riskFactors != null ? riskFactors : "-");

		return(ret);
	}
	
}
