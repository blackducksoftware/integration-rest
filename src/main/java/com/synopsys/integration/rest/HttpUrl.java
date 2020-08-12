/**
 * integration-rest
 * <p>
 * Copyright (c) 2020 Synopsys, Inc.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.rest;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.Stringable;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class HttpUrl extends Stringable {
    private String urlString;
    private URI uri;
    private URL url;

    public HttpUrl(String url) throws IntegrationException {
        populateUrl(url);
    }

    public HttpUrl(URL url) throws IntegrationException {
        populateUrl(url.toString());
    }

    public HttpUrl(URI uri) throws IntegrationException {
        populateUrl(uri.toString());
    }

    public HttpUrl appendRelativeUrl(String relativeUrl) throws IntegrationException {
        String baseUrl = urlString;
        try {
            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }
            URL baseURL = new URL(baseUrl);
            if (relativeUrl.startsWith("/")) {
                relativeUrl = relativeUrl.substring(1);
            }

            return new HttpUrl(new URL(baseURL, relativeUrl).toString());
        } catch (MalformedURLException e) {
            throw new IntegrationException(String.format("Error appending the relative url (%s) to base url (%s): %s", relativeUrl, baseUrl, e.getMessage()), e);
        }
    }

    private void populateUrl(String url) throws IntegrationException {
        if (StringUtils.isBlank(url)) {
            throw new IntegrationException("The url must not be blank.");
        }
        this.urlString = url;
        try {
            this.uri = new URI(url);
            this.url = uri.toURL();
        } catch (IllegalArgumentException | URISyntaxException | MalformedURLException e) {
            throw new IntegrationException(String.format("The url (%s) is not valid: %s", url, e.getMessage()), e);
        }
    }

    public String string() {
        return urlString;
    }

    public URL url() {
        return url;
    }

    public URI uri() {
        return uri;
    }

    @Override
    public String toString() {
        return urlString;
    }

}
