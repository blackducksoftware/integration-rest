/*
 * integration-rest
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

public class ObjectBodyContent implements BodyContent {
    private final Object bodyContentObject;
    private final ContentType contentType;

    public ObjectBodyContent(Object bodyContentObject, ContentType contentType) {
        this.bodyContentObject = bodyContentObject;
        this.contentType = contentType;
    }

    @Override
    public HttpEntity createEntity(BodyContentConverter bodyContentConverter) {
        return bodyContentConverter.fromObject(bodyContentObject, contentType);
    }

}
