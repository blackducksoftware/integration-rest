/*
 * integration-rest
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.body;

import java.io.File;
import java.util.Map;

import org.apache.http.HttpEntity;

public class MultipartBodyContent implements BodyContent {
    private final Map<String, File> bodyContentFileMap;
    private final Map<String, String> bodyContentStringMap;

    public MultipartBodyContent(Map<String, File> bodyContentFileMap, Map<String, String> bodyContentStringMap) {
        this.bodyContentStringMap = bodyContentStringMap;
        this.bodyContentFileMap = bodyContentFileMap;
    }

    @Override
    public HttpEntity createEntity(BodyContentConverter bodyContentConverter) {
        return bodyContentConverter.fromMultipart(bodyContentFileMap, bodyContentStringMap);
    }

}
