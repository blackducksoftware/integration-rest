package com.synopsys.integration.rest

import com.synopsys.integration.rest.credentials.Credentials
import com.synopsys.integration.rest.credentials.CredentialsBuilder
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.Test

class CredentialsBuilderTest {
    @Test
    void testBuildObject() {
        String username = "username"
        String password = "password"
        CredentialsBuilder builder = new CredentialsBuilder()
        builder.username = username
        builder.password = password
        Credentials credentials = builder.build()
        String maskedPassword = credentials.getMaskedPassword()
        assert username == credentials.username.get()
        assert password == credentials.password.get()
        assert maskedPassword.length() == 24
        assert password != maskedPassword
        assert StringUtils.containsOnly(maskedPassword, "*")
    }

}
