/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.util.Stringable;

public class HttpUrl extends Stringable {
    private String urlString;
    private URI uri;
    private URL url;

    public static Optional<HttpUrl> createSafely(String url) {
        try {
            return Optional.of(new HttpUrl(url));
        } catch (IntegrationException ignored) {
            return Optional.empty();
        }
    }

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
