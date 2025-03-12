/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrml.api.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemrml.ModuleConstants;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.api.ModelService;
import org.openmrs.module.kenyaemrml.api.db.hibernate.HibernateMLinKenyaEMRDao;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.ui.framework.SimpleObject;

public class MLinKenyaEMRServiceImpl extends BaseOpenmrsService implements MLinKenyaEMRService {
	
	HibernateMLinKenyaEMRDao mLinKenyaEMRDao;
	
	UserService userService;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setMLinKenyaEMRDao(HibernateMLinKenyaEMRDao mLinKenyaEMRDao) {
		this.mLinKenyaEMRDao = mLinKenyaEMRDao;
	}
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public PatientRiskScore saveOrUpdateRiskScore(PatientRiskScore riskScore) {
		saveIITRiskScoreAsAnObs(riskScore);
		return mLinKenyaEMRDao.saveOrUpdateRiskScore(riskScore);
	}

	/**
	 * Save the given IIT risk score as an OBS
	 * 
	 */
	private Boolean saveIITRiskScoreAsAnObs(PatientRiskScore riskScore) {
		Boolean ret = false;

		try {
			// Save as an OBS
			// Create an encounter
			EncounterService encounterService = Context.getEncounterService();
			ConceptService conceptService = Context.getConceptService();
			ObsService obsService = Context.getObsService();

			EncounterType encounterType = encounterService.getEncounterTypeByUuid(ModuleConstants.IIT_SCORE_ENCOUNTER_TYPE);

			if(encounterType != null) {
				// Create new encounter
				Encounter encounter = new Encounter();
				encounter.setEncounterType(encounterType);
				encounter.setPatient(riskScore.getPatient());
				Location defaultLocation = Context.getService(KenyaEmrService.class).getDefaultLocation();
				encounter.setLocation(defaultLocation);
				encounter.setEncounterDatetime(new Date());
				encounter.setCreator(Context.getAuthenticatedUser());
				encounter.setDateCreated(new Date());
				// Save encounter
				encounterService.saveEncounter(encounter);

				// create OBS for IIT value
				Concept valueConcept = conceptService.getConceptByUuid(ModuleConstants.IIT_SCORE_RESULT_CONCEPT);
				Obs obs = new Obs();
				obs.setPerson(riskScore.getPatient());
				obs.setConcept(valueConcept);
				obs.setValueNumeric(riskScore.getRiskScore());
				obs.setObsDatetime(new Date());
				obs.setEncounter(encounter);
				
				obsService.saveObs(obs, "Added IIT numeric observation");
				
				encounter.addObs(obs);
				encounterService.saveEncounter(encounter);

				System.out.println("OrderEntry Module: Success creating IIT value obs");

				// create OBS for IIT description (VERY HIGH, HIGH, MEDIUM, LOW)
				String description = riskScore.getDescription();
				description = description.trim().toLowerCase();
				if(description.equalsIgnoreCase("Low Risk") || description.equalsIgnoreCase("Medium Risk") || description.equalsIgnoreCase("High Risk") || description.equalsIgnoreCase("Very High Risk")) {
					Concept descriptionAnswerConcept = null;
					if(description.equalsIgnoreCase("Low Risk")) {
						descriptionAnswerConcept = conceptService.getConceptByUuid(ModuleConstants.IIT_SCORE_DESCRIPTION_LOW_ANSWER_CONCEPT);
					} else if(description.equalsIgnoreCase("Medium Risk")) {
						descriptionAnswerConcept = conceptService.getConceptByUuid(ModuleConstants.IIT_SCORE_DESCRIPTION_MEDIUM_ANSWER_CONCEPT);
					} else if(description.equalsIgnoreCase("High Risk")) {
						descriptionAnswerConcept = conceptService.getConceptByUuid(ModuleConstants.IIT_SCORE_DESCRIPTION_HIGH_ANSWER_CONCEPT);
					} else if(description.equalsIgnoreCase("Very High Risk")) {
						descriptionAnswerConcept = conceptService.getConceptByUuid(ModuleConstants.IIT_SCORE_DESCRIPTION_VERYHIGH_ANSWER_CONCEPT);
					}
					if(descriptionAnswerConcept != null) {
						Concept descriptionQuestionConcept = conceptService.getConceptByUuid(ModuleConstants.IIT_SCORE_DESCRIPTION_QUESTION_CONCEPT);
						if(descriptionQuestionConcept != null) {
							Obs descriptionObs = new Obs();
							descriptionObs.setPerson(riskScore.getPatient());
							descriptionObs.setConcept(descriptionQuestionConcept);
							descriptionObs.setValueCoded(descriptionAnswerConcept);
							descriptionObs.setObsDatetime(new Date());
							descriptionObs.setEncounter(encounter);
							
							obsService.saveObs(descriptionObs, "Added IIT description observation");
							
							encounter.addObs(descriptionObs);
							encounterService.saveEncounter(encounter);

							System.out.println("OrderEntry Module: Success creating IIT description obs");

							return true;
						} else {
							System.err.println("OrderEntry Module: Could not get concept for this description question : Not creating a description obs");
						}
					} else {
						System.err.println("OrderEntry Module: Could not get concept for this description : Not creating a description obs: " + description);
					}
				} else {
					System.err.println("OrderEntry Module: Could not find a matching concept for this IIT score description: " + description + " : Not creating a description obs");
				}
			} else {
				System.err.println("OrderEntry Module: Error creating IIT score as an OBS: could not find IIT score encounter type");
			}
		} catch(Exception ex) {
			System.err.println("OrderEntry Module: Error saving the IIT score as an OBS: " + ex.getMessage());
			ex.printStackTrace();
		}

		return ret;
	}
	
