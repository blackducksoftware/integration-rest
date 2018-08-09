package com.blackducksoftware.integration.rest.body;

import org.apache.http.HttpEntity;

import com.blackducksoftware.integration.rest.request.Request;

public interface BodyContent {
    HttpEntity createEntity(final Request request);
}
