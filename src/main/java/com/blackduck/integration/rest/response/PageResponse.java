/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.response;

import java.util.List;

import com.blackduck.integration.rest.component.IntRestResponse;

public class PageResponse<R extends IntRestResponse> {
    private int count;
    private List<R> items;

    public PageResponse(final int count, final List<R> items) {
        this.count = count;
        this.items = items;
    }

    public int getCount() {
        return count;
    }

    public List<R> getItems() {
        return items;
    }

}
