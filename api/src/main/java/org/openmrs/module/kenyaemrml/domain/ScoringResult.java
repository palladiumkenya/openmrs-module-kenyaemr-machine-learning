package org.openmrs.module.kenyaemrml.domain;

import java.util.Map;

public class ScoringResult {
	
	private static final long serialVersionUID = 8087811708391221715L;
	
	private Map<String, Object> result;
	
	public ScoringResult() {
	}
	
	public ScoringResult(Map<String, Object> result) {
		this.result = result;
	}
	
	public Map<String, Object> getResult() {
		return result;
	}
	
	public void setResult(Map<String, Object> result) {
		this.result = result;
	}
}
