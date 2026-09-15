package com.marcosperboni.integrationbff.domain.exception;

public class DownstreamUnavailableException extends RuntimeException {

    public DownstreamUnavailableException(String downstreamName, Throwable cause) {
        super(downstreamName + " is currently unavailable", cause);
    }
}
