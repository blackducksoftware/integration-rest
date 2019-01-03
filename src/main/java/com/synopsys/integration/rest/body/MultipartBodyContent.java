/**
 * integration-rest
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.rest.body;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;

import com.synopsys.integration.rest.request.Request;

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
            addTextBody(builder, entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public Map<String, String> getBodyContentStringMap() {
        return bodyContentStringMap;
    }

    public Map<String, File> getBodyContentFileMap() {
        return bodyContentFileMap;
    }

    private void addTextBody(final MultipartEntityBuilder builder, final String name, final String value) {
        // should be 'builder.addTextBody(entry.getKey(), entry.getValue());'
        // BUT Black Duck fails to parse form pieces with the header Content-Type
        // So we must remove that header. For more info see https://jira.dc1.lan/browse/IDETECT-514
        final StringBody body = new StringBody(value, ContentType.DEFAULT_TEXT);
        final FormBodyPart part = FormBodyPartBuilder.create(name, body).build();
        part.getHeader().removeFields("Content-Type");
        builder.addPart(part);
    }
}