	@Override
	public PatientRiskScore getPatientRiskScoreById(Integer id) {
		return mLinKenyaEMRDao.getPatientRiskScoreById(id);
	}

	@Override
	public PatientRiskScore getLatestPatientRiskScoreByPatient(Patient patient) {
		return mLinKenyaEMRDao.getLatestPatientRiskScoreByPatient(patient);
	}
	
	@Override
	public PatientRiskScore getLatestPatientRiskScoreByPatient(Patient patient, Boolean reporting) {
		Date lastScore = getPatientLatestRiskEvaluationDate(patient);
		Date dateToday = new Date();
		// If it is the same day or we are reporting, fetch from DB
		if((lastScore != null && DateUtils.isSameDay(lastScore, dateToday)) || reporting) {
			return mLinKenyaEMRDao.getLatestPatientRiskScoreByPatient(patient);
		} else {
			// Check if IIT is enabled
			String iitFeatureEnabled = "kenyaemrml.iitml.feature.enabled";
			GlobalProperty gpIITFeatureEnabled = Context.getAdministrationService().getGlobalPropertyObject(iitFeatureEnabled);

			if(gpIITFeatureEnabled != null && gpIITFeatureEnabled.getPropertyValue().trim().equalsIgnoreCase("true")) {
				// System.out.println("IIT ML Score: Generating a new risk score || and saving to DB");
				ModelService modelService = Context.getService(ModelService.class);
				PatientRiskScore patientRiskScore = modelService.generatePatientRiskScore(patient);
				// Save/Update to DB (for reports) -- Incase a record for current date doesnt exist
				saveOrUpdateRiskScore(patientRiskScore);
				return(patientRiskScore);
			} else {
				return mLinKenyaEMRDao.getLatestPatientRiskScoreByPatient(patient);
			}
		}
	}

	@Override
	public PatientRiskScore getLatestPatientRiskScoreByPatientRealTime(Patient patient) {
		// Check if IIT is enabled
        String iitFeatureEnabled = "kenyaemrml.iitml.feature.enabled";
        GlobalProperty gpIITFeatureEnabled = Context.getAdministrationService().getGlobalPropertyObject(iitFeatureEnabled);

        if(gpIITFeatureEnabled != null && gpIITFeatureEnabled.getPropertyValue().trim().equalsIgnoreCase("true")) {
			// System.out.println("IIT ML Score: Generating a new risk score || and saving to DB");
			ModelService modelService = Context.getService(ModelService.class);
			PatientRiskScore patientRiskScore = modelService.generatePatientRiskScore(patient);
			// Save/Update to DB (for reports) -- Incase a record for current date doesnt exist
			saveOrUpdateRiskScore(patientRiskScore);
			return(patientRiskScore);
		} else {
			return mLinKenyaEMRDao.getLatestPatientRiskScoreByPatient(patient);
		}
	}
	
	@Override
	public List<PatientRiskScore> getPatientRiskScoreByPatient(Patient patient) {
		return mLinKenyaEMRDao.getPatientRiskScoreByPatient(patient);
	}
	
	@Override
	public List<PatientRiskScore> getPatientRiskScoreByPatient(Patient patient, Date onOrBefore, Date onOrAfter) {
		return mLinKenyaEMRDao.getPatientRiskScoreByPatient(patient, onOrBefore, onOrAfter);
	}
	
	@Override
	public List<PatientRiskScore> getAllPatientRiskScore() {
		return mLinKenyaEMRDao.getAllPatientRiskScore();
	}

	@Override
	public Date getLatestRiskEvaluationDate() {
		return mLinKenyaEMRDao.getLatestRiskEvaluationDate();
	}

	@Override
	public SimpleObject getIITRiskScoresSummary() {
		return mLinKenyaEMRDao.getIITRiskScoresSummary();
	}

	@Override
	public Collection<Integer> getAllPatients() {
		return mLinKenyaEMRDao.getAllPatients();
	}

	@Override
	public Date getPatientLatestRiskEvaluationDate(Patient patient) {
		return mLinKenyaEMRDao.getPatientLatestRiskEvaluationDate(patient);
	}
}
