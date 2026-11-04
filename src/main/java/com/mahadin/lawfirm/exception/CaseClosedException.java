package com.mahadin.lawfirm.exception;

public class CaseClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CaseClosedException(String message) {
        super(message);
    }
}
