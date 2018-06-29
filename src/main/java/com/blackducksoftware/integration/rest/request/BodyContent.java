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
package com.blackducksoftware.integration.rest.request;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

/**
 * Only one of the body content fields should be set at any one time.
 */
public class BodyContent {
    private final File bodyContentFile;
    private final Map<String, String> bodyContentMap;
    private final String bodyContent;

    public BodyContent(final File bodyContentFile) {
        this.bodyContentFile = bodyContentFile;
        this.bodyContentMap = null;
        this.bodyContent = null;
    }

    public BodyContent(final Map<String, String> bodyContentMap) {
        this.bodyContentFile = null;
        this.bodyContentMap = bodyContentMap;
        this.bodyContent = null;
    }

    public BodyContent(final String bodyContent) {
        this.bodyContentFile = null;
        this.bodyContentMap = null;
        this.bodyContent = bodyContent;
    }

    public BodyContentType getBodyContentType() {
        if (bodyContentFile != null) {
            return BodyContentType.FILE;
        } else if (bodyContentMap != null && !bodyContentMap.isEmpty()) {
            return BodyContentType.MAP;
        } else if (StringUtils.isNotBlank(bodyContent)) {
            return BodyContentType.STRING;
        } else {
            return null;
        }
    }

    public HttpEntity createEntity(final Request request) {
        final BodyContentType bodyContentType = getBodyContentType();

        if (BodyContentType.FILE == bodyContentType) {
            return new FileEntity(getBodyContentFile(), ContentType.create(request.getMimeType(), request.getBodyEncoding()));
        } else if (BodyContentType.MAP == getBodyContentType()) {
            final List<NameValuePair> parameters = new ArrayList<>();
            for (final Entry<String, String> entry : getBodyContentMap().entrySet()) {
                final NameValuePair nameValuePair = new BasicNameValuePair(entry.getKey(), entry.getValue());
                parameters.add(nameValuePair);
            }
            return new UrlEncodedFormEntity(parameters, request.getBodyEncoding());
        } else if (BodyContentType.STRING == bodyContentType) {
            return new StringEntity(getBodyContent(), ContentType.create(request.getMimeType(), request.getBodyEncoding()));
        }

        return null;
    }

    public File getBodyContentFile() {
        return bodyContentFile;
    }

    public Map<String, String> getBodyContentMap() {
        return bodyContentMap;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public static enum BodyContentType {
        FILE,
        MAP,
        STRING,
    }

}
