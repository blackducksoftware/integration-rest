/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest;

import org.apache.commons.lang3.StringUtils;

public enum HttpMethod {
    GET,
    PUT,
    POST,
    PATCH,
    DELETE,
    HEAD,
    OPTIONS,
    TRACE;

    public static HttpMethod fromMethod(String method) {
        return HttpMethod.valueOf(StringUtils.upperCase(method));
    }

}
