package org.openmrs.module.kenyaemrml.fragment.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.kenyaemrml.util.MLDataExchange;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppAction;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Date;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.Visit;

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
		System.err.println("IIT ML: Stopping score generation");
		User user = Context.getUserContext().getAuthenticatedUser();
		if(user != null) {
			System.err.println("IIT ML: Stopping score generation: setting stop vars");
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

		PatientRiskScore patientRiskScore = new PatientRiskScore();
		Patient patient = Context.getPatientService().getPatient(patientId);
		List<Visit> visits = Context.getVisitService().getActiveVisitsByPatient(patient);

		// Check if we are currently checked in
		if(visits.size() > 0) {
			//check if we have a saved score
			Date lastScore = Context.getService(MLinKenyaEMRService.class).getPatientLatestRiskEvaluationDate(patient);
			if(lastScore != null) {
				//check if a green card has been filled since the last score
				Form hivGreenCardForm = MetadataUtils.existing(Form.class, HivMetadata._Form.HIV_GREEN_CARD);
				List<Form> hivCareForms = Arrays.asList(hivGreenCardForm);
				Location defaultLocation = Context.getService(KenyaEmrService.class).getDefaultLocation();
				EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder()
							.setIncludeVoided(false)
							.setFromDate(lastScore)
							// .setToDate(new Date())
							.setPatient(patient)
							.setEnteredViaForms(hivCareForms)
							.setLocation(defaultLocation)
							.createEncounterSearchCriteria();
				List<Encounter> hivCareEncounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
				if(hivCareEncounters.size() > 0) {
					// We have had a greencard form filled after the last encounter, we can now generate a new score NB: Greencard save should have triggered score generation
					patientRiskScore = Context.getService(MLinKenyaEMRService.class).getLatestPatientRiskScoreByPatientRealTime(patient);
				} else {
					patientRiskScore = Context.getService(MLinKenyaEMRService.class).getLatestPatientRiskScoreByPatient(patient);
				}
			} else {
				patientRiskScore = Context.getService(MLinKenyaEMRService.class).getLatestPatientRiskScoreByPatient(patient, false);
			}
		} else {
			Date checkScore = Context.getService(MLinKenyaEMRService.class).getPatientLatestRiskEvaluationDate(patient);
			if(checkScore == null) {
				patientRiskScore = Context.getService(MLinKenyaEMRService.class).getLatestPatientRiskScoreByPatient(patient, false);
			} else {
				patientRiskScore = Context.getService(MLinKenyaEMRService.class).getLatestPatientRiskScoreByPatient(patient);
			}
		}

		Date evaluationDate = null;
		Double riskScore = null;
		String description = null;
		String riskFactors = null;
		
		if(patientRiskScore != null) {
			evaluationDate = patientRiskScore.getEvaluationDate();
			riskScore = patientRiskScore.getRiskScore();
			description = patientRiskScore.getDescription();
			riskFactors = patientRiskScore.getRiskFactors();
		}

		ret.put("riskScore", (riskScore != null && riskScore > 0.00) ? (int) Math.rint((riskScore * 100)) + " %" : "-");
		ret.put("evaluationDate", evaluationDate != null ? kenyaUi.formatDate(evaluationDate) : "-");
		ret.put("description", description != null ? description : "-");
		ret.put("riskFactors", riskFactors != null ? riskFactors : "-");

		return(ret);
	}
	
}
