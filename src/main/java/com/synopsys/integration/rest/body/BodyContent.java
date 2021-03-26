/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import org.apache.http.HttpEntity;

import com.synopsys.integration.rest.request.Request;

public interface BodyContent {
    HttpEntity createEntity(final Request request);
}
