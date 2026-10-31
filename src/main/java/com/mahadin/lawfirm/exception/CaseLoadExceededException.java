package com.mahadin.lawfirm.exception;

public class CaseLoadExceededException extends Exception {

    private static final long serialVersionUID = 1L;

    public CaseLoadExceededException(String message) {
        super(message);
    }
}
