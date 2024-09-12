/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.body;

import org.apache.http.HttpEntity;

public class EntityBodyContent implements BodyContent {
    private final HttpEntity httpEntity;

    public EntityBodyContent(HttpEntity httpEntity) {
        this.httpEntity = httpEntity;
    }

    @Override
    public HttpEntity createEntity(BodyContentConverter bodyContentConverter) {
        return bodyContentConverter.fromHttpEntity(httpEntity);
    }

}
