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
package com.synopsys.integration.rest.connection;

import org.apache.http.client.methods.HttpUriRequest;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Response;

/**
 * A rest connection that will attempt to reconnect in the event of the client being unauthorized multiple times before
 * throwing an exception. Other unsuccessful status codes will result in an exception being thrown
 */
public abstract class ReconnectingRestConnection extends RestConnection {
    public ReconnectingRestConnection(final IntLogger logger, final int timeout, final boolean alwaysTrustServerCertificate, final ProxyInfo proxyInfo) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo);
    }

    @Override
    public Response executeRequest(final HttpUriRequest request) throws IntegrationException {
        return executeRequest(request, 0);
    }

    /**
     * Will attempt connection again. Subclasses should override the handleErrorResponse method to prepare the request to be executed again
     */
    private Response executeRequest(final HttpUriRequest request, final int retryCount) throws IntegrationException {
        final Response response = super.executeRequest(request);

        final int statusCode = response.getStatusCode();
        final boolean unauthorized = isUnauthorized(statusCode);

        if (unauthorized && retryCount < 2) {
            return executeRequest(request, retryCount + 1);
        } else if (unauthorized) {
            throw new IntegrationException("Failed to reconnect: " + statusCode);
        }

        return response;
    }

    protected boolean isUnauthorized(final int statusCode) {
        return statusCode == RestConstants.UNAUTHORIZED_401 || statusCode == RestConstants.FORBIDDEN_403;
    }
}
