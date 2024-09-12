/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.response;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.exception.IntegrationRestException;

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
