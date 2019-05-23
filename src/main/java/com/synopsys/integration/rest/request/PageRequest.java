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
package com.synopsys.integration.rest.request;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

public abstract class PageRequest {
    private final Request.Builder requestBuilder;
    private int offset;
    private int limit;

    public static final PageRequest createDefault(final Request.Builder requestBuilder, final int offset, final int limit) {
        final PageRequest pageRequest = createDefault(requestBuilder);
        pageRequest.offset = offset;
        pageRequest.limit = limit;
        return pageRequest;
    }

    public static final PageRequest createDefault(final Request.Builder requestBuilder) {
        return new PageRequest(requestBuilder) {
            @Override
            public String getLimitKey() {
                return "limit";
            }

            @Override
            public String getOffsetKey() {
                return "offset";
            }
        };
    }

    public PageRequest(final Request.Builder requestBuilder) {
        this.requestBuilder = requestBuilder;
        int offset = 0;
        int limit = 100;
        if (requestBuilder.getQueryParameters() != null) {
            // we know that limit and offset are only ever set as single values
            // so iterator().next() is a reasonable way to get them out of the
            // Set
            if (requestBuilder.getQueryParameters().containsKey(getOffsetKey())) {
                offset = NumberUtils.toInt(requestBuilder.getQueryParameters().get(getOffsetKey()).iterator().next(), 0);
            }
            if (requestBuilder.getQueryParameters().containsKey(getLimitKey())) {
                limit = NumberUtils.toInt(requestBuilder.getQueryParameters().get(getLimitKey()).iterator().next(), 100);
            }
        }

        this.offset = offset;
        this.limit = limit;
    }

    public PageRequest(final Request.Builder requestBuilder, final int offset, final int limit) {
        this.requestBuilder = requestBuilder;
        this.offset = offset;
        this.limit = limit;
    }

    public Request createRequest() {
        final Request request = requestBuilder.build();
        final Set<String> limitValue = new HashSet<>();
        limitValue.add(String.valueOf(getLimit()));

        final Set<String> offsetValue = new HashSet<>();
        offsetValue.add(String.valueOf(getOffset()));

        request.getQueryParameters().put(getLimitKey(), limitValue);
        request.getQueryParameters().put(getOffsetKey(), offsetValue);
        return request;
    }

    public Request.Builder getRequestBuilder() {
        return requestBuilder;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public abstract String getLimitKey();

    public abstract String getOffsetKey();

}
