/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
