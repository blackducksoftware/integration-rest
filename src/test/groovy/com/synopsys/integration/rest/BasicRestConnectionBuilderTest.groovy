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

import com.synopsys.integration.rest.connection.RestConnection
import com.synopsys.integration.rest.connection.UnauthenticatedRestConnection
import com.synopsys.integration.rest.proxy.ProxyInfo
import com.synopsys.integration.test.tool.TestLogger
import org.junit.Test

class BasicRestConnectionBuilderTest {
    @Test
    void testMinimumValid() {
        RestConnection builder = createValid()
    }

    @Test
    void testInvalidTimeout() {
        RestConnection restConnection = createValid()
        restConnection.timeout = -1
        assert !restConnection.isValid()

        restConnection.timeout = 120
        assert restConnection.isValid()
    }

    @Test
    void testLogger() {
        RestConnection valid = createValid()
        assert valid.isValid()

        RestConnection invalid = new UnauthenticatedRestConnection(null, RestConnection.DEFAULT_TIMEOUT, false, ProxyInfo.NO_PROXY_INFO)
        assert !invalid.isValid()
    }

    private static RestConnection createValid() throws IllegalAccessException {
        return new UnauthenticatedRestConnection(new TestLogger(), RestConnection.DEFAULT_TIMEOUT, false, ProxyInfo.NO_PROXY_INFO)
    }

}
