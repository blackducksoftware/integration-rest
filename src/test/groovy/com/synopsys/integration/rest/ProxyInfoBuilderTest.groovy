package com.synopsys.integration.rest

import com.synopsys.integration.rest.credentials.Credentials
import com.synopsys.integration.rest.proxy.ProxyInfo
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder
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
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port

        assert ntlmDomain == proxyInfo1.ntlmDomain
        assert ntlmWorkstation == proxyInfo1.ntlmWorkstation
    }

    @Test
    void testUnauthenticatedBuilder() {
        String proxyHost = "proxyHost"
        int proxyPort = 25

        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = proxyHost
        builder.port = proxyPort

        ProxyInfo proxyInfo1 = builder.build()
        assert proxyHost == proxyInfo1.host
        assert proxyPort == proxyInfo1.port

        assert null == proxyInfo1.ntlmDomain
        assert null == proxyInfo1.ntlmWorkstation
    }

    @Test
    void testProxyValid() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        assert !builder.hasProxySettings()
        assert builder.isValid()

        builder.host = "proxyhost"
        builder.port = 25
        assert builder.hasProxySettings()
        assert builder.isValid()

        builder.host = "proxyhost"
        builder.port = 25
        builder.credentials = new Credentials("proxyUser", "proxyPassword")
        builder.ntlmDomain = "domain"
        builder.ntlmWorkstation = "workstation"

        assert builder.hasProxySettings()
        assert builder.isValid()
    }

    @Test
    void testInvalidPort() {
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
    void testValidCredentials() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = "proxyhost"
        builder.port = 25
        builder.credentials = new Credentials("proxyUser", "proxyPassword")
        assert builder.hasProxySettings()
        assert builder.isValid()
    }

    @Test
    void testInvalidCredentials() {
        ProxyInfoBuilder builder = new ProxyInfoBuilder()
        builder.host = ""
        builder.port = 25
        builder.credentials = new Credentials("proxyUser", "proxyPassword")
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
}
