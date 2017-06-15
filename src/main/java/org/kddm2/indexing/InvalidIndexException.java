package org.kddm2.indexing;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.SERVICE_UNAVAILABLE, reason = "Invalid lucene index")
public class InvalidIndexException extends Exception {

    public InvalidIndexException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidIndexException(String message) {
        super(message);
    }
}
