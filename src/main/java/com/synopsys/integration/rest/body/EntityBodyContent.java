/*
 * integration-rest
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import org.apache.http.HttpEntity;

import com.synopsys.integration.rest.request.Request;

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
