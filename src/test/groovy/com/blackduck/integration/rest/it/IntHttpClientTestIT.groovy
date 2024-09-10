package com.blackduck.integration.rest.it

import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.PrintStreamIntLogger
import com.blackduck.integration.rest.HttpUrl
import com.blackduck.integration.rest.RestConstants
import com.blackduck.integration.rest.client.IntHttpClient
import com.blackduck.integration.rest.credentials.Credentials
import com.blackduck.integration.rest.credentials.CredentialsBuilder
import com.blackduck.integration.rest.proxy.ProxyInfo
import com.blackduck.integration.rest.proxy.ProxyInfoBuilder
import com.blackduck.integration.rest.request.Request
import org.apache.commons.lang3.math.NumberUtils
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

@Tag("integration")
class IntHttpClientTestIT {
    private static RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper()

    @Test
    void testNullHostForProxy() {
        ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
        proxyBuilder.host = null
        ProxyInfo proxyInfo = proxyBuilder.build()
        final IntHttpClient restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
        HttpUrl url = restConnectionTestHelper.getIntegrationServerUrl()
        Request request = new Request.Builder(url).build()
        restConnection.execute(request).withCloseable {
            assertTrue(it.statusCodeSuccess)
        }
    }

    @Test
    @Disabled
    void testPassThroughProxyWithHttp() {
        ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
        proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH")
        proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH"))
        ProxyInfo proxyInfo = proxyBuilder.build()
        final IntHttpClient restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
        HttpUrl url = restConnectionTestHelper.getIntegrationServerUrl()
        Request request = new Request.Builder(url).build()
        restConnection.execute(request).withCloseable {
            assertTrue(it.statusCodeSuccess)
        }
    }

    @Test
    @Disabled
    void testBasicProxyWithHttp() {
        CredentialsBuilder credentialsBuilder = new CredentialsBuilder()
        credentialsBuilder.setUsername(restConnectionTestHelper.getProperty("TEST_PROXY_USER_BASIC"))
        credentialsBuilder.setPassword(restConnectionTestHelper.getProperty("TEST_PROXY_PASSWORD_BASIC"))

        ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
        proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_BASIC")
        proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_BASIC"))
        proxyBuilder.credentials = credentialsBuilder.build()
        ProxyInfo proxyInfo = proxyBuilder.build()
        final IntHttpClient restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
        HttpUrl url = restConnectionTestHelper.getIntegrationServerUrl()
        Request request = new Request.Builder(url).build()
        restConnection.execute(request).withCloseable {
            assertTrue(it.statusCodeSuccess)
        }
    }

    @Test
    @Disabled
    void testBasicProxyFailsWithoutCredentialsWithHttp() {
        ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
        proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_BASIC")
        proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_BASIC"))
        ProxyInfo proxyInfo = proxyBuilder.build()
        final IntHttpClient restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
        HttpUrl url = restConnectionTestHelper.getIntegrationServerUrl()
        Request request = new Request.Builder(url).build()
        restConnection.execute(request).withCloseable {
            assertEquals(RestConstants.PROXY_AUTH_407, it.statusCode)
            assertTrue(it.statusMessage.contains("Proxy Authentication Required"))
            assertTrue(it.statusCodeError)
        }
    }

    @Test
    @Disabled
    void testBasicProxyFailsWithoutCredentialsWithHttps() {
        RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper(TestingPropertyKey.TEST_HTTPS_SERVER_URL.name())
        ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
        proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_BASIC")
        proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_BASIC"))
        ProxyInfo proxyInfo = proxyBuilder.build()
        final IntHttpClient restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
        HttpUrl url = restConnectionTestHelper.getIntegrationServerUrl()
        Request request = new Request.Builder(url).build()
        restConnection.execute(request).withCloseable {
            assertEquals(RestConstants.PROXY_AUTH_407, it.statusCode)
            assertTrue(it.statusMessage.contains("Proxy Authentication Required"))
            assertTrue(it.statusCodeError)
        }
    }

    @Test
    @Disabled
    void testDigestProxyWithHttp() {
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
        final IntHttpClient restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
        HttpUrl url = restConnectionTestHelper.getIntegrationServerUrl()
        Request request = new Request.Builder(url).build()
        restConnection.execute(request).withCloseable {
            assertTrue(it.statusCodeSuccess)
        }
    }

    @Test
    @Disabled
    void testDigestProxyFailsWithoutCredentialsWithHttp() {
        ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
        proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_DIGEST")
        proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_DIGEST"))
        ProxyInfo proxyInfo = proxyBuilder.build()
        final IntHttpClient restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
        HttpUrl url = restConnectionTestHelper.getIntegrationServerUrl()
        Request request = new Request.Builder(url).build()
        restConnection.execute(request).withCloseable {
            assertEquals(RestConstants.PROXY_AUTH_407, it.statusCode)
            assertTrue(it.statusMessage.contains("Proxy Authentication Required"))
            assertTrue(it.statusCodeError)
        }
    }

    @Test
    @Disabled
    void testNtlmProxyWithHttp() {
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
        final IntHttpClient restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
        HttpUrl url = restConnectionTestHelper.getIntegrationServerUrl()
        Request request = new Request.Builder(url).build()
        restConnection.execute(request).withCloseable {
            assertTrue(it.statusCodeSuccess)
        }
    }

    @Test
    @Disabled
    void testNtlmProxyFailsWithoutCredentialsWithHttp() {
        ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder()
        proxyBuilder.host = restConnectionTestHelper.getProperty("TEST_PROXY_HOST_NTLM")
        proxyBuilder.port = NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_NTLM"))
        ProxyInfo proxyInfo = proxyBuilder.build()
        final IntHttpClient restConnection = restConnectionTestHelper.getRestConnection(proxyInfo)
        HttpUrl url = restConnectionTestHelper.getIntegrationServerUrl()
        Request request = new Request.Builder(url).build()
        restConnection.execute(request).withCloseable {
            assertEquals(RestConstants.PROXY_AUTH_407, it.statusCode)
            assertTrue(it.statusMessage.contains("Proxy Authentication Required"))
            assertTrue(it.statusCodeError)
        }
    }

    @Test
    @Disabled
    void testUnauthorizedGet() throws Exception {
        String url = restConnectionTestHelper.getProperty("TEST_AUTHENTICATED_SERVER_URL")
        final IntHttpClient restConnection = restConnectionTestHelper.getRestConnection()
        final Request request = new Request.Builder(new HttpUrl(url)).build()
        System.out.println("Executing: " + request.toString())
        restConnection.execute(request).withCloseable {
            assertTrue(it.statusCodeError)
            assertEquals(RestConstants.UNAUTHORIZED_401, it.statusCode)
        }
    }

}
