/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.body;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

public class FileBodyContent implements BodyContent {
    private final File bodyContentFile;
    private final ContentType contentType;

    public FileBodyContent(File bodyContentFile, ContentType contentType) {
        this.bodyContentFile = bodyContentFile;
        this.contentType = contentType;
    }

    @Override
    public HttpEntity createEntity(BodyContentConverter bodyContentConverter) {
        return bodyContentConverter.fromFile(bodyContentFile, contentType);
    }

}
