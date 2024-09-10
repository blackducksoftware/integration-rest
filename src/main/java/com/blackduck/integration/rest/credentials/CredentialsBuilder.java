/*
 * integration-rest
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.credentials;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.builder.IntegrationBuilder;

public class CredentialsBuilder extends IntegrationBuilder<Credentials> {
    private String username;
    private String password;

    @Override
    protected Credentials buildWithoutValidation() {
        return new Credentials(username, password);
    }

    @Override
    protected void validate(final BuilderStatus builderStatus) {
        if (StringUtils.isAnyBlank(username, password) && !StringUtils.isAllBlank(username, password)) {
            builderStatus.addErrorMessage("The username and password must both be populated or both be empty.");
        }
    }

    public void setUsernameAndPassword(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

}
