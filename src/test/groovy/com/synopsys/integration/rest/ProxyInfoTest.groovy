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
import org.apache.commons.lang3.StringUtils
import org.junit.Test

class ProxyInfoTest {

    private static String VALID_URL = "http://www.google.com"

    @Test
    void testProxyConstructor() {

        Credentials credentials = null
        String proxyHost = null
        int proxyPort = 0
        String ntlmDomain = null
        String ntlmWorkstation = null

        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost, proxyPort, credentials, ntlmDomain, ntlmWorkstation)
        assert null == proxyInfo1.host
        assert 0 == proxyInfo1.port
        assert null == proxyInfo1.proxyCredentials
        assert null == proxyInfo1.ntlmDomain
        assert null == proxyInfo1.ntlmWorkstation

        String username = "username"
        String password = "password"
        credentials = new Credentials(username, password)
        proxyHost = "proxyHost"
        proxyPort = 25
        ntlmDomain = 'domain'
        ntlmWorkstation = 'workstation'

        proxyInfo1 = new ProxyInfo(proxyHost, proxyPort, credentials, ntlmDomain, ntlmWorkstation)
        String maskedPassword = proxyInfo1.getMaskedPassword()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port
        assert credentials == proxyInfo1.proxyCredentials
        assert ntlmDomain == proxyInfo1.ntlmDomain
        assert ntlmWorkstation == proxyInfo1.ntlmWorkstation

        assert password == proxyInfo1.password
        assert maskedPassword.length() == 24
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")
    }

    @Test
    void testOpenConnection() {
        String username = "username"
        String password = "password"
        Credentials credentials = new Credentials(username, password)
        String proxyHost = "proxyHost"
        int proxyPort = 25
        String ntlmDomain = 'domain'
        String ntlmWorkstation = 'workstation'

        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost, proxyPort, credentials, ntlmDomain, ntlmWorkstation)

        proxyInfo1.openConnection(new URL(VALID_URL))
    }

    @Test
    void testGetProxy() {
        String username = "username"
        String password = "password"
        Credentials credentials = new Credentials(username, password)
        String proxyHost = "proxyHost"
        int proxyPort = 25
        String ntlmDomain = 'domain'
        String ntlmWorkstation = 'workstation'

        ProxyInfo proxyInfo = new ProxyInfo(proxyHost, proxyPort, credentials, ntlmDomain, ntlmWorkstation)
        assert null != proxyInfo.getProxy()
        assert Proxy.NO_PROXY != proxyInfo.getProxy()

        proxyInfo = ProxyInfo.NO_PROXY_INFO
        assert null == proxyInfo.getProxy()
    }

    @Test
    void testHashCode() {
        String username1 = "username"
        String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1)
        String proxyHost1 = "proxyHost"
        int proxyPort1 = 25
        String ntlmDomain1 = 'domain'
        String ntlmWorkstation1 = 'workstation'

        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, ntlmDomain1, ntlmWorkstation1)


        String username2 = "username"
        String password2 = "password"
        Credentials credentials2 = new Credentials(username2, password2)
        String proxyHost2 = "proxyHost"
        int proxyPort2 = 25
        String ntlmDomain2 = 'domain'
        String ntlmWorkstation2 = 'workstation'

        ProxyInfo proxyInfo2 = new ProxyInfo(proxyHost2, proxyPort2, credentials2, ntlmDomain2, ntlmWorkstation2)

        assert proxyInfo1.hashCode() == proxyInfo2.hashCode()
    }

    @Test
    void testEquals() {
        String username1 = "username"
        String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1)
        String proxyHost1 = "proxyHost"
        int proxyPort1 = 25
        String ntlmDomain1 = 'domain'
        String ntlmWorkstation1 = 'workstation'
        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, ntlmDomain1, ntlmWorkstation1)


        String username2 = "username"
        String password2 = "password"
        Credentials credentials2 = new Credentials(username2, password2)
        String proxyHost2 = "proxyHost"
        int proxyPort2 = 25
        String ntlmDomain2 = 'domain'
        String ntlmWorkstation2 = 'workstation'
        ProxyInfo proxyInfo2 = new ProxyInfo(proxyHost2, proxyPort2, credentials2, ntlmDomain2, ntlmWorkstation2)

        assert proxyInfo1.equals(proxyInfo2)
    }

    @Test
    void testToString() {
        String username1 = "username"
        String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1)
        String proxyHost1 = "proxyHost"
        int proxyPort1 = 25
        String ntlmDomain1 = 'domain'
        String ntlmWorkstation1 = 'workstation'
        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, ntlmDomain1, ntlmWorkstation1)


        String username2 = "username"
        String password2 = "password"
        Credentials credentials2 = new Credentials(username2, password2)
        String proxyHost2 = "proxyHost"
        int proxyPort2 = 25
        String ntlmDomain2 = 'domain'
        String ntlmWorkstation2 = 'workstation'
        ProxyInfo proxyInfo2 = new ProxyInfo(proxyHost2, proxyPort2, credentials2, ntlmDomain2, ntlmWorkstation2)

        assert proxyInfo1.toString() == proxyInfo2.toString()
    }
}
