package org.openmrs.module.kenyaemrml.exception;

public class EvaluatorCreationException extends KeJPMMLEvaluatorException {
	
	private static final long serialVersionUID = 7958127205129073697L;
	
	public EvaluatorCreationException(String message) {
		super(message);
	}
	
	public EvaluatorCreationException(String message, Throwable cause) {
		super(message, cause);
	}
}
