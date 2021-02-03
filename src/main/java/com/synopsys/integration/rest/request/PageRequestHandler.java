/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.rest.request;

import java.util.Collection;

import com.synopsys.integration.rest.component.IntRestResponse;

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
