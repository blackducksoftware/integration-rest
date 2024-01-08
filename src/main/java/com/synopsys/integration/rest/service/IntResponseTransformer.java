/*
 * integration-rest
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.component.IntRestResponse;
import com.synopsys.integration.rest.request.PageRequestHandler;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class IntResponseTransformer {
    private final IntHttpClient intHttpClient;
    private final IntJsonTransformer intJsonTransformer;

    public IntResponseTransformer(final IntHttpClient intHttpClient, final IntJsonTransformer intJsonTransformer) {
        this.intHttpClient = intHttpClient;
        this.intJsonTransformer = intJsonTransformer;
    }

    public <R extends IntRestResponse> R getResponses(Request.Builder requestBuilder, PageRequestHandler pageRequestHandler, final Class<R> responseClass, int pageSize) throws IntegrationException {
        final List<R> allResponses = new ArrayList<>();
        int currentResponseDataCount = 0;
        int totalResponseDataCount;
        int offset = 0;
        do {
            final Request request = pageRequestHandler.createPageRequest(requestBuilder, offset, pageSize);
            final R response = getResponse(request, responseClass);
            allResponses.add(response);

            currentResponseDataCount += pageRequestHandler.getCurrentResponseCount(response);
            totalResponseDataCount = pageRequestHandler.getTotalResponseCount(response);
            offset += pageSize;
        } while (totalResponseDataCount > currentResponseDataCount);

        return pageRequestHandler.combineResponses(allResponses);
    }

    public <R extends IntRestResponse> R getResponse(Request request, Class<R> responseClass) throws IntegrationException {
        try (final Response response = intHttpClient.execute(request)) {
            intHttpClient.throwExceptionForError(response);
            return intJsonTransformer.getResponse(response, responseClass);
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public <R extends IntRestResponse> R getResponseAs(String json, Class<R> responseClass) throws IntegrationException {
        return intJsonTransformer.getComponentAs(json, responseClass);
    }

    public <R extends IntRestResponse> R getResponseAs(JsonObject jsonObject, Class<R> responseClass) throws IntegrationException {
        return intJsonTransformer.getComponentAs(jsonObject, responseClass);
    }

}
