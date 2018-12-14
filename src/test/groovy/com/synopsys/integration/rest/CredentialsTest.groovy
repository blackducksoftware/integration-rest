package com.synopsys.integration.rest

import com.synopsys.integration.rest.credentials.Credentials
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.Test

class CredentialsTest {
    @Test
    void testUserAndPasswordConstructor() {
        final String username = "username"
        final String password = "password"
        Credentials credentials = new Credentials(username, password)
        final String maskedPassword = credentials.getMaskedPassword()
        assert username == credentials.username.get()
        assert password == credentials.password.get()
        assert maskedPassword.length() == 24
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")
    }

    @Test
    void testHashCode() {
        final String username1 = "username"
        final String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1)
        final String username2 = "username"
        final String password2 = "password"
        Credentials credentials2 = new Credentials(username2, password2)

        assert credentials1.hashCode() == credentials2.hashCode()
    }

    @Test
    void testEquals() {
        final String username1 = "username"
        final String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1)
        final String username2 = "username"
        final String password2 = "password"
        Credentials credentials2 = new Credentials(username2, password2)

        assert credentials1.equals(credentials2)
    }

    @Test
    void testToString() {
        final String username1 = "username"
        final String password1 = "password"
        Credentials credentials1 = new Credentials(username1, password1)
        final String username2 = "username"
        final String password2 = "password"
        Credentials credentials2 = new Credentials(username2, password2)

        assert credentials1.toString() == credentials2.toString()
    }
}
