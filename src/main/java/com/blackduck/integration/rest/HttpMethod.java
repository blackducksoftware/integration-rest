/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest;

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
