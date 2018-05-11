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
package com.blackducksoftware.integration.rest

import com.blackducksoftware.integration.rest.connection.RestConnectionField
import com.blackducksoftware.integration.rest.credentials.CredentialsField
import com.blackducksoftware.integration.rest.proxy.ProxyInfoField
import org.junit.Test

class BuilderFieldEnumTest {

    @Test
    public void testRestConnectionFieldEnum() {
        assert "restConnectionUrl" == RestConnectionField.URL.getKey()
        assert "restConnectionTimeout" == RestConnectionField.TIMEOUT.getKey()
        assert "restConnectionLogger" == RestConnectionField.LOGGER.getKey()
        assert "restConnectionHeaders" == RestConnectionField.COMMON_HEADERS.getKey()
    }

    @Test
    public void testCredentialsFieldEnum() {
        assert "username" == CredentialsField.USERNAME.getKey()
        assert "password" == CredentialsField.PASSWORD.getKey()
    }

    @Test
    public void testProxyInfoFieldEnum() {
        assert "proxyHost" == ProxyInfoField.PROXYHOST.getKey();
        assert "proxyPort" == ProxyInfoField.PROXYPORT.getKey();
        assert "proxyUsername" == ProxyInfoField.PROXYUSERNAME.getKey();
        assert "proxyPassword" == ProxyInfoField.PROXYPASSWORD.getKey();
        assert "noProxyHosts" == ProxyInfoField.NOPROXYHOSTS.getKey();
    }

}
