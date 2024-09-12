package com.blackduck.integration.rest

import com.blackduck.integration.rest.credentials.Credentials
import com.blackduck.integration.rest.proxy.ProxyInfo
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.Test

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
        assert null == proxyInfo1.host.orElse(null)
        assert 0 == proxyInfo1.port
        assert null == proxyInfo1.proxyCredentials.orElse(null)
        assert null == proxyInfo1.ntlmDomain.orElse(null)
        assert null == proxyInfo1.ntlmWorkstation.orElse(null)

        String username = "username"
        String password = "password"
        credentials = new Credentials(username, password)
        proxyHost = "proxyHost"
        proxyPort = 25
        ntlmDomain = 'domain'
        ntlmWorkstation = 'workstation'

        proxyInfo1 = new ProxyInfo(proxyHost, proxyPort, credentials, ntlmDomain, ntlmWorkstation)
        String maskedPassword = proxyInfo1.getMaskedPassword().orElse(null)
        assert proxyHost == proxyInfo1.host.orElse(null)
        assert proxyPort == proxyInfo1.port
        assert credentials == proxyInfo1.proxyCredentials.orElse(null)
        assert ntlmDomain == proxyInfo1.ntlmDomain.orElse(null)
        assert ntlmWorkstation == proxyInfo1.ntlmWorkstation.orElse(null)

        assert password == proxyInfo1.password.orElse(null)
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
        assert proxyInfo.getProxy().isPresent()
        assert Proxy.NO_PROXY != proxyInfo.getProxy()

        proxyInfo = ProxyInfo.NO_PROXY_INFO
        assert !proxyInfo.getProxy().isPresent()
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
