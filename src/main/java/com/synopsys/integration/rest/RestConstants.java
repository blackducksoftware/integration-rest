/**
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.rest;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class RestConstants {
    public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    /* 2XX: generally "OK" */
    public static final int OK_200 = HttpURLConnection.HTTP_OK;
    public static final int CREATED_201 = HttpURLConnection.HTTP_CREATED;
    public static final int ACCEPTED_202 = HttpURLConnection.HTTP_ACCEPTED;
    public static final int NOT_AUTHORITATIVE_203 = HttpURLConnection.HTTP_NOT_AUTHORITATIVE;
    public static final int NO_CONTENT_204 = HttpURLConnection.HTTP_NO_CONTENT;
    public static final int RESET_205 = HttpURLConnection.HTTP_RESET;
    public static final int PARTIAL_206 = HttpURLConnection.HTTP_PARTIAL;

    /* 3XX: relocation/redirect */
    public static final int MULT_CHOICE_300 = HttpURLConnection.HTTP_MULT_CHOICE;
    public static final int MOVED_PERM_301 = HttpURLConnection.HTTP_MOVED_PERM;
    public static final int MOVED_TEMP_302 = HttpURLConnection.HTTP_MOVED_TEMP;
    public static final int SEE_OTHER_303 = HttpURLConnection.HTTP_SEE_OTHER;
    public static final int NOT_MODIFIED_304 = HttpURLConnection.HTTP_NOT_MODIFIED;
    public static final int USE_PROXY_305 = HttpURLConnection.HTTP_USE_PROXY;

    /* 4XX: client error */
    public static final int BAD_REQUEST_400 = HttpURLConnection.HTTP_BAD_REQUEST;
    public static final int UNAUTHORIZED_401 = HttpURLConnection.HTTP_UNAUTHORIZED;
    public static final int PAYMENT_REQUIRED_402 = HttpURLConnection.HTTP_PAYMENT_REQUIRED;
    public static final int FORBIDDEN_403 = HttpURLConnection.HTTP_FORBIDDEN;
    public static final int NOT_FOUND_404 = HttpURLConnection.HTTP_NOT_FOUND;
    public static final int BAD_METHOD_405 = HttpURLConnection.HTTP_BAD_METHOD;
    public static final int NOT_ACCEPTABLE_406 = HttpURLConnection.HTTP_NOT_ACCEPTABLE;
    public static final int PROXY_AUTH_407 = HttpURLConnection.HTTP_PROXY_AUTH;
    public static final int CLIENT_TIMEOUT_408 = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
    public static final int CONFLICT_409 = HttpURLConnection.HTTP_CONFLICT;
    public static final int GONE_410 = HttpURLConnection.HTTP_GONE;
    public static final int LENGTH_REQUIRED_411 = HttpURLConnection.HTTP_LENGTH_REQUIRED;
    public static final int PRECON_FAILED_412 = HttpURLConnection.HTTP_PRECON_FAILED;
    public static final int ENTITY_TOO_LARGE_413 = HttpURLConnection.HTTP_ENTITY_TOO_LARGE;
    public static final int REQ_TOO_LONG_414 = HttpURLConnection.HTTP_REQ_TOO_LONG;
    public static final int UNSUPPORTED_TYPE_415 = HttpURLConnection.HTTP_UNSUPPORTED_TYPE;

    /* 5XX: server error */
    public static final int INTERNAL_ERROR_500 = HttpURLConnection.HTTP_INTERNAL_ERROR;
    public static final int NOT_IMPLEMENTED_501 = HttpURLConnection.HTTP_NOT_IMPLEMENTED;
    public static final int BAD_GATEWAY_502 = HttpURLConnection.HTTP_BAD_GATEWAY;
    public static final int UNAVAILABLE_503 = HttpURLConnection.HTTP_UNAVAILABLE;
    public static final int GATEWAY_TIMEOUT_504 = HttpURLConnection.HTTP_GATEWAY_TIMEOUT;
    public static final int VERSION_505 = HttpURLConnection.HTTP_VERSION;

    public static Date parseDateString(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.parse(dateString);
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

}
