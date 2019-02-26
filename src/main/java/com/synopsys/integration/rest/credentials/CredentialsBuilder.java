/**
 * integration-rest
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.rest.credentials;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.util.BuilderStatus;
import com.synopsys.integration.util.IntegrationBuilder;

public class CredentialsBuilder extends IntegrationBuilder<Credentials, CredentialsBuilder.Property> {
    @Override
    protected Credentials buildWithoutValidation() {
        return new Credentials(getUsername(), getPassword());
    }

    @Override
    protected void validate(final BuilderStatus builderStatus) {
        if (StringUtils.isAnyBlank(getUsername(), getPassword()) && !StringUtils.isAllBlank(getUsername(), getPassword())) {
            builderStatus.addErrorMessage("The username and password must both be populated or both be empty.");
        }
    }

    public void setUsernameAndPassword(final String username, final String password) {
        setUsername(username);
        setPassword(password);
    }

    public String getUsername() {
        return get(Property.USERNAME);
    }

    public void setUsername(final String username) {
        put(Property.USERNAME, username);
    }

    public String getPassword() {
        return get(Property.PASSWORD);
    }

    public void setPassword(final String password) {
        put(Property.PASSWORD, password);
    }

    public enum Property {
        USERNAME,
        PASSWORD;

    }

}