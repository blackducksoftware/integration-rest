package com.blackducksoftware.integration.rest.body;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

import com.blackducksoftware.integration.rest.request.Request;

public class FileBodyContent implements BodyContent {
    private final File bodyContentFile;

    public FileBodyContent(final File bodyContentFile) {
        this.bodyContentFile = bodyContentFile;
    }

    @Override
    public HttpEntity createEntity(final Request request) {
        return new FileEntity(getBodyContentFile(), ContentType.create(request.getMimeType(), request.getBodyEncoding()));
    }

    public File getBodyContentFile() {
        return bodyContentFile;
    }
}
