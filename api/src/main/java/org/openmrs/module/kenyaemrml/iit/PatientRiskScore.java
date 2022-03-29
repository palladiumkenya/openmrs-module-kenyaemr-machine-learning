/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.kenyaemrml.iit;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Patient;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * A model for IIT - interruption in treatment score for a patient IIT evaluates the possibility of
 * a patient to have interruption in ARV treatment based on a number of factors It is a model class.
 * It should extend either {@link BaseOpenmrsObject} or {@link BaseOpenmrsMetadata}.
 */
public class PatientRiskScore extends BaseOpenmrsData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
	private Patient patient;
	
	private String sourceSystemUuid;
	
	private Double riskScore;
	
	private Date evaluationDate;
	
	@Override
	public Integer getId() {
		return id;
	}
	
	@Override
	public void setId(final Integer id) {
		this.id = id;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public String getSourceSystemUuid() {
		return sourceSystemUuid;
	}
	
	public void setSourceSystemUuid(String sourceSystemUuid) {
		this.sourceSystemUuid = sourceSystemUuid;
	}
	
	public Double getRiskScore() {
		return riskScore;
	}
	
	public void setRiskScore(Double riskScore) {
		this.riskScore = riskScore;
	}
	
	public Date getEvaluationDate() {
		return evaluationDate;
	}
	
	public void setEvaluationDate(Date evaluationDate) {
		this.evaluationDate = evaluationDate;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		PatientRiskScore that = (PatientRiskScore) o;
		return id.equals(that.id) && patient.equals(that.patient) && sourceSystemUuid.equals(that.sourceSystemUuid)
		        && riskScore.equals(that.riskScore) && evaluationDate.equals(that.evaluationDate);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id, patient, sourceSystemUuid, riskScore, evaluationDate);
	}
}
