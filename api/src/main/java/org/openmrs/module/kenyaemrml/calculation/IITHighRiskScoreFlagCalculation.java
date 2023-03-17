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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.metadatadeploy.MetadataUtils;

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

		for (Integer ptId : cohort) {
			if (inHivProgram.contains(ptId)) {
				Patient currentPatient = Context.getPatientService().getPatient(ptId);
				PatientRiskScore latestRiskScore = Context.getService(MLinKenyaEMRService.class).getLatestPatientRiskScoreByPatient(currentPatient, false); // We get real time IIT risk score
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
