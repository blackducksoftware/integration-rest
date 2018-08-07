package com.blackducksoftware.integration.rest.body;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.blackducksoftware.integration.rest.request.Request;

public class MultipartBodyContent implements BodyContent {
    private final Map<String, String> bodyContentStringMap;
    private final Map<String, File> bodyContentFileMap;

    public MultipartBodyContent(final Map<String, File> bodyContentFileMap, final Map<String, String> bodyContentStringMap) {
        this.bodyContentStringMap = bodyContentStringMap;
        this.bodyContentFileMap = bodyContentFileMap;
    }

    @Override
    public HttpEntity createEntity(final Request request) {
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (final Entry<String, File> entry : getBodyContentFileMap().entrySet()) {
            builder.addBinaryBody(entry.getKey(), entry.getValue());
        }
        for (final Entry<String, String> entry : getBodyContentStringMap().entrySet()) {
            builder.addTextBody(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public Map<String, String> getBodyContentStringMap() {
        return bodyContentStringMap;
    }

    public Map<String, File> getBodyContentFileMap() {
        return bodyContentFileMap;
    }
}
