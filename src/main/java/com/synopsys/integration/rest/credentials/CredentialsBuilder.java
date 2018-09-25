/**
 * integration-rest
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import com.synopsys.integration.builder.AbstractBuilder;
import com.synopsys.integration.validator.AbstractValidator;

public class CredentialsBuilder extends AbstractBuilder<Credentials> {
    private String username;
    private String password;

    @Override
    public Credentials buildObject() {
        Credentials creds = new Credentials(username, password);
        return creds;
    }

    @Override
    public AbstractValidator createValidator() {
        final CredentialsValidator validator = new CredentialsValidator();
        validator.setUsername(getUsername());
        validator.setPassword(getPassword());
        return validator;
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
