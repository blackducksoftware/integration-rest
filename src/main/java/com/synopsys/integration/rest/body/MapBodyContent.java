/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import java.util.Map;

import org.apache.http.HttpEntity;

public class MapBodyContent implements BodyContent {
    private final Map<String, String> bodyContentStringMap;

    public MapBodyContent(Map<String, String> bodyContentStringMap) {
        this.bodyContentStringMap = bodyContentStringMap;
    }

    @Override
    public HttpEntity createEntity(BodyContentConverter bodyContentConverter) {
        return bodyContentConverter.fromMap(bodyContentStringMap);
    }

}
