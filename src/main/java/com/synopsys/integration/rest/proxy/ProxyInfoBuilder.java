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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.util.BuilderStatus;
import com.synopsys.integration.util.IntegrationBuilder;

public class ProxyInfoBuilder extends IntegrationBuilder<ProxyInfo> {
    private String host;
    private int port;
    private Credentials credentials;
    private String ignoredProxyHosts;
    private String ntlmDomain;
    private String ntlmWorkstation;

    @Override
    protected ProxyInfo buildWithoutValidation() {
        final ProxyInfo proxyInfo = new ProxyInfo(host, port, credentials, ignoredProxyHosts, ntlmDomain, ntlmWorkstation);
        return proxyInfo;
    }

    @Override
    protected void validate(final BuilderStatus builderStatus) {
        if (hasProxySettings()) {
            if (StringUtils.isBlank(host) || port <= 0) {
                builderStatus.addErrorMessage("Both the proxy host and port greater than zero must be specified.");
            }

            if (!StringUtils.isAllBlank(ntlmDomain, ntlmWorkstation) && (null == credentials || credentials.isBlank())) {
                builderStatus.addErrorMessage("Proxy username and password must be set for the NTLM proxy.");
            }

            if (StringUtils.isNotBlank(ignoredProxyHosts)) {
                try {
                    if (ignoredProxyHosts.contains(",")) {
                        String[] ignoreHosts = null;
                        ignoreHosts = ignoredProxyHosts.split(",");
                        for (final String ignoreHost : ignoreHosts) {
                            Pattern.compile(ignoreHost.trim());
                        }
                    } else {
                        Pattern.compile(ignoredProxyHosts);
                    }
                } catch (final PatternSyntaxException ex) {
                    builderStatus.addErrorMessage("Proxy ignore hosts does not compile to a valid regular expression.");
                }
            }
        }
    }

    private boolean hasProxySettings() {
        return StringUtils.isNotBlank(host) || 0 != port || (null != credentials && !credentials.isBlank()) || StringUtils.isNotBlank(ignoredProxyHosts)
                       || StringUtils.isNotBlank(ntlmDomain) || StringUtils.isNotBlank(ntlmWorkstation);
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(final Credentials credentials) {
        this.credentials = credentials;
    }

    public String getIgnoredProxyHosts() {
        return ignoredProxyHosts;
    }

    public void setIgnoredProxyHosts(final String ignoredProxyHosts) {
        this.ignoredProxyHosts = ignoredProxyHosts;
    }

    public String getNtlmDomain() {
        return ntlmDomain;
    }

    public void setNtlmDomain(final String ntlmDomain) {
        this.ntlmDomain = ntlmDomain;
    }

    public String getNtlmWorkstation() {
        return ntlmWorkstation;
    }

    public void setNtlmWorkstation(final String ntlmWorkstation) {
        this.ntlmWorkstation = ntlmWorkstation;
    }

}
