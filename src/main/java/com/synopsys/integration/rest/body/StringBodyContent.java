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

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.synopsys.integration.rest.request.Request;

public class StringBodyContent implements BodyContent {
    private final String bodyContent;

    public StringBodyContent(final String bodyContent) {
        this.bodyContent = bodyContent;
    }

    @Override
    public HttpEntity createEntity(final Request request) {
        return new StringEntity(getBodyContent(), ContentType.create(request.getMimeType(), request.getBodyEncoding()));
    }

    public String getBodyContent() {
        return bodyContent;
    }
}
