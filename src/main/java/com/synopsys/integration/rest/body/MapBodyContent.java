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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.synopsys.integration.rest.request.Request;

public class MapBodyContent implements BodyContent {
    private final Map<String, String> bodyContentMap;

    public MapBodyContent(final Map<String, String> bodyContentMap) {
        this.bodyContentMap = bodyContentMap;
    }

    public Map<String, String> getBodyContentMap() {
        return bodyContentMap;
    }

    @Override
    public HttpEntity createEntity(final Request request) {
        final List<NameValuePair> parameters = new ArrayList<>();
        for (final Entry<String, String> entry : getBodyContentMap().entrySet()) {
            final NameValuePair nameValuePair = new BasicNameValuePair(entry.getKey(), entry.getValue());
            parameters.add(nameValuePair);
        }
        return new UrlEncodedFormEntity(parameters, request.getBodyEncoding());
    }
}
