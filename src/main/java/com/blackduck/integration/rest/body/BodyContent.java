/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.body;

import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

public interface BodyContent {
    ContentType JSON_UTF_8 = ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8);
    ContentType OCTET_STREAM_UTF_8 = ContentType.APPLICATION_OCTET_STREAM.withCharset(StandardCharsets.UTF_8);
    ContentType TEXT_PLAIN_UTF_8 = ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8);

    HttpEntity createEntity(BodyContentConverter bodyContentConverter);

}
