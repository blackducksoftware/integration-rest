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
package com.synopsys.integration.rest.it

import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.PrintStreamIntLogger
import com.synopsys.integration.rest.connection.RestConnection
import com.synopsys.integration.rest.credentials.Credentials
import com.synopsys.integration.rest.credentials.CredentialsBuilder
import com.synopsys.integration.rest.proxy.ProxyInfo
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder
import com.synopsys.integration.rest.request.Request
import org.apache.commons.lang3.math.NumberUtils
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

@Tag("integration")
class RestConnectionTestIT {
    private final IntLogger logger = new PrintStreamIntLogger(System.out, RestConnectionTestHelper.DEFAULT_LOGGING_LEVEL)

    private static RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper()

    @Test
    void testTimeoutSet() {
        final RestConnection restConnection = restConnectionTestHelper.getRestConnection()
        restConnection.timeout = 459
        assertEquals(459, restConnection.timeout)
    }

    @Test
    void testPassThroughProxyWithHttp() {
        try {
            ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
            proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH")
            proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH"))
            ProxyInfo proxyInfo = proxyBuilder.build()
            final RestConnection restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
            restConnection.initialize()
            Request request = new Request.Builder().uri(restConnectionTestHelper.getIntegrationServerUrlString()).build()
            restConnection.executeRequestWithException(request)
        } catch (final Exception e) {
            fail("No exception should be thrown with a valid config: " + e.getMessage())
        }
    }

    @Test
    void testBasicProxyWithHttp() {
        try {
            CredentialsBuilder credentialsBuilder = new CredentialsBuilder()
            credentialsBuilder.setUsername(restConnectionTestHelper.getProperty("TEST_PROXY_USER_BASIC"))
            credentialsBuilder.setPassword(restConnectionTestHelper.getProperty("TEST_PROXY_PASSWORD_BASIC"))

            ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
            proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_BASIC")
            proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_BASIC"))
            proxyBuilder.credentials = credentialsBuilder.build()
            ProxyInfo proxyInfo = proxyBuilder.build()
            final RestConnection restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
            restConnection.initialize()
            Request request = new Request.Builder().uri(restConnectionTestHelper.getIntegrationServerUrlString()).build()
            restConnection.executeRequestWithException(request)
        } catch (final Exception e) {
            fail("No exception should be thrown with a valid config: " + e.getMessage())
        }
    }

    @Test
    void testBasicProxyFailsWithoutCredentialsWithHttp() {
        try {
            ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
            proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_BASIC")
            proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_BASIC"))
            ProxyInfo proxyInfo = proxyBuilder.build()
            final RestConnection restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
            restConnection.initialize()
            Request request = new Request.Builder().uri(restConnectionTestHelper.getIntegrationServerUrlString()).build()
            restConnection.executeRequestWithException(request)
            fail("An exception should be thrown")
        } catch (final Exception e) {
            assertFalse(e.getMessage().contains("Can not reach this server"), e.getMessage())
            assertTrue(e.getMessage().contains("Proxy Authentication Required"), e.getMessage())
        }
    }

    @Test
    void testBasicProxyFailsWithoutCredentialsWithHttps() {
        RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper(TestingPropertyKey.TEST_HTTPS_SERVER_URL.name())
        try {
            ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
            proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_BASIC")
            proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_BASIC"))
            ProxyInfo proxyInfo = proxyBuilder.build()
            final RestConnection restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
            restConnection.initialize()
            Request request = new Request.Builder().uri(restConnectionTestHelper.getIntegrationServerUrlString()).build()
            restConnection.executeRequestWithException(request)
            fail("An exception should be thrown")
        } catch (final Exception e) {
            assertFalse(e.getMessage().contains("Can not reach this server"), e.getMessage())
            assertTrue(e.getMessage().contains("Proxy Authentication Required"), e.getMessage())
        }
    }

