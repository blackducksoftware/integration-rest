/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.body;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

public class StringBodyContent implements BodyContent {
    private final String bodyContentString;
    private final ContentType contentType;

    public static final StringBodyContent json(String bodyContentString) {
        return new StringBodyContent(bodyContentString, BodyContent.JSON_UTF_8);
    }

    public StringBodyContent(String bodyContentString, ContentType contentType) {
        this.bodyContentString = bodyContentString;
        this.contentType = contentType;
    }

    @Override
    public HttpEntity createEntity(BodyContentConverter bodyContentConverter) {
        return bodyContentConverter.fromString(bodyContentString, contentType);
    }

}
