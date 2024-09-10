package com.blackduck.integration.rest

import com.blackduck.integration.rest.credentials.Credentials
import com.blackduck.integration.rest.proxy.ProxyInfo
import com.blackduck.integration.rest.proxy.ProxyInfoBuilder
import org.junit.jupiter.api.Test

class ProxyInfoBuilderTest {
    @Test
    void testBuilder() {
        Credentials credentials = new Credentials("username", "password")
        String proxyHost = "proxyHost"
        int proxyPort = 25
        String ntlmDomain = 'domain'
        String ntlmWorkstation = 'workstation'

        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort
        builder.credentials = credentials
        builder.ntlmDomain = ntlmDomain
        builder.ntlmWorkstation = ntlmWorkstation

        ProxyInfo proxyInfo1 = builder.build()
        assert proxyHost == proxyInfo1.host.orElse(null)
        assert proxyPort == proxyInfo1.port

        assert ntlmDomain == proxyInfo1.ntlmDomain.orElse(null)
        assert ntlmWorkstation == proxyInfo1.ntlmWorkstation.orElse(null)
    }

    @Test
    void testUnauthenticatedBuilder() {
        String proxyHost = "proxyHost"
        int proxyPort = 25

        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort

        ProxyInfo proxyInfo1 = builder.build()
        assert proxyHost == proxyInfo1.host.orElse(null)
        assert proxyPort == proxyInfo1.port

        assert null == proxyInfo1.ntlmDomain.orElse(null)
        assert null == proxyInfo1.ntlmWorkstation.orElse(null)
    }

    @Test
    void testProxyValid() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        assert builder.isValid()

        builder.host = "proxyhost"
        builder.port = 25
        assert builder.isValid()

        builder.host = "proxyhost"
        builder.port = 25
        builder.credentials = new Credentials("proxyUser", "proxyPassword")
        builder.ntlmDomain = "domain"
        builder.ntlmWorkstation = "workstation"

        assert builder.isValid()
    }

    @Test
    void testInvalidPort() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = -1
        assert !builder.isValid()

        builder = new ProxyInfoBuilder()
        builder.host = ""
        builder.port = 25
        assert !builder.isValid()

        builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 0
        assert !builder.isValid()
    }

    @Test
    void testValidCredentials() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 25
        builder.credentials = new Credentials("proxyUser", "proxyPassword")
        assert builder.isValid()
    }

    @Test
    void testInvalidCredentials() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = ""
        builder.port = 25
        builder.credentials = new Credentials("proxyUser", "proxyPassword")
        assert !builder.isValid()

        builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 25
        builder.ntlmDomain = "domain"
        builder.ntlmWorkstation = "workstation"
        assert !builder.isValid()

        builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 25
        builder.ntlmDomain = 'domain'
        assert !builder.isValid()

        builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 25
        builder.ntlmWorkstation = "workstation"
        assert !builder.isValid()
    }

    @Test
    void testBlankProxy() {
        ProxyInfoBuilder proxyInfoBuilder = new ProxyInfoBuilder();
        assert proxyInfoBuilder.isBlank()
        assert proxyInfoBuilder.isValid()
        assert ProxyInfo.NO_PROXY_INFO == proxyInfoBuilder.build()

        proxyInfoBuilder.setCredentials(Credentials.NO_CREDENTIALS);

        assert proxyInfoBuilder.isBlank()
        assert proxyInfoBuilder.isValid()
        assert ProxyInfo.NO_PROXY_INFO == proxyInfoBuilder.build()

        proxyInfoBuilder.setCredentials(null)

        assert proxyInfoBuilder.isBlank()
        assert proxyInfoBuilder.isValid()
        assert ProxyInfo.NO_PROXY_INFO == proxyInfoBuilder.build()
    }

}
