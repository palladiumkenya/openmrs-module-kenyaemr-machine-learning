/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrml.calculation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Program;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.BooleanResult;
import org.openmrs.module.kenyacore.calculation.Filters;
import org.openmrs.module.kenyacore.calculation.PatientFlagCalculation;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;

/**
 * Calculate whether a patient has high IIT risk score based on data pulled from NDWH
 */
public class IITHighRiskScoreFlagCalculation extends AbstractPatientCalculation implements PatientFlagCalculation {
	
	private String customMessage;
	
	/**
	 * @see PatientFlagCalculation#getFlagMessage()
	 */
	@Override
	public String getFlagMessage() {
		return customMessage;
	}
	
	/**
	 * @see org.openmrs.calculation.patient.PatientCalculation#evaluate(Collection, Map,
	 *      PatientCalculationContext)
	 * @should determine whether a patient has high IIT risk
	 */
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues, PatientCalculationContext context) {
		
		Program hivProgram = MetadataUtils.existing(Program.class, HivMetadata._Program.HIV);
		Set<Integer> alive = Filters.alive(cohort, context);
		Set<Integer> inHivProgram = Filters.inProgram(hivProgram, alive, context);

		CalculationResultMap ret = new CalculationResultMap();

		try {
			for (Integer ptId : cohort) {
				try {
					if (inHivProgram.contains(ptId)) {
						Patient currentPatient = Context.getPatientService().getPatient(ptId);
						PatientRiskScore latestRiskScore = null;
						List<Visit> visits = Context.getVisitService().getActiveVisitsByPatient(currentPatient);
						// Check if we are currently checked in
						if(visits.size() > 0) {
							//check if we have a saved score
							Date lastScore = Context.getService(MLinKenyaEMRService.class).getPatientLatestRiskEvaluationDate(currentPatient);
							if(lastScore != null) {
								//check if a green card has been filled since the last score
								Form hivGreenCardForm = MetadataUtils.existing(Form.class, HivMetadata._Form.HIV_GREEN_CARD);
								List<Form> hivCareForms = Arrays.asList(hivGreenCardForm);
								Location defaultLocation = Context.getService(KenyaEmrService.class).getDefaultLocation();
								EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder()
											.setIncludeVoided(false)
											.setFromDate(lastScore)
											// .setToDate(new Date())
											.setPatient(currentPatient)
											.setEnteredViaForms(hivCareForms)
											.setLocation(defaultLocation)
											.createEncounterSearchCriteria();
								List<Encounter> hivCareEncounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
								if(hivCareEncounters.size() > 0) {
									// We have had a greencard form filled after the last encounter, we can now generate a new score. NB: Greencard save should have triggered score generation
									latestRiskScore = Context.getService(MLinKenyaEMRService.class).getLatestPatientRiskScoreByPatientRealTime(currentPatient);
								} else {
									latestRiskScore = Context.getService(MLinKenyaEMRService.class).getLatestPatientRiskScoreByPatient(currentPatient);
								}
							}
						} else {
							latestRiskScore = Context.getService(MLinKenyaEMRService.class).getLatestPatientRiskScoreByPatient(currentPatient);
						}
						if (latestRiskScore != null) {
							String riskGroup = latestRiskScore.getDescription();
							if (riskGroup.trim().equalsIgnoreCase("High Risk")) {					
								setCustomMessage("IIT High risk");
								ret.put(ptId, new BooleanResult(true, this, context));
							} else {
								ret.put(ptId, new BooleanResult(false, this, context));
							}
						}
					}
				} catch(Exception em) {
					// System.err.println("IIT ML: " + em.getMessage());
					em.printStackTrace();
				}
			}
		} catch(Exception ex) {
			// System.err.println("IIT ML: " + ex.getMessage());
			ex.printStackTrace();
		}

		return ret;
	}

	/**
	 * Get the last visit date
	 */
	private Date getLastVisitDate(List<Visit> allVisits) {
		Date latestDate = null;
		List<Date> visitDates = new ArrayList<Date>();
		for(Visit visit:allVisits) {
			visitDates.add(visit.getStartDatetime());
		}
		latestDate = Collections.max(visitDates);
		return(latestDate);
	}
	
	public String getCustomMessage() {
		return customMessage;
	}
	
	public void setCustomMessage(String customMessage) {
		this.customMessage = customMessage;
	}
}
