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

import org.openmrs.Patient;
import org.openmrs.api.UserService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.api.db.hibernate.HibernateMLinKenyaEMRDao;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;

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
	public Collection<Integer> getAllPatientsWithHighRiskScores() {
		return mLinKenyaEMRDao.getAllPatientsWithHighRiskScores();
	}

	@Override
	public Collection<Integer> getAllPatientsWithMediumRiskScores() {
		return mLinKenyaEMRDao.getAllPatientsWithMediumRiskScores();
	}

	@Override
	public Collection<Integer> getAllPatientsWithLowRiskScores() {
		return mLinKenyaEMRDao.getAllPatientsWithLowRiskScores();
	}

	@Override
	public Collection<Integer> getAllPatients() {
		return mLinKenyaEMRDao.getAllPatients();
	}
}
