package com.synopsys.integration.rest

import com.synopsys.integration.rest.credentials.Credentials
import com.synopsys.integration.rest.proxy.ProxyInfo
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder
import org.junit.jupiter.api.Test

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
