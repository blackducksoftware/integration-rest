/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.builder.IntegrationBuilder;
import com.synopsys.integration.rest.credentials.Credentials;

public class ProxyInfoBuilder extends IntegrationBuilder<ProxyInfo> {
    private String host;
    private int port;
    private Credentials credentials;
    private String ntlmDomain;
    private String ntlmWorkstation;

    @Override
    protected ProxyInfo buildWithoutValidation() {
        if (isBlank()) {
            return ProxyInfo.NO_PROXY_INFO;
        } else {
            return new ProxyInfo(host, port, credentials, ntlmDomain, ntlmWorkstation);
        }
    }

    @Override
    protected void validate(final BuilderStatus builderStatus) {
        final ProxyInfo tempInfo = buildWithoutValidation();
        if (!tempInfo.isBlank()) {
            if (StringUtils.isBlank(host) || port <= 0) {
                builderStatus.addErrorMessage("The proxy host must be specified and the port must be greater than zero.");
            }

            if (!StringUtils.isAllBlank(ntlmDomain, ntlmWorkstation) && (null == credentials || credentials.isBlank())) {
                builderStatus.addErrorMessage("Proxy username and password must be set for the NTLM proxy.");
            }
        }
    }

    public boolean isBlank() {
        boolean isBlank = true;
        isBlank &= StringUtils.isBlank(host);
        isBlank &= port <= 0;
        isBlank &= null == credentials || credentials.isBlank();
        isBlank &= StringUtils.isBlank(ntlmDomain);
        isBlank &= StringUtils.isBlank(ntlmWorkstation);
        return isBlank;
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
