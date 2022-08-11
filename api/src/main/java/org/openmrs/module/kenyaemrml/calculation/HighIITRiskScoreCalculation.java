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

import org.openmrs.GlobalProperty;
import org.openmrs.Program;
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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Calculate whether a patient has high IIT risk score based on data pulled from NDWH
 */
public class HighIITRiskScoreCalculation extends AbstractPatientCalculation implements PatientFlagCalculation {
	
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
	 * @should determine whether a patient has high IIT
	 */
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues,
	        PatientCalculationContext context) {
		
		GlobalProperty highRiskThresholdGP = Context.getAdministrationService().getGlobalPropertyObject(
		    "kenyaemrml.palantir.high.iit.risk.threshold");
		
		CalculationResultMap ret = new CalculationResultMap();
		
		if (highRiskThresholdGP != null) {
			String highRiskThresholdVal = highRiskThresholdGP.getPropertyValue();
			
			// Get HIV program
			Program hivProgram = MetadataUtils.existing(Program.class, HivMetadata._Program.HIV);
			double highRiskThreshold = Double.valueOf(highRiskThresholdVal);
			
			// Get all patients who are alive and in HIV program
			Set<Integer> alive = Filters.alive(cohort, context);
			Set<Integer> inHivProgram = Filters.inProgram(hivProgram, alive, context);
			
			for (Integer ptId : cohort) {
				boolean hasHighRiskScore = false;
				
				// check if a patient is alive
				if (inHivProgram.contains(ptId)) {
					PatientRiskScore latestRiskScore = Context.getService(MLinKenyaEMRService.class)
					        .getLatestPatientRiskScoreByPatient(Context.getPatientService().getPatient(ptId));
					if (latestRiskScore != null) {
						double riskScore = latestRiskScore.getRiskScore();
						if (riskScore > highRiskThreshold) {
							hasHighRiskScore = true;
							setCustomMessage("IIT high risk: " + (Math.floor(riskScore * 100)) + "%");
						}
						
					}
					
				}
				ret.put(ptId, new BooleanResult(hasHighRiskScore, this, context));
			}
		}
		return ret;
	}
	
	public String getCustomMessage() {
		return customMessage;
	}
	
	public void setCustomMessage(String customMessage) {
		this.customMessage = customMessage;
	}
}
