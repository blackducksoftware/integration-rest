/**
 * Hub Common Rest
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
 * under the License.*/
package com.synopsys.integration.rest

import com.synopsys.integration.rest.connection.UnauthenticatedRestConnectionBuilder
import com.synopsys.integration.test.tool.TestLogger
import org.junit.Test

class UnauthenticatedRestConnectionBuilderTest {
    @Test
    public void testMinimumValid() {
        UnauthenticatedRestConnectionBuilder builder = createValid()
        assert builder.isValid()
    }

    @Test
    public void testInvalidTimeout() {
        UnauthenticatedRestConnectionBuilder builder = createValid()
        builder.timeout = -1
        assert !builder.isValid()

        builder.timeout = 120
        assert builder.isValid()
    }

    @Test
    public void testNoBaseUrlInvalid() {
        UnauthenticatedRestConnectionBuilder builder = createValid()
        builder.baseUrl = null
        assert !builder.isValid()

        builder.baseUrl = "http://www.google.com"
        assert builder.isValid()
    }

    @Test
    public void testBaseUrlInvalid() {
        UnauthenticatedRestConnectionBuilder builder = createValid()
        builder.baseUrl = "htp:/a.bad.domain"
        assert !builder.isValid()

        builder.baseUrl = "http://www.google.com"
        assert builder.isValid()
    }

    @Test
    public void testLogger() {
        UnauthenticatedRestConnectionBuilder builder = createValid()
        builder.logger = null
        assert !builder.isValid()

        builder.logger = new TestLogger();
        assert builder.isValid()
    }

    @Test
    public void testHeadersValid() {
        UnauthenticatedRestConnectionBuilder builder = createValid()
        builder.commonRequestHeaders = null
        assert !builder.isValid()

        builder.commonRequestHeaders = new HashMap<>();
        assert builder.isValid()
    }

    private UnauthenticatedRestConnectionBuilder createValid() {
        UnauthenticatedRestConnectionBuilder builder = new UnauthenticatedRestConnectionBuilder();
        builder.baseUrl = "http://www.google.com"
        builder.logger = new TestLogger()
        return builder
    }

}
