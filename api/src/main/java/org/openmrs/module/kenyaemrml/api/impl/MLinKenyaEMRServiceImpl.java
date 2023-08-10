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
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
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
		return mLinKenyaEMRDao.saveOrUpdateRiskScore(riskScore);
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
