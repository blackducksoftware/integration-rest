/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import com.synopsys.integration.rest.request.Request;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class StringBodyContent implements BodyContent {
    private final String bodyContent;

    public StringBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    @Override
    public HttpEntity createEntity(Request request) {
        return new StringEntity(getBodyContent(), ContentType.create(request.getAcceptMimeType(), request.getBodyEncoding()));
    }

    public String getBodyContent() {
        return bodyContent;
    }
}
