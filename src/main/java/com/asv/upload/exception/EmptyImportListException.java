package com.asv.upload.exception;

/**
 * @author alexandrov
 * @since 07.09.2016
 */
public class EmptyImportListException extends RuntimeException {

    public EmptyImportListException() {
        super();
    }

    public EmptyImportListException(String message) {
        super(message);
    }

    public EmptyImportListException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyImportListException(Throwable cause) {
        super(cause);
    }

}