    @Test
    void testDigestProxyWithHttp() {
        try {
            String proxyUsername = restConnectionTestHelper.getProperty("TEST_PROXY_USER_DIGEST")
            String proxyPassword = restConnectionTestHelper.getProperty("TEST_PROXY_PASSWORD_DIGEST")
            CredentialsBuilder credentialsBuilder = new CredentialsBuilder()
            credentialsBuilder.setUsername(proxyUsername)
            credentialsBuilder.setPassword(proxyPassword)
            Credentials proxyCredentials = credentialsBuilder.build()

            ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
            proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_DIGEST")
            proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_DIGEST"))
            proxyBuilder.credentials = proxyCredentials
            ProxyInfo proxyInfo = proxyBuilder.build()
            final RestConnection restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
            restConnection.initialize()
            Request request = new Request.Builder().uri(restConnectionTestHelper.getIntegrationServerUrlString()).build()
            restConnection.executeRequestWithException(request)
        } catch (final Exception e) {
            fail("No exception should be thrown with a valid config: " + e.getMessage())
        }
    }

    @Test
    void testDigestProxyFailsWithoutCredentialsWithHttp() {
        try {
            ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
            proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_DIGEST")
            proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_DIGEST"))
            ProxyInfo proxyInfo = proxyBuilder.build()
            final RestConnection restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
            restConnection.initialize()
            Request request = new Request.Builder().uri(restConnectionTestHelper.getIntegrationServerUrlString()).build()
            restConnection.executeRequestWithException(request).close()
            fail("An exception should be thrown")
        } catch (final Exception e) {
            assertFalse(e.getMessage().contains("Can not reach this server"), e.getMessage())
            assertTrue(e.getMessage().contains("Proxy Authentication Required"), e.getMessage())
        }
    }

    @Test
    void testNtlmProxyWithHttp() {
        try {
            CredentialsBuilder credentialsBuilder = new CredentialsBuilder()
            credentialsBuilder.setUsername(restConnectionTestHelper.getProperty("TEST_PROXY_USER_NTLM"))
            credentialsBuilder.setPassword(restConnectionTestHelper.getProperty("TEST_PROXY_PASSWORD_NTLM"))
            ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
            proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_NTLM")
            proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_NTLM"))
            proxyBuilder.credentials = credentialsBuilder.build()
            proxyBuilder.ntlmDomain = restConnectionTestHelper.getProperty("TEST_PROXY_DOMAIN_NTLM")
            proxyBuilder.ntlmWorkstation = restConnectionTestHelper.getProperty("TEST_PROXY_WORKSTATION_NTLM")
            ProxyInfo proxyInfo = proxyBuilder.build()
            final RestConnection restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
            restConnection.initialize()
            Request request = new Request.Builder().uri(restConnectionTestHelper.getIntegrationServerUrlString()).build()
            restConnection.executeRequestWithException(request).close()
        } catch (final Exception e) {
            fail("No exception should be thrown with a valid config: " + e.getMessage())
        }
    }

    @Test
    void testNtlmProxyFailsWithoutCredentialsWithHttp() {
        try {
            ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
            proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_NTLM")
            proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_NTLM"))
            ProxyInfo proxyInfo = proxyBuilder.build()
            final RestConnection restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
            Request request = new Request.Builder().uri(restConnectionTestHelper.getIntegrationServerUrlString()).build()
            restConnection.executeRequestWithException(request).close()
            fail("An exception should be thrown")
        } catch (final Exception e) {
            assertFalse(e.getMessage().contains("Can not reach this server"), e.getMessage())
            assertTrue(e.getMessage().contains("Proxy Authentication Required"), e.getMessage())
        }
    }

    @Test
    void testUnauthorizedGet() throws Exception {
        String url = restConnectionTestHelper.getProperty("TEST_AUTHENTICATED_SERVER_URL")
        final RestConnection restConnection = new RestConnection(logger, 120, true, ProxyInfo.NO_PROXY_INFO)
        final Request hubRequest = new Request.Builder(url.toString()).build()
        System.out.println("Executing: " + hubRequest.toString())
        try {
            restConnection.executeRequestWithException(hubRequest)
            fail("Expected Unauthorized Exception")
        } catch (final Exception e) {
            logger.trace("[ExceptionMessage]: " + e.getMessage())
            assertTrue(e.getMessage().contains("401"))
        }
    }
}
