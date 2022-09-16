package org.openmrs.module.kenyaemrml.domain;

import java.util.Map;

public class ModelInputFields {
	
	private static final long serialVersionUID = 9053732207572116071L;
	
	private Map<String, Object> fields;
	
	public Map<String, Object> getFields() {
		return fields;
	}
	
	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}
}
