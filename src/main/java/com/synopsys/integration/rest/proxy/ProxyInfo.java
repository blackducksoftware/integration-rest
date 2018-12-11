/**
 * integration-rest
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
 * under the License.
 */
package com.synopsys.integration.rest.proxy;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.util.Stringable;

public class ProxyInfo extends Stringable implements Serializable {
    public final static ProxyInfo NO_PROXY_INFO = new NoProxyInfo();

    private static final long serialVersionUID = -7476704373593358472L;

    private final String host;
    private final int port;
    private final Credentials proxyCredentials;
    private final String ntlmDomain;
    private final String ntlmWorkstation;

    public ProxyInfo(final String host, final int port, final Credentials proxyCredentials, final String ntlmDomain, final String ntlmWorkstation) {
        this.host = host;
        this.port = port;
        this.proxyCredentials = proxyCredentials;
        this.ntlmDomain = ntlmDomain;
        this.ntlmWorkstation = ntlmWorkstation;
    }

    public boolean isBlank() {
        boolean isBlank = true;

        isBlank &= StringUtils.isBlank(host);
        isBlank &= port <= 0;
        isBlank &= null == proxyCredentials || proxyCredentials.isBlank();
        isBlank &= StringUtils.isBlank(ntlmDomain);
        isBlank &= StringUtils.isBlank(ntlmWorkstation);

        return isBlank;
    }

    public URLConnection openConnection(final URL url) throws IOException {
        final Proxy proxy = getProxy();
        return url.openConnection(proxy);
    }

    public Proxy getProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    public boolean shouldUseProxy() {
        return !NO_PROXY_INFO.equals(this);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        if (getProxyCredentials() == null) {
            return null;
        } else {
            return getProxyCredentials().getUsername();
        }
    }

    public String getPassword() {
        if (getProxyCredentials() == null) {
            return null;
        } else {
            return getProxyCredentials().getPassword();
        }
    }

    public String getMaskedPassword() {
        if (getProxyCredentials() == null) {
            return null;
        } else {
            return getProxyCredentials().getMaskedPassword();
        }
    }

    public String getNtlmDomain() {
        return ntlmDomain;
    }

    public String getNtlmWorkstation() {
        return ntlmWorkstation;
    }

    public boolean hasAuthenticatedProxySettings() {
        return proxyCredentials != null && StringUtils.isNotBlank(proxyCredentials.getUsername()) && StringUtils.isNotBlank(proxyCredentials.getPassword());
    }

    private Credentials getProxyCredentials() {
        return proxyCredentials;
    }

    private final static class NoProxyInfo extends ProxyInfo {
        private static final long serialVersionUID = 7646573390510702513L;

        public NoProxyInfo() {
            super("", 0, null, null, null);
        }

        @Override
        public Proxy getProxy() {
            return null;
        }
    }

}
