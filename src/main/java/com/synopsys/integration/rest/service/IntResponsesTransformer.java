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
import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.component.IntRestResponse;
import com.synopsys.integration.rest.request.PageRequest;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.rest.response.PageResponse;
import com.synopsys.integration.rest.response.PageResponseCreator;

public class IntResponsesTransformer {
    private final IntHttpClient intHttpClient;
    private final IntJsonTransformer intJsonTransformer;

    public IntResponsesTransformer(final IntHttpClient intHttpClient, final IntJsonTransformer intJsonTransformer) {
        this.intHttpClient = intHttpClient;
        this.intJsonTransformer = intJsonTransformer;
    }

    public <T extends IntRestResponse> PageResponse<T> getAllResponses(final PageRequest pagedRequest, final PageResponseCreator<T> pageResponseCreator, final Class<T> clazz) throws IntegrationException {
        return getResponses(pagedRequest, pageResponseCreator, clazz, true);
    }

    public <T extends IntRestResponse> PageResponse<T> getResponses(final PageRequest pagedRequest, final PageResponseCreator<T> pageResponseCreator, final Class<T> clazz, final boolean getAll) throws IntegrationException {
        final List<T> allResponses = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = pagedRequest.getOffset();
        Request request = pagedRequest.createRequest();
        try (final Response initialResponse = intHttpClient.execute(request)) {
            intHttpClient.throwExceptionForError(initialResponse);
            final String initialJsonResponse = initialResponse.getContentString();
            PageResponse<T> pageResponse = intJsonTransformer.getResponses(initialJsonResponse, pageResponseCreator, clazz);
            allResponses.addAll(pageResponse.getItems());

            totalCount = pageResponse.getCount();
            if (!getAll) {
                return pageResponseCreator.apply(totalCount, allResponses);
            }

            while (allResponses.size() < totalCount && currentOffset < totalCount) {
                currentOffset += pagedRequest.getLimit();
                final PageRequest offsetPagedRequest = createPageRequest(pagedRequest.getRequestBuilder(), pagedRequest.getOffsetKey(), currentOffset, pagedRequest.getLimitKey(), pagedRequest.getLimit());
                request = offsetPagedRequest.createRequest();
                try (final Response response = intHttpClient.execute(request)) {
                    intHttpClient.throwExceptionForError(response);
                    final String jsonResponse = response.getContentString();
                    pageResponse = intJsonTransformer.getResponses(jsonResponse, pageResponseCreator, clazz);
                    allResponses.addAll(pageResponse.getItems());
                } catch (final IOException e) {
                    throw new IntegrationException(e);
                }
            }
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }

        return pageResponseCreator.apply(totalCount, allResponses);
    }

    private PageRequest createPageRequest(final Request.Builder requestBuilder, final String offsetKey, final int offset, final String limitKey, final int limit) {
        return new PageRequest(requestBuilder, offset, limit) {
            @Override
            public String getLimitKey() {
                return limitKey;
            }

            @Override
            public String getOffsetKey() {
                return offsetKey;
            }
        };
    }

}
