/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrml.api;

import java.util.Date;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.kenyaemrml.MLinKenyaEMRConfig;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface MLinKenyaEMRService extends OpenmrsService {
		
	/**
	 * Saves or updates risk score
	 * 
	 * @param riskScore
	 * @return
	 */
	@Authorized(MLinKenyaEMRConfig.MODULE_PRIVILEGE)
	@Transactional
	PatientRiskScore saveOrUpdateRiskScore(PatientRiskScore riskScore);
	
	/**
	 * Returns a PatientRiskScore for a given id
	 * 
	 * @param id
	 * @return
	 */
	PatientRiskScore getPatientRiskScoreById(Integer id);
	
	/**
	 * Gets the latest PatientRiskScore for a patient
	 * 
	 * @param patient
	 * @return
	 */
	PatientRiskScore getLatestPatientRiskScoreByPatient(Patient patient);

	/**
	 * Get all patients with high risk scores
	 * @return a list of patients
	 */
	public Collection<Integer> getAllPatientsWithHighRiskScores();

	/**
	 * Get all patients with medium risk scores
	 * @return a list of patients
	 */
	public Collection<Integer> getAllPatientsWithMediumRiskScores();

	/**
	 * Get all patients with low risk scores
	 * @return a list of patients
	 */
	public Collection<Integer> getAllPatientsWithLowRiskScores();

	/**
	 * Get all patients
	 * @return a list of patients
	 */
	public Collection<Integer> getAllPatients();
	
	/**
	 * Gets a list of risk score for a patient
	 * 
	 * @param patient
	 * @return
	 */
	List<PatientRiskScore> getPatientRiskScoreByPatient(Patient patient);
	
	/**
	 * Gets a list of risk score for a patient
	 * 
	 * @param patient
	 * @return
	 */
	List<PatientRiskScore> getPatientRiskScoreByPatient(Patient patient, Date onOrBefore, Date onOrAfter);
	
	/**
	 * Gets a list of risk score for a patient
	 * 
	 * @return
	 */
	List<PatientRiskScore> getAllPatientRiskScore();

	Date getLatestRiskEvaluationDate();

}
