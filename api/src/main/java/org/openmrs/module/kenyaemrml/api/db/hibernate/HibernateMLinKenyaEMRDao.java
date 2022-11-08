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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.openmrs.Patient;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.api.db.MLinKenyaEMRDao;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;

import org.openmrs.Program;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.patient.PatientCalculationService;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import java.util.Set;
import org.openmrs.module.kenyacore.calculation.Filters;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.context.annotation.Bean;
import org.openmrs.api.context.Context;

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
	 * Get all ML patients with HIGH risk scores and who are alive and on HIV program
	 * @return a list of patients
	 */
	@Override
	public Collection<Integer> getAllPatientsWithHighRiskScores() {
		PatientCalculationService service = Context.getService(PatientCalculationService.class);
		PatientCalculationContext patientCalculationContext = service.createCalculationContext();
		Criteria criteria = getSession().createCriteria(PatientRiskScore.class);

		List<PatientRiskScore> pList = criteria.list();
		HashSet<Integer> hIds = new HashSet<>();

		for (PatientRiskScore patientRiskScore : pList) {
			Patient patient = patientRiskScore.getPatient();
			if(patient != null)	{	
				Integer pId = patient.getPatientId();
				if(!hIds.contains(pId)) {
					hIds.add(pId);
				}
			}
		}
		// Get HIV program
		Program hivProgram = MetadataUtils.existing(Program.class, HivMetadata._Program.HIV);
		// Get all patients who are alive and in HIV program
		Set<Integer> alive = Filters.alive(hIds, patientCalculationContext);
		Set<Integer> inHivProgram = Filters.inProgram(hivProgram, alive, patientCalculationContext);
		
		HashSet<Integer> ret = new HashSet<>();
		for (Integer ptId : inHivProgram) {
			PatientRiskScore latestRiskScore = Context.getService(MLinKenyaEMRService.class)
							.getLatestPatientRiskScoreByPatient(Context.getPatientService().getPatient(ptId));
			if (latestRiskScore != null) {
				String riskGroup = latestRiskScore.getDescription();
				if (riskGroup.trim().equalsIgnoreCase("High Risk")) {
					ret.add(ptId);
				}				
			}
		}
		
		return(ret);
	}

	/**
	 * Get all ML patients with MEDIUM risk scores and who are alive and on HIV program
	 * @return a list of patients
	 */
	@Override
	public Collection<Integer> getAllPatientsWithMediumRiskScores() {
		PatientCalculationService service = Context.getService(PatientCalculationService.class);
		PatientCalculationContext patientCalculationContext = service.createCalculationContext();
		Criteria criteria = getSession().createCriteria(PatientRiskScore.class);

		List<PatientRiskScore> pList = criteria.list();
		HashSet<Integer> hIds = new HashSet<>();

		for (PatientRiskScore patientRiskScore : pList) {
			Patient patient = patientRiskScore.getPatient();
			if(patient != null)	{	
				Integer pId = patient.getPatientId();
				if(!hIds.contains(pId)) {
					hIds.add(pId);
				}
			}
		}
		// Get HIV program
		Program hivProgram = MetadataUtils.existing(Program.class, HivMetadata._Program.HIV);
		// Get all patients who are alive and in HIV program
		Set<Integer> alive = Filters.alive(hIds, patientCalculationContext);
		Set<Integer> inHivProgram = Filters.inProgram(hivProgram, alive, patientCalculationContext);

		HashSet<Integer> ret = new HashSet<>();
		for (Integer ptId : inHivProgram) {
			PatientRiskScore latestRiskScore = Context.getService(MLinKenyaEMRService.class)
							.getLatestPatientRiskScoreByPatient(Context.getPatientService().getPatient(ptId));
			if (latestRiskScore != null) {
				String riskGroup = latestRiskScore.getDescription();
				if (riskGroup.trim().equalsIgnoreCase("Medium Risk")) {
					ret.add(ptId);
				}				
			}
		}
		
		return(ret);
	}

	/**
	 * Get all ML patients with LOW risk scores and who are alive and on HIV program
	 * @return a list of patients
	 */
	@Override
	public Collection<Integer> getAllPatientsWithLowRiskScores() {
		PatientCalculationService service = Context.getService(PatientCalculationService.class);
		PatientCalculationContext patientCalculationContext = service.createCalculationContext();
		Criteria criteria = getSession().createCriteria(PatientRiskScore.class);

		List<PatientRiskScore> pList = criteria.list();
		HashSet<Integer> hIds = new HashSet<>();

		for (PatientRiskScore patientRiskScore : pList) {
			Patient patient = patientRiskScore.getPatient();
			if(patient != null)	{	
				Integer pId = patient.getPatientId();
				if(!hIds.contains(pId)) {
					hIds.add(pId);
				}
			}
		}
		// Get HIV program
		Program hivProgram = MetadataUtils.existing(Program.class, HivMetadata._Program.HIV);
		// Get all patients who are alive and in HIV program
		Set<Integer> alive = Filters.alive(hIds, patientCalculationContext);
		Set<Integer> inHivProgram = Filters.inProgram(hivProgram, alive, patientCalculationContext);
		
		HashSet<Integer> ret = new HashSet<>();
		for (Integer ptId : inHivProgram) {
			PatientRiskScore latestRiskScore = Context.getService(MLinKenyaEMRService.class)
							.getLatestPatientRiskScoreByPatient(Context.getPatientService().getPatient(ptId));
			if (latestRiskScore != null) {
				String riskGroup = latestRiskScore.getDescription();
				if (riskGroup.trim().equalsIgnoreCase("Low Risk")) {
					ret.add(ptId);
				}				
			}
		}
		
		return(ret);
	}

	/**
	 * Get all ML patients who are alive and on HIV program
	 * @return a list of patients
	 */
	@Override
	public Collection<Integer> getAllPatients() {
		PatientCalculationService service = Context.getService(PatientCalculationService.class);
		PatientCalculationContext patientCalculationContext = service.createCalculationContext();
		Criteria criteria = getSession().createCriteria(PatientRiskScore.class);

		List<PatientRiskScore> pList = criteria.list();
		System.out.println("Major: " + pList.size());
		HashSet<Integer> hIds = new HashSet<>();

		for (PatientRiskScore patientRiskScore : pList) {
			Patient patient = patientRiskScore.getPatient();
			if(patient != null && patientRiskScore.getDescription() != null && (patientRiskScore.getDescription().trim().equalsIgnoreCase("High Risk") || patientRiskScore.getDescription().trim().equalsIgnoreCase("Medium Risk") || patientRiskScore.getDescription().trim().equalsIgnoreCase("Low Risk"))) {
				Integer pId = patient.getPatientId();
				if(!hIds.contains(pId)) {
					hIds.add(pId);
				}
			}
		}
		
		// Get HIV program
		Program hivProgram = MetadataUtils.existing(Program.class, HivMetadata._Program.HIV);
		// Get all patients who are alive and in HIV program
		Set<Integer> alive = Filters.alive(hIds, patientCalculationContext);
		Set<Integer> inHivProgram = Filters.inProgram(hivProgram, alive, patientCalculationContext);
		
		return(inHivProgram);
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

	@Override
	public Date getLatestRiskEvaluationDate() {
		Criteria criteria = getSession().createCriteria(PatientRiskScore.class);
		criteria.addOrder(Order.desc("evaluationDate"));
		criteria.setMaxResults(1);
		PatientRiskScore patientRiskScore = (PatientRiskScore) criteria.uniqueResult();
		return patientRiskScore.getEvaluationDate();

	}
}
