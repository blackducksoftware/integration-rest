/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.proxy;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.builder.BuilderStatus;
import com.blackduck.integration.builder.IntegrationBuilder;
import com.blackduck.integration.rest.credentials.Credentials;

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
