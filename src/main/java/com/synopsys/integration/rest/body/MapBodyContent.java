/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
