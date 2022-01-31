/*
 * integration-rest
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.HttpEntity;

public class MapBodyContent implements BodyContent {
    private final Map<String, String> bodyContentStringMap;
    private final Charset encoding;

    public MapBodyContent(Map<String, String> bodyContentStringMap, Charset encoding) {
        this.bodyContentStringMap = bodyContentStringMap;
        this.encoding = encoding;
    }

    @Override
    public HttpEntity createEntity(BodyContentConverter bodyContentConverter) {
        return bodyContentConverter.fromMap(bodyContentStringMap, encoding);
    }

}
