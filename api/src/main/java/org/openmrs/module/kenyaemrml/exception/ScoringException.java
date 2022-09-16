package org.openmrs.module.kenyaemrml.exception;

public class ScoringException extends KeJPMMLEvaluatorException {
	
	private static final long serialVersionUID = -5206681147934496591L;
	
	public ScoringException(String message) {
		super(message);
	}
	
	public ScoringException(String message, Throwable cause) {
		super(message, cause);
	}
}
