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

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.kenyaemrml.Item;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.api.dao.MLinKenyaEMRDao;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;

import java.util.Date;
import java.util.List;

public class MLinKenyaEMRServiceImpl extends BaseOpenmrsService implements MLinKenyaEMRService {
	
	MLinKenyaEMRDao dao;
	
	UserService userService;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setDao(MLinKenyaEMRDao dao) {
		this.dao = dao;
	}
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public Item getItemByUuid(String uuid) throws APIException {
		return dao.getItemByUuid(uuid);
	}
	
	@Override
	public Item saveItem(Item item) throws APIException {
		if (item.getOwner() == null) {
			item.setOwner(userService.getUser(1));
		}
		
		return dao.saveItem(item);
	}
	
	@Override
	public PatientRiskScore saveOrUpdateRiskScore(PatientRiskScore riskScore) {
		return dao.saveOrUpdateRiskScore(riskScore);
	}
	
	@Override
	public PatientRiskScore getPatientRiskScoreById(Integer id) {
		return dao.getPatientRiskScoreById(id);
	}
	
	@Override
	public PatientRiskScore getLatestPatientRiskScoreByPatient(Patient patient) {
		return dao.getLatestPatientRiskScoreByPatient(patient);
	}
	
	@Override
	public List<PatientRiskScore> getPatientRiskScoreByPatient(Patient patient) {
		return dao.getPatientRiskScoreByPatient(patient);
	}
	
	@Override
	public List<PatientRiskScore> getPatientRiskScoreByPatient(Patient patient, Date onOrBefore, Date onOrAfter) {
		return dao.getPatientRiskScoreByPatient(patient, onOrBefore, onOrAfter);
	}
	
	@Override
	public List<PatientRiskScore> getAllPatientRiskScore() {
		return dao.getAllPatientRiskScore();
	}
}
