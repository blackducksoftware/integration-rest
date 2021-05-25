/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

public class StringBodyContent implements BodyContent {
    private final String bodyContentString;
    private final ContentType contentType;

    public StringBodyContent(String bodyContentString, ContentType contentType) {
        this.bodyContentString = bodyContentString;
        this.contentType = contentType;
    }

    @Override
    public HttpEntity createEntity(BodyContentConverter bodyContentConverter) {
        return bodyContentConverter.fromString(bodyContentString, contentType);
    }

}
