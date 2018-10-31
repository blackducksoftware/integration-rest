/**
 * Hub Common Rest
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
 * under the License.*/
package com.synopsys.integration.rest

import com.synopsys.integration.rest.credentials.Credentials
import com.synopsys.integration.rest.proxy.ProxyInfo
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder
import org.junit.Test

class ProxyInfoBuilderTest {
    @Test
    public void testBuilder() {
        Credentials credentials = new Credentials("username", "password");
        String proxyHost = "proxyHost"
        int proxyPort = 25
        String proxyIgnoredHosts = ".*"
        String ntlmDomain = 'domain'
        String ntlmWorkstation = 'workstation'

        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort
        builder.credentials = credentials;
        builder.ignoredProxyHosts = proxyIgnoredHosts
        builder.ntlmDomain = ntlmDomain
        builder.ntlmWorkstation = ntlmWorkstation

        ProxyInfo proxyInfo1 = builder.build()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port
        assert proxyIgnoredHosts == proxyInfo1.ignoredProxyHosts

        assert ntlmDomain == proxyInfo1.ntlmDomain
        assert ntlmWorkstation == proxyInfo1.ntlmWorkstation
    }

    @Test
    public void testUnauthenticatedBuilder() {
        String proxyHost = "proxyHost"
        int proxyPort = 25

        String proxyIgnoredHosts = ".*"
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort
        builder.ignoredProxyHosts = proxyIgnoredHosts

        ProxyInfo proxyInfo1 = builder.build()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port
        assert proxyIgnoredHosts == proxyInfo1.ignoredProxyHosts

        assert null == proxyInfo1.ntlmDomain
        assert null == proxyInfo1.ntlmWorkstation
    }

    @Test
    public void testProxyValid() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        assert !builder.hasProxySettings()
        assert builder.isValid()

        builder.host = "proxyhost"
        builder.port = 25
        assert builder.hasProxySettings()
        assert builder.isValid()

        builder.host = "proxyhost"
        builder.port = 25
        builder.credentials = new Credentials("proxyUser", "proxyPassword");
        builder.ignoredProxyHosts = ".*"
        builder.ntlmDomain = "domain"
        builder.ntlmWorkstation = "workstation"

        assert builder.hasProxySettings()
        assert builder.isValid()
    }

    @Test
    public void testInvalidPort() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = -1
        assert builder.hasProxySettings()
        assert !builder.isValid()

        builder = new ProxyInfoBuilder()
        builder.host = ""
        builder.port = 25
        assert builder.hasProxySettings()
        assert !builder.isValid()

        builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 0
        assert builder.hasProxySettings()
        assert !builder.isValid()
    }

    @Test
    public void testValidCredentials() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 25
        builder.credentials = new Credentials("proxyUser", "proxyPassword")
        assert builder.hasProxySettings()
        assert builder.isValid()
    }

    @Test
    public void testInvalidCredentials() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = ""
        builder.port = 25
        builder.credentials = new Credentials("proxyUser", "proxyPassword")
        builder.ignoredProxyHosts = ".*"
        assert builder.hasProxySettings()
        assert !builder.isValid()

        builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 25
        builder.ntlmDomain = "domain"
        builder.ntlmWorkstation = "workstation"
        assert builder.hasProxySettings()
        assert !builder.isValid()

        builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 25
        builder.ntlmDomain = 'domain'
        assert builder.hasProxySettings()
        assert !builder.isValid()

        builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 25
        builder.ntlmWorkstation = "workstation"
        assert builder.hasProxySettings()
        assert !builder.isValid()
    }

    @Test
    public void testIgnoredHostValid() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 25
        builder.ignoredProxyHosts = ".*,.*"
        assert builder.hasProxySettings()
        assert builder.isValid()
    }

    @Test
    public void testIgnoredHostInvalid() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 25
        builder.credentials = new Credentials("proxyUser", "proxyPassword");
        builder.ignoredProxyHosts = ".asdfajdflkjaf{ ])(faslkfj,{][[)("
        assert builder.hasProxySettings()
        assert !builder.isValid()

        builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 25
        builder.ignoredProxyHosts = ".asdfajdflkjaf{ ])(faslkfj"
        assert builder.hasProxySettings()
        assert !builder.isValid()
    }

}
