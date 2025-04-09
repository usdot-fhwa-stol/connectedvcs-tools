package gov.usdot.cv.service.rest;

public class SemiValidatorException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public SemiValidatorException(String message) {
		super(message);
	}
	
	public SemiValidatorException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SemiValidatorException(Throwable cause) {
		super(cause);
	}
}
