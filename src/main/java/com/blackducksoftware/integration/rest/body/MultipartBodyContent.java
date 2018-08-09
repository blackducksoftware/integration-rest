/**
 * integration-rest
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.rest.body;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.blackducksoftware.integration.rest.request.Request;

public class MultipartBodyContent implements BodyContent {
    private final Map<String, String> bodyContentStringMap;
    private final Map<String, File> bodyContentFileMap;

    public MultipartBodyContent(final Map<String, File> bodyContentFileMap, final Map<String, String> bodyContentStringMap) {
        this.bodyContentStringMap = bodyContentStringMap;
        this.bodyContentFileMap = bodyContentFileMap;
    }

    @Override
    public HttpEntity createEntity(final Request request) {
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (final Entry<String, File> entry : getBodyContentFileMap().entrySet()) {
            builder.addBinaryBody(entry.getKey(), entry.getValue());
        }
        for (final Entry<String, String> entry : getBodyContentStringMap().entrySet()) {
            builder.addTextBody(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public Map<String, String> getBodyContentStringMap() {
        return bodyContentStringMap;
    }

    public Map<String, File> getBodyContentFileMap() {
        return bodyContentFileMap;
    }
}
