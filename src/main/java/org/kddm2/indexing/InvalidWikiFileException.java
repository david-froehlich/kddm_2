package org.kddm2.indexing;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE, reason = "Invalid wiki XML file")
public class InvalidWikiFileException extends Throwable {
    public InvalidWikiFileException(String message) {
        super(message);
    }

    public InvalidWikiFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
