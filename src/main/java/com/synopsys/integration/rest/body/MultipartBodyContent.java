/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.synopsys.integration.rest.request.Request;

public class MultipartBodyContent implements BodyContent {
    public static final ContentType OCTET_STREAM_UTF_8 = ContentType.APPLICATION_OCTET_STREAM.withCharset(StandardCharsets.UTF_8);
    public static final ContentType TEXT_PLAIN_UTF_8 = ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8);

    private final Map<String, String> bodyContentStringMap;
    private final Map<String, File> bodyContentFileMap;

    public MultipartBodyContent(Map<String, File> bodyContentFileMap, Map<String, String> bodyContentStringMap) {
        this.bodyContentStringMap = bodyContentStringMap;
        this.bodyContentFileMap = bodyContentFileMap;
    }

    @Override
    public HttpEntity createEntity(Request request) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (Entry<String, File> entry : getBodyContentFileMap().entrySet()) {
            builder.addBinaryBody(entry.getKey(), entry.getValue(), OCTET_STREAM_UTF_8, entry.getValue().getName());
        }
        for (Entry<String, String> entry : getBodyContentStringMap().entrySet()) {
            builder.addTextBody(entry.getKey(), entry.getValue(), TEXT_PLAIN_UTF_8);
        }
        builder.setCharset(StandardCharsets.UTF_8);
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        return builder.build();
    }

    public Map<String, String> getBodyContentStringMap() {
        return bodyContentStringMap;
    }

    public Map<String, File> getBodyContentFileMap() {
        return bodyContentFileMap;
    }

}
