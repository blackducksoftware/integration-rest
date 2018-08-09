package com.blackducksoftware.integration.rest.body;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.blackducksoftware.integration.rest.request.Request;

public class StringBodyContent implements BodyContent {
    private final String bodyContent;

    public StringBodyContent(final String bodyContent) {
        this.bodyContent = bodyContent;
    }

    @Override
    public HttpEntity createEntity(final Request request) {
        return new StringEntity(getBodyContent(), ContentType.create(request.getMimeType(), request.getBodyEncoding()));
    }

    public String getBodyContent() {
        return bodyContent;
    }
}
