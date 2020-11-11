package com.synopsys.integration.rest.body;

import org.apache.http.HttpEntity;

import com.synopsys.integration.rest.request.Request;

public class EntityBodyContent implements BodyContent {
    private final HttpEntity httpEntity;

    public EntityBodyContent(final HttpEntity httpEntity) {
        this.httpEntity = httpEntity;
    }

    @Override
    public HttpEntity createEntity(final Request request) {
        return httpEntity;
    }

}
