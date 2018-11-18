package com.synopsys.integration.rest

import com.synopsys.integration.rest.credentials.Credentials
import com.synopsys.integration.rest.proxy.ProxyInfo
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.Test

class ProxyInfoTest {

    private static String VALID_URL = "http://www.google.com"

    @Test
    public void testProxyConstructor() {

        String username1 = null
        String password1 = null
        Credentials credentials1 = null
        String proxyHost1 = null
        int proxyPort1 = 0
        String proxyIgnoredHosts1 = null
        String ntlmDomain1 = null
        String ntlmWorkstation1 = null

        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1, ntlmDomain1, ntlmWorkstation1)
        assert null == proxyInfo1.host
        assert 0 == proxyInfo1.port
        assert null == proxyInfo1.proxyCredentials
        assert null == proxyInfo1.ignoredProxyHosts
        assert null == proxyInfo1.ntlmDomain
        assert null == proxyInfo1.ntlmWorkstation

        username1 = "username"
        password1 = "password"
        credentials1 = new Credentials(username1, password1);
        proxyHost1 = "proxyHost"
        proxyPort1 = 25
        proxyIgnoredHosts1 = "*"
        ntlmDomain1 = 'domain'
        ntlmWorkstation1 = 'workstation'

        proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1, ntlmDomain1, ntlmWorkstation1)
        String maskedPassword = proxyInfo1.getMaskedPassword()
        assert proxyHost1 == proxyInfo1.host
        assert proxyPort1 == proxyInfo1.port
        assert credentials1 == proxyInfo1.proxyCredentials
        assert proxyIgnoredHosts1 == proxyInfo1.ignoredProxyHosts
        assert ntlmDomain1 == proxyInfo1.ntlmDomain
        assert ntlmWorkstation1 == proxyInfo1.ntlmWorkstation

        assert password1 == proxyInfo1.password
        assert maskedPassword.length() == 24
        assert password1 != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")
    }

    @Test
    public void testOpenConnection() {
        String username1 = "username"
        String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1);
        String proxyHost1 = "proxyHost"
        int proxyPort1 = 25
        String proxyIgnoredHosts1 = ".*"
        String ntlmDomain1 = 'domain'
        String ntlmWorkstation1 = 'workstation'

        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1, ntlmDomain1, ntlmWorkstation1)

        proxyInfo1.openConnection(new URL(VALID_URL))
    }

    @Test
    public void testShouldUseProxy() {
        String username1 = "username"
        String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1);
        String proxyHost1 = "proxyHost"
        int proxyPort1 = 25
        String proxyIgnoredHosts1 = ""
        String ntlmDomain1 = 'domain'
        String ntlmWorkstation1 = 'workstation'

        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1, ntlmDomain1, ntlmWorkstation1)

        assert true == proxyInfo1.shouldUseProxyForUrl(new URL(VALID_URL))

        proxyIgnoredHosts1 = ".*"
        proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1, ntlmDomain1, ntlmWorkstation1)
        boolean result = proxyInfo1.shouldUseProxyForUrl(new URL(VALID_URL))
        assert !result
    }

    @Test
    public void testGetProxy() {
        String username1 = "username"
        String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1);
        String proxyHost1 = "proxyHost"
        int proxyPort1 = 25
        String proxyIgnoredHosts1 = ""
        String ntlmDomain1 = 'domain'
        String ntlmWorkstation1 = 'workstation'

        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1, ntlmDomain1, ntlmWorkstation1)
        assert null != proxyInfo1.getProxy(new URL(VALID_URL))

        proxyIgnoredHosts1 = ".*"
        proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1, ntlmDomain1, ntlmWorkstation1)
        assert Proxy.NO_PROXY == proxyInfo1.getProxy(new URL(VALID_URL))
    }

    @Test
    public void testHashCode() {
        String username1 = "username"
        String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1);
        String proxyHost1 = "proxyHost"
        int proxyPort1 = 25
        String proxyIgnoredHosts1 = "*"
        String ntlmDomain1 = 'domain'
        String ntlmWorkstation1 = 'workstation'

        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1, ntlmDomain1, ntlmWorkstation1)

        String username2 = "username"
        String password2 = "password"
        Credentials credentials2 = new Credentials(username1, password1);
        String proxyHost2 = "proxyHost"
        int proxyPort2 = 25
        String proxyIgnoredHosts2 = "*"
        String ntlmDomain2 = 'domain'
        String ntlmWorkstation2 = 'workstation'

        ProxyInfo proxyInfo2 = new ProxyInfo(proxyHost2, proxyPort2, credentials2, proxyIgnoredHosts2, ntlmDomain2, ntlmWorkstation2)

        assert proxyInfo1.hashCode() == proxyInfo2.hashCode()
    }

    @Test
    public void testEquals() {
        String username1 = "username"
        String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1);
        String proxyHost1 = "proxyHost"
        int proxyPort1 = 25
        String proxyIgnoredHosts1 = "*"
        String ntlmDomain1 = 'domain'
        String ntlmWorkstation1 = 'workstation'
        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1, ntlmDomain1, ntlmWorkstation1)

        String username2 = "username"
        String password2 = "password"
        Credentials credentials2 = new Credentials(username1, password1);
        String proxyHost2 = "proxyHost"
        int proxyPort2 = 25
        String proxyIgnoredHosts2 = "*"
        String ntlmDomain2 = 'domain'
        String ntlmWorkstation2 = 'workstation'
        ProxyInfo proxyInfo2 = new ProxyInfo(proxyHost2, proxyPort2, credentials2, proxyIgnoredHosts2, ntlmDomain2, ntlmWorkstation2)

        assert proxyInfo1.equals(proxyInfo2)
    }

    @Test
    public void testToString() {
        String username1 = "username"
        String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1);
        String proxyHost1 = "proxyHost"
        int proxyPort1 = 25
        String proxyIgnoredHosts1 = "*"
        String ntlmDomain1 = 'domain'
        String ntlmWorkstation1 = 'workstation'
        ProxyInfo proxyInfo1 = new ProxyInfo(proxyHost1, proxyPort1, credentials1, proxyIgnoredHosts1, ntlmDomain1, ntlmWorkstation1)

        String username2 = "username"
        String password2 = "password"
        Credentials credentials2 = new Credentials(username1, password1);
        String proxyHost2 = "proxyHost"
        int proxyPort2 = 25
        String proxyIgnoredHosts2 = "*"
        String ntlmDomain2 = 'domain'
        String ntlmWorkstation2 = 'workstation'
        ProxyInfo proxyInfo2 = new ProxyInfo(proxyHost2, proxyPort2, credentials2, proxyIgnoredHosts2, ntlmDomain2, ntlmWorkstation2)

        assert proxyInfo1.toString() == proxyInfo2.toString()
    }
}
