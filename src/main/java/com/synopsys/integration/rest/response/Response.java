/**
 * integration-rest
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.rest.response;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public interface Response extends Closeable {
    String LAST_MODIFIED_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    String LAST_MODIFIED_HEADER_KEY = "Last-Modified";

    HttpUriRequest getRequest();

    int getStatusCode();

    boolean isStatusCodeSuccess();

    boolean isStatusCodeError();

    String getStatusMessage();

    InputStream getContent() throws IntegrationException;

    String getContentString() throws IntegrationException;

    String getContentString(Charset encoding) throws IntegrationException;

    Long getContentLength();

    String getContentEncoding();

    String getContentType();

    Map<String, String> getHeaders();

    String getHeaderValue(String name);

    CloseableHttpResponse getActualResponse();

    @Override
    void close() throws IOException;

    long getLastModified() throws IntegrationException;

    void throwExceptionForError() throws IntegrationRestException;
}
