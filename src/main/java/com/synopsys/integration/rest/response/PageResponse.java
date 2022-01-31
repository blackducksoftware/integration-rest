/*
 * integration-rest
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.response;

import java.util.List;

import com.synopsys.integration.rest.component.IntRestResponse;

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
