/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrml.reporting.data.converter;

import org.openmrs.api.context.Context;
import org.openmrs.calculation.result.CalculationResult;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.ui.framework.SimpleObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LastRiskScoreCalculationResultConverter implements DataConverter {
	
	private String variable;
	
	public LastRiskScoreCalculationResultConverter(String variable) {
		this.variable = variable;
	}
	
	@Override
	public Object convert(Object obj) {
		
		if (obj == null) {
			return "";
		}
		
		Object value = ((CalculationResult) obj).getValue();
		
		if (value instanceof SimpleResult) {
			SimpleObject valObj = (SimpleObject) ((SimpleResult) value).getValue();
			
			if (variable.equals("lastRiskScore")) {
				return valObj.get(variable);
				
			} else if (variable.equals("evaluationDate")) {
				return valObj.get(variable);
			}
		}
		
		return null;
	}
	
	@Override
	public Class<?> getInputDataType() {
		return CalculationResult.class;
	}
	
	@Override
	public Class<?> getDataType() {
		return String.class;
	}
	
	private String formatDate(Date date) {
		DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
		return date == null ? "" : dateFormatter.format(date);
	}
	
	public String getVariable() {
		return variable;
	}
	
	public void setVariable(String variable) {
		this.variable = variable;
	}
}
