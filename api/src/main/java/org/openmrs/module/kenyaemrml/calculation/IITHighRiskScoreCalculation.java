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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResult;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.BooleanResult;
import org.openmrs.module.kenyacore.calculation.Filters;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.OnArtCalculation;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.metadatadeploy.MetadataUtils;

/**
 * Calculate whether a patient has high IIT risk score based on data pulled from NDWH
 */
public class IITHighRiskScoreCalculation extends AbstractPatientCalculation {
	
	/**
	 * @see org.openmrs.calculation.patient.PatientCalculation#evaluate(Collection, Map,
	 *      PatientCalculationContext)
	 * @should determine whether a patient has high IIT risk
	 */
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues, PatientCalculationContext context) {
		
		CalculationResultMap ret = new CalculationResultMap();
		// Get HIV program
		Program hivProgram = MetadataUtils.existing(Program.class, HivMetadata._Program.HIV);
		// Get all patients who are alive and in HIV program
		Set<Integer> alive = Filters.alive(cohort, context);
		Set<Integer> inHivProgram = Filters.inProgram(hivProgram, alive, context);

		try {
			for (Integer ptId : inHivProgram) {
				try {
					Patient currentPatient = Context.getPatientService().getPatient(ptId);
					if(currentInArt(currentPatient)) {
						PatientRiskScore latestRiskScore = Context.getService(MLinKenyaEMRService.class)
								.getLatestPatientRiskScoreByPatient(currentPatient, true);
						if (latestRiskScore != null) {
							String riskGroup = latestRiskScore.getDescription();
							if (riskGroup.trim().equalsIgnoreCase("High Risk")) {
								ret.put(ptId, new BooleanResult(true, this, context));
							}
						}
					}
				} catch(Exception em) {
					System.err.println("IIT ML High Risk reporting: " + em.getMessage());
					em.printStackTrace();
				}
			}
		} catch(Exception ex) {
			System.err.println("IIT ML High Risk reporting: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		return ret;
	}

	/**
	 * Checks whether the patient is current on ART
	 * @return true if patient is current on ART
	 *
	 * */

	public Boolean currentInArt(Patient patient) {
		CalculationResult patientCurrentInART = EmrCalculationUtils.evaluateForPatient(OnArtCalculation.class, null, patient);
		return (patientCurrentInART != null ? (Boolean) patientCurrentInART.getValue() : false);

	}
	
}
