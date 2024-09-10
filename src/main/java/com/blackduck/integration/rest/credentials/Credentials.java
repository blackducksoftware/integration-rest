/*
 * integration-rest
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.credentials;

import com.synopsys.integration.builder.Buildable;
import com.synopsys.integration.util.MaskedStringFieldToStringBuilder;
import com.synopsys.integration.util.Stringable;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static com.synopsys.integration.util.MaskedStringFieldToStringBuilder.MASKED_VALUE;

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
