/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import com.synopsys.integration.rest.request.Request;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

import java.io.File;

public class FileBodyContent implements BodyContent {
    private final File bodyContentFile;

    public FileBodyContent(File bodyContentFile) {
        this.bodyContentFile = bodyContentFile;
    }

    @Override
    public HttpEntity createEntity(Request request) {
        return new FileEntity(getBodyContentFile(), ContentType.create(request.getAcceptMimeType(), request.getBodyEncoding()));
    }

    public File getBodyContentFile() {
        return bodyContentFile;
    }
}
