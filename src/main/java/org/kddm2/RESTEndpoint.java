package org.kddm2;

import java.util.Set;

public class RESTEndpoint {
    public final Set<String> urlPatterns;
    public final Set<String> methods;

    public RESTEndpoint(Set<String> urlPatterns, Set<String> methods) {
        this.urlPatterns = urlPatterns;
        this.methods = methods;
    }
}
