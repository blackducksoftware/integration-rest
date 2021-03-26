/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
