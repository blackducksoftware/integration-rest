/*
 * integration-rest
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

import com.synopsys.integration.rest.request.Request;

public interface BodyContent {
    ContentType JSON_UTF_8 = ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8);
    ContentType OCTET_STREAM_UTF_8 = ContentType.APPLICATION_OCTET_STREAM.withCharset(StandardCharsets.UTF_8);
    ContentType TEXT_PLAIN_UTF_8 = ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8);

    HttpEntity createEntity(BodyContentConverter bodyContentConverter);

}
