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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.impl.client.CloseableHttpClient;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class DefaultResponse implements Response {
    private final HttpUriRequest request;
    private final CloseableHttpClient client;
    private final CloseableHttpResponse response;

    public DefaultResponse(HttpUriRequest request, CloseableHttpClient client, CloseableHttpResponse response) {
        this.request = request;
        this.client = client;
        this.response = response;
    }

    @Override
    public HttpUriRequest getRequest() {
        return request;
    }

    @Override
    public int getStatusCode() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public boolean isStatusCodeSuccess() {
        return getStatusCode() >= RestConstants.OK_200 && getStatusCode() < RestConstants.MULT_CHOICE_300;
    }

    @Override
    public boolean isStatusCodeError() {
        return getStatusCode() >= RestConstants.BAD_REQUEST_400;
    }

    @Override
    public String getStatusMessage() {
        return response.getStatusLine().getReasonPhrase();
    }

    @Override
    public InputStream getContent() throws IntegrationException {
        if (response.getEntity() != null) {
            try {
                return response.getEntity().getContent();
            } catch (UnsupportedOperationException | IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        } else {
            return null;
        }
    }

    @Override
    public String getContentString() throws IntegrationException {
        return getContentString(StandardCharsets.UTF_8);
    }

    @Override
    public String getContentString(Charset encoding) throws IntegrationException {
        if (response.getEntity() != null) {
            try (InputStream inputStream = response.getEntity().getContent()) {
                return IOUtils.toString(inputStream, encoding);
            } catch (UnsupportedOperationException | IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        } else {
            return null;
        }
    }

    @Override
    public Long getContentLength() {
        if (response.getEntity() != null) {
            return response.getEntity().getContentLength();
        } else {
            return null;
        }
    }

    @Override
    public String getContentEncoding() {
        if (response.getEntity() != null && response.getEntity().getContentEncoding() != null) {
            return response.getEntity().getContentEncoding().getValue();
        } else {
            return null;
        }
    }

    @Override
    public String getContentType() {
        if (response.getEntity() != null && response.getEntity().getContentType() != null) {
            return response.getEntity().getContentType().getValue();
        } else {
            return null;
        }
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (response.getAllHeaders() != null && response.getAllHeaders().length > 0) {
            for (Header header : response.getAllHeaders()) {
                headers.put(header.getName(), header.getValue());
            }
        }
        return headers;
    }

    @Override
    public String getHeaderValue(String name) {
        if (response.containsHeader(name)) {
            return response.getFirstHeader(name).getValue();
        }
        return null;
    }

    @Override
    public CloseableHttpResponse getActualResponse() {
        return response;
    }

    @Override
    public void close() throws IOException {
        response.close();
        client.close();
    }

    @Override
    public long getLastModified() throws IntegrationException {
        String lastModified = getHeaderValue(Response.LAST_MODIFIED_HEADER_KEY);
        long lastModifiedLong = 0L;

        if (StringUtils.isNotBlank(lastModified)) {
            // Should parse the Date just like URLConnection did
            try {
                SimpleDateFormat format = new SimpleDateFormat(Response.LAST_MODIFIED_FORMAT, Locale.US);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date parsed = format.parse(lastModified);
                lastModifiedLong = parsed.getTime();
            } catch (ParseException e) {
                throw new IntegrationException("Could not parse the last modified date : " + e.getMessage());
            }
        }

        return lastModifiedLong;
    }

    @Override
    public void throwExceptionForError() throws IntegrationRestException {
        if (isStatusCodeError()) {
            int statusCode = getStatusCode();
            String statusMessage = getStatusMessage();
            String httpResponseContent;
            try {
                httpResponseContent = getContentString();
            } catch (IntegrationException e) {
                httpResponseContent = e.getMessage();
            }

            String statusCodeDescription = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, Locale.ENGLISH);

            String reasonPhraseDescription = "";
            if (StringUtils.isNotBlank(statusMessage)) {
                reasonPhraseDescription = String.format(", reason phrase was %s", statusMessage);
            }

            String messageFormat = "There was a problem trying to %s %s, response was %s %s%s.";
            String message = String.format(messageFormat, request.getMethod(), request.getURI().toString(), statusCode, statusCodeDescription, reasonPhraseDescription);
            throw new IntegrationRestException(statusCode, statusMessage, httpResponseContent, message);
        }
    }

}
