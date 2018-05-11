/**
 * Hub Common Rest
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
 * under the License.*/
package com.blackducksoftware.integration.rest

import com.blackducksoftware.integration.rest.request.Request
import org.apache.commons.codec.Charsets
import org.apache.http.entity.ContentType
import org.junit.Test

import java.nio.charset.Charset

class RequestTest {
    @Test
    public void testRequest() {
        String uri = 'URI'
        Map<String, String> queryParametes = [test: "one", query: "two"]
        String q = 'q'
        HttpMethod method = HttpMethod.DELETE
        String mimeType = 'mime'
        Charset bodyEncoding = Charsets.UTF_8
        Map<String, String> additionalHeaders = [header: "one", thing: "two"]

        Request request = new Request(new Request.Builder())
        assert HttpMethod.GET == request.method
        assert Charsets.UTF_8 == request.bodyEncoding
        assert ContentType.APPLICATION_JSON.getMimeType() == request.mimeType
        assert null == request.uri
        assert null == request.additionalHeaders
        assert request.getPopulatedQueryParameters().isEmpty()

        request = new Request(new Request.Builder(uri))
        assert HttpMethod.GET == request.method
        assert Charsets.UTF_8 == request.bodyEncoding
        assert ContentType.APPLICATION_JSON.getMimeType() == request.mimeType
        assert uri == request.uri
        assert null == request.additionalHeaders
        assert request.getPopulatedQueryParameters().isEmpty()

        request = new Request(null, null, null, null, null, null, null)
        assert null == request.method
        assert null == request.bodyEncoding
        assert null == request.mimeType
        assert null == request.uri
        assert null == request.additionalHeaders
        assert request.getPopulatedQueryParameters().isEmpty()
    }
}
