/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.body;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;

public class BodyContentConverter {
    public static final ContentType OCTET_STREAM_UTF_8 = ContentType.APPLICATION_OCTET_STREAM.withCharset(StandardCharsets.UTF_8);
    public static final ContentType TEXT_PLAIN_UTF_8 = ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8);

    private final Gson gson;
    private final String mimeType;
    private final Charset bodyEncoding;

    public BodyContentConverter(Gson gson, String mimeType, Charset bodyEncoding) {
        this.gson = gson;

        //ContentType requires a non-null mime type
        this.mimeType = StringUtils.isNotBlank(mimeType) ? mimeType : ContentType.APPLICATION_JSON.getMimeType();

        this.bodyEncoding = bodyEncoding;
    }

    public HttpEntity fromHttpEntity(HttpEntity httpEntity) {
        return httpEntity;
    }

    public HttpEntity fromFile(File bodyContentFile) {
        return new FileEntity(bodyContentFile, ContentType.create(mimeType, bodyEncoding));
    }

    public HttpEntity fromMap(Map<String, String> bodyContentStringMap) {
        List<NameValuePair> parameters = new ArrayList<>();
        for (Map.Entry<String, String> entry : bodyContentStringMap.entrySet()) {
            NameValuePair nameValuePair = new BasicNameValuePair(entry.getKey(), entry.getValue());
            parameters.add(nameValuePair);
        }
        return new UrlEncodedFormEntity(parameters, bodyEncoding);
    }

    public HttpEntity fromMultipart(Map<String, File> bodyContentFileMap, Map<String, String> bodyContentStringMap) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (Map.Entry<String, File> entry : bodyContentFileMap.entrySet()) {
            builder.addBinaryBody(entry.getKey(), entry.getValue(), OCTET_STREAM_UTF_8, entry.getValue().getName());
        }
        for (Map.Entry<String, String> entry : bodyContentStringMap.entrySet()) {
            builder.addTextBody(entry.getKey(), entry.getValue(), TEXT_PLAIN_UTF_8);
        }
        builder.setCharset(StandardCharsets.UTF_8);
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        return builder.build();
    }

    public HttpEntity fromString(String bodyContentString) {
        return new StringEntity(bodyContentString, ContentType.create(mimeType, bodyEncoding));
    }

    public HttpEntity fromObject(Object bodyContentObject) {
        String bodyContentString = gson.toJson(bodyContentObject);
        return fromString(bodyContentString);
    }

}
