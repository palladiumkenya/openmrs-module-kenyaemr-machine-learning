/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrml.fragment.controller.iitRiskScore;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.openmrs.Visit;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Controller for getting a history of risk score and grouped by the date of evaluation
 */
public class IitPatientRiskScoreFragmentController {

    private static final Logger log = LoggerFactory.getLogger(IitPatientRiskScoreFragmentController.class);

    public void controller(@RequestParam("patientId") Patient patient, PageModel model, UiUtils ui) {
        //Pick latest Patient risk score,evaluationDate and Description
        Date evaluationDate = null;
        String description = null;
        String riskFactor = null;
        long dateDiff = 100;
        Boolean checkVisit = false;

        PatientRiskScore latestRiskScore = Context.getService(MLinKenyaEMRService.class)
                .getLatestPatientRiskScoreByPatient(Context.getPatientService().getPatient(patient.getPatientId()));
        if (latestRiskScore != null) {
            evaluationDate = latestRiskScore.getEvaluationDate();
            description = latestRiskScore.getDescription();
            riskFactor = latestRiskScore.getRiskFactors();

            Date currentDate = new Date();
            // Ensure the evaluation date is less than a month ago (30 days)
            long diffInMillies = Math.abs(currentDate.getTime() - evaluationDate.getTime());
            dateDiff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            // Check if last visit is after evaluation date
            List<Visit> allVisits = Context.getVisitService().getVisitsByPatient(patient);
            Date latestVisitDate = getLastVisitDate(allVisits);
            checkVisit = latestVisitDate.after(evaluationDate);
        }
        KenyaUiUtils kenyaui = Context.getRegisteredComponents(KenyaUiUtils.class).get(0);

        if(dateDiff <= 30 && !checkVisit) {
            model.put("riskScore", latestRiskScore != null ? latestRiskScore.getRiskScore() : "-");
            model.put("evaluationDate", evaluationDate != null ? kenyaui.formatDate(evaluationDate) : "-");
            model.put("description", description != null ? description : "-");
            model.put("riskFactor", riskFactor != null ? riskFactor : "-");
        } else {
            model.put("riskScore", "-");
            model.put("evaluationDate", "-");
            model.put("description", "-");
            model.put("riskFactor", "-");
        }
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
}
