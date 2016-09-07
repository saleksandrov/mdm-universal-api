package com.asv.upload.exception;

/**
 * @author alexandrov
 * @since 07.09.2016
 */
public class TooManyImportTasksException extends RuntimeException {

    public TooManyImportTasksException() {
    }

    public TooManyImportTasksException(String message) {
        super(message);
    }

    public TooManyImportTasksException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooManyImportTasksException(Throwable cause) {
        super(cause);
    }

}
