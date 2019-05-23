/**
 * integration-rest
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.rest.service;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.component.IntRestResponse;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class IntResponseTransformer {
    private final IntHttpClient intHttpClient;
    private final IntJsonTransformer intJsonTransformer;

    public IntResponseTransformer(final IntHttpClient intHttpClient, final IntJsonTransformer intJsonTransformer) {
        this.intHttpClient = intHttpClient;
        this.intJsonTransformer = intJsonTransformer;
    }

    public <T extends IntRestResponse> T getResponse(final Request request, final Class<T> clazz) throws IntegrationException {
        try (final Response response = intHttpClient.execute(request)) {
            intHttpClient.throwExceptionForError(response);
            return intJsonTransformer.getResponse(response, clazz);
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public <T extends IntRestResponse> T getResponseAs(final String json, final Class<T> clazz) throws IntegrationException {
        return intJsonTransformer.getResponseAs(json, clazz);
    }

    public <T extends IntRestResponse> T getResponseAs(final JsonElement jsonElement, final Class<T> clazz) throws IntegrationException {
        return intJsonTransformer.getResponseAs(jsonElement, clazz);
    }

}
