/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import java.io.File;

import org.apache.http.HttpEntity;

public class FileBodyContent implements BodyContent {
    private final File bodyContentFile;

    public FileBodyContent(File bodyContentFile) {
        this.bodyContentFile = bodyContentFile;
    }

    @Override
    public HttpEntity createEntity(BodyContentConverter bodyContentConverter) {
        return bodyContentConverter.fromFile(bodyContentFile);
    }

}
