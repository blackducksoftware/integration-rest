/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.synopsys.integration.rest.request.Request;

public class MultipartBodyContent implements BodyContent {
    public static final ContentType OCTET_STREAM_UTF_8 = ContentType.APPLICATION_OCTET_STREAM.withCharset(StandardCharsets.UTF_8);
    public static final ContentType TEXT_PLAIN_UTF_8 = ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8);

    private final Map<String, String> bodyContentStringMap;
    private final Map<String, File> bodyContentFileMap;

    public MultipartBodyContent(Map<String, File> bodyContentFileMap, Map<String, String> bodyContentStringMap) {
        this.bodyContentStringMap = bodyContentStringMap;
        this.bodyContentFileMap = bodyContentFileMap;
    }

    @Override
    public HttpEntity createEntity(Request request) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (Entry<String, File> entry : getBodyContentFileMap().entrySet()) {
            builder.addBinaryBody(entry.getKey(), entry.getValue(), OCTET_STREAM_UTF_8, entry.getValue().getName());
        }
        for (Entry<String, String> entry : getBodyContentStringMap().entrySet()) {
            builder.addTextBody(entry.getKey(), entry.getValue(), TEXT_PLAIN_UTF_8);
        }
        builder.setCharset(StandardCharsets.UTF_8);
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        return builder.build();
    }

    public Map<String, String> getBodyContentStringMap() {
        return bodyContentStringMap;
    }

    public Map<String, File> getBodyContentFileMap() {
        return bodyContentFileMap;
    }

}
