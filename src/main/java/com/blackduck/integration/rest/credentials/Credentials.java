/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.credentials;

import static com.blackduck.integration.util.MaskedStringFieldToStringBuilder.MASKED_VALUE;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.builder.Buildable;
import com.blackduck.integration.util.MaskedStringFieldToStringBuilder;
import com.blackduck.integration.util.Stringable;

public class Credentials extends Stringable implements Buildable {
    public static final Credentials NO_CREDENTIALS = new Credentials(null, null);

    public static CredentialsBuilder newBuilder() {
        return new CredentialsBuilder();
    }

    private final String username;
    private final String password;

    Credentials(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public String getMaskedPassword() {
        return MASKED_VALUE;
    }

    public boolean isBlank() {
        return StringUtils.isAllBlank(username, password);
    }

    @Override
    public String toString() {
        return new MaskedStringFieldToStringBuilder(this, "password").toString();
    }

}
