/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

import com.blackduck.integration.builder.Buildable;
import com.blackduck.integration.rest.credentials.Credentials;
import com.blackduck.integration.util.Stringable;

public class ProxyInfo extends Stringable implements Buildable {
    public final static ProxyInfo NO_PROXY_INFO = new ProxyInfo();

    public static ProxyInfoBuilder newBuilder() {
        return new ProxyInfoBuilder();
    }

    private final String host;
    private final int port;
    private final Credentials proxyCredentials;
    private final String ntlmDomain;
    private final String ntlmWorkstation;
    private final boolean blank;

    ProxyInfo(final String host, final int port, final Credentials proxyCredentials, final String ntlmDomain, final String ntlmWorkstation) {
        this.host = host;
        this.port = port;
        this.proxyCredentials = proxyCredentials;
        this.ntlmDomain = ntlmDomain;
        this.ntlmWorkstation = ntlmWorkstation;
        blank = false;
    }

    ProxyInfo() {
        host = null;
        port = 0;
        proxyCredentials = null;
        ntlmDomain = null;
        ntlmWorkstation = null;
        blank = true;
    }

    public boolean isBlank() {
        return blank;
    }

    public boolean shouldUseProxy() {
        return !isBlank();
    }

    public Optional<URLConnection> openConnection(final URL url) throws IOException {
        if (getProxy().isPresent()) {
            return Optional.of(url.openConnection(getProxy().get()));
        }
        return Optional.empty();
    }

    public Optional<Proxy> getProxy() {
        if (isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
    }

    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    public int getPort() {
        return port;
    }

    public Optional<String> getUsername() {
        if (getProxyCredentials().isPresent()) {
            return getProxyCredentials().get().getUsername();
        }
        return Optional.empty();
    }

    public Optional<String> getPassword() {
        if (getProxyCredentials().isPresent()) {
            return getProxyCredentials().get().getPassword();
        }
        return Optional.empty();
    }

    public Optional<String> getMaskedPassword() {
        if (getProxyCredentials().isPresent()) {
            return Optional.of(getProxyCredentials().get().getMaskedPassword());
        }
        return Optional.empty();
    }

    public Optional<String> getNtlmDomain() {
        return Optional.ofNullable(ntlmDomain);
    }

    public Optional<String> getNtlmWorkstation() {
        return Optional.ofNullable(ntlmWorkstation);
    }

    public boolean hasAuthenticatedProxySettings() {
        return null != proxyCredentials && !proxyCredentials.isBlank();
    }

    public Optional<Credentials> getProxyCredentials() {
        return Optional.ofNullable(proxyCredentials);
    }

}
