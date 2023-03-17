/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrml.api.db;

import java.util.Date;
import java.util.List;
import java.util.Collection;

import org.openmrs.Patient;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.ui.framework.SimpleObject;

//@Repository("kenyaemrml.MLinKenyaEMRDao")
public interface MLinKenyaEMRDao {
	
	/**
	 * Saves or updates risk score
	 * 
	 * @param riskScore
	 * @return
	 */
	public PatientRiskScore saveOrUpdateRiskScore(PatientRiskScore riskScore);
	
	/**
	 * Returns a PatientRiskScore for a given id
	 * 
	 * @param id
	 * @return
	 */
	public PatientRiskScore getPatientRiskScoreById(Integer id);
	
	/**
	 * Gets the latest PatientRiskScore for a patient
	 * 
	 * @param patient
	 * @return
	 */
	public PatientRiskScore getLatestPatientRiskScoreByPatient(Patient patient);

	/**
	 * Get all ML patients with HIGH risk scores
	 * @return a list of patients
	 */
	public Collection<Integer> getAllPatientsWithHighRiskScores();

	/**
	 * Get all ML patients with MEDIUM risk scores
	 * @return a list of patients
	 */
	public Collection<Integer> getAllPatientsWithMediumRiskScores();

	/**
	 * Get all ML patients with LOW risk scores
	 * @return a list of patients
	 */
	public Collection<Integer> getAllPatientsWithLowRiskScores();

	/**
	 * Get a summary of IIT risk scores
	 * @return a summary
	 */
	public SimpleObject getIITRiskScoresSummary();

	/**
	 * Get all ML patients
	 * @return a list of patients
	 */
	public Collection<Integer> getAllPatients();
	
	/**
	 * Gets a list of risk score for a patient
	 * 
	 * @param patient
	 * @return
	 */
	public List<PatientRiskScore> getPatientRiskScoreByPatient(Patient patient);
	
	/**
	 * Gets a list of risk score for a patient
	 * 
	 * @param patient
	 * @return
	 */
	public List<PatientRiskScore> getPatientRiskScoreByPatient(Patient patient, Date onOrBefore, Date onOrAfter);
	
	/**
	 * Gets a list of risk score for a patient
	 * 
	 * @return
	 */
	public List<PatientRiskScore> getAllPatientRiskScore();

	/**
	 *  Gets the latest risk evaluation date for all patient records
	 */
    Date getLatestRiskEvaluationDate();

	/**
	 *  Gets the latest risk evaluation date for a patient
	 */
	Date getPatientLatestRiskEvaluationDate(Patient patient);
}
