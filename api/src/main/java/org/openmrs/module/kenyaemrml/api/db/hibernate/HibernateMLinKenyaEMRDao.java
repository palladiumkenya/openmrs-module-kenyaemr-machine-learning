/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrml.api.db.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.module.kenyaemrml.api.db.MLinKenyaEMRDao;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;

public class HibernateMLinKenyaEMRDao implements MLinKenyaEMRDao {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	private Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	/**
	 * Saves or updates risk score
	 * 
	 * @param riskScore
	 * @return
	 */
	public PatientRiskScore saveOrUpdateRiskScore(PatientRiskScore riskScore) {
		getSession().saveOrUpdate(riskScore);
		return riskScore;
	}
	
	/**
	 * Returns a PatientRiskScore for a given id
	 * 
	 * @param id
	 * @return
	 */
	public PatientRiskScore getPatientRiskScoreById(Integer id) {
		return (PatientRiskScore) getSession().createCriteria(PatientRiskScore.class).add(Restrictions.eq("id", id))
		        .uniqueResult();
		
	}
	
	/**
	 * Gets the latest PatientRiskScore for a patient
	 * 
	 * @param patient
	 * @return
	 */
	public PatientRiskScore getLatestPatientRiskScoreByPatient(Patient patient) {
		Criteria criteria = getSession().createCriteria(PatientRiskScore.class);
		criteria.add(Restrictions.eq("patient", patient));
		criteria.addOrder(Order.desc("evaluationDate"));
		criteria.setMaxResults(1);
		
		PatientRiskScore patientRiskScore = (PatientRiskScore) criteria.uniqueResult();
		
		return patientRiskScore;
	}
	
	/**
	 * Gets a list of risk score for a patient
	 * 
	 * @param patient
	 * @return
	 */
	public List<PatientRiskScore> getPatientRiskScoreByPatient(Patient patient) {
		return (List<PatientRiskScore>) getSession().createCriteria(PatientRiskScore.class)
		        .add(Restrictions.eq("patient", patient)).list();
		
	}
	
	/**
	 * Gets a list of risk score for a patient
	 * 
	 * @param patient
	 * @return
	 */
	public List<PatientRiskScore> getPatientRiskScoreByPatient(Patient patient, Date onOrBefore, Date onOrAfter) {
		return (List<PatientRiskScore>) getSession().createCriteria(PatientRiskScore.class)
		        .add(Restrictions.eq("patient", patient)).list();
	}
	
	/**
	 * Gets a list of risk score for a patient
	 * 
	 * @return
	 */
	public List<PatientRiskScore> getAllPatientRiskScore() {
		return (List<PatientRiskScore>) getSession().createCriteria(PatientRiskScore.class).list();
		
	}
}
