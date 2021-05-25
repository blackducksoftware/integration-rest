/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import org.apache.http.HttpEntity;

public class ObjectBodyContent implements BodyContent {
    private final Object bodyContentObject;

    public ObjectBodyContent(Object bodyContentObject) {
        this.bodyContentObject = bodyContentObject;
    }

    @Override
    public HttpEntity createEntity(BodyContentConverter bodyContentConverter) {
        return bodyContentConverter.fromObject(bodyContentObject);
    }

}
