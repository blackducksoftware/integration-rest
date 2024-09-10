/*
 * integration-rest
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.request;

import java.util.Collection;

import com.blackduck.integration.rest.component.IntRestResponse;

public interface PageRequestHandler {
    /**
     * @return A request for a page of data starting from the offset and ending at the offset + limit
     */
    Request createPageRequest(final Request.Builder requestBuilder, int offset, int limit);

    /**
     * @return The total number of objects that can be retrieved from the endpoint from which this response originated
     */
    <R extends IntRestResponse> int getTotalResponseCount(R response);

    /**
     * @return The number of objects in the current page of data
     */
    <R extends IntRestResponse> int getCurrentResponseCount(R response);

    /**
     * @return One response object representing the unification of all pages of data
     */
    <R extends IntRestResponse> R combineResponses(Collection<R> pagedResponses);

}
