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

import com.synopsys.integration.encryption.PasswordEncrypter
import com.synopsys.integration.rest.proxy.ProxyInfo
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder
import org.apache.commons.lang3.StringUtils
import org.junit.Test

class ProxyInfoBuilderTest {

    @Test
    public void testBuilder() {
        String username = "username"
        String password = "password"
        String proxyHost = "proxyHost"
        int proxyPort = 25
        String proxyIgnoredHosts = ".*"
        String ntlmDomain = 'domain'
        String ntlmWorkstation = 'workstation'

        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort
        builder.username = username
        builder.password = password
        builder.ignoredProxyHosts = proxyIgnoredHosts
        builder.ntlmDomain = ntlmDomain
        builder.ntlmWorkstation = ntlmWorkstation

        ProxyInfo proxyInfo1 = builder.build()
        String maskedPassword = proxyInfo1.getMaskedPassword()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port
        assert proxyIgnoredHosts == proxyInfo1.ignoredProxyHosts

        assert ntlmDomain == proxyInfo1.ntlmDomain
        assert ntlmWorkstation == proxyInfo1.ntlmWorkstation

        assert maskedPassword.length() == password.length()
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")
    }

    @Test
    public void testEncryptedPasswordBuilder() {
        String username = "username"
        String password = "password"
        String proxyHost = "proxyHost"
        int proxyPort = 25
        String proxyIgnoredHosts = ".*"
        String ntlmDomain = 'domain'
        String ntlmWorkstation = 'workstation'

        String encryptedPassword = PasswordEncrypter.encrypt(password)
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort
        builder.username = username
        builder.password = encryptedPassword
        builder.passwordLength = password.length()
        builder.ignoredProxyHosts = proxyIgnoredHosts
        builder.ntlmDomain = ntlmDomain
        builder.ntlmWorkstation = ntlmWorkstation

        ProxyInfo proxyInfo1 = builder.build()
        String maskedPassword = proxyInfo1.getMaskedPassword()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port
        assert proxyIgnoredHosts == proxyInfo1.ignoredProxyHosts

        assert ntlmDomain == proxyInfo1.ntlmDomain
        assert ntlmWorkstation == proxyInfo1.ntlmWorkstation

        assert password != builder.password
        assert password.length() == builder.passwordLength
        assert maskedPassword.length() == password.length()
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")
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
        String maskedPassword = proxyInfo1.getMaskedPassword()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port
        assert proxyIgnoredHosts == proxyInfo1.ignoredProxyHosts

        assert null == proxyInfo1.ntlmDomain
        assert null == proxyInfo1.ntlmWorkstation
    }
}
