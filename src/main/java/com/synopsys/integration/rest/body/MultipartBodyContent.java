/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.synopsys.integration.rest.request.Request;

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
