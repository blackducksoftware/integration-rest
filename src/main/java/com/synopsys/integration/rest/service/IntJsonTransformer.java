/**
 * integration-rest
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.rest.service;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.component.IntRestComponent;
import com.synopsys.integration.rest.component.IntRestResponse;
import com.synopsys.integration.rest.request.Response;

public class IntJsonTransformer {
    private final Gson gson;
    private final IntLogger logger;

    public IntJsonTransformer(final Gson gson, final IntLogger logger) {
        this.gson = gson;
        this.logger = logger;
    }

    public <R extends IntRestResponse> R getResponse(Response response, Type responseType) throws IntegrationException {
        final String json = response.getContentString();
        final R transformedResponse = getComponentAs(json, responseType);
        transformedResponse.setGson(gson);
        return transformedResponse;
    }

    public <C extends IntRestComponent> C getComponentAs(String json, Type responseType) throws IntegrationException {
        try {
            final JsonObject jsonElement = gson.fromJson(json, JsonObject.class);
            return getComponentAs(jsonElement, responseType);
        } catch (final JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided json with Gson:%s%s", System.lineSeparator(), json));
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public <C extends IntRestComponent> C getComponentAs(JsonObject jsonObject, Type responseType) throws IntegrationException {
        final String json = gson.toJson(jsonObject);
        try {
            addJsonAsField(jsonObject);
            C transformedResponse = gson.fromJson(jsonObject, responseType);
            transformedResponse.setJsonElement(jsonObject);

            // TODO find a way to transform JsonPatch field from responses
            return transformedResponse;
        } catch (final JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided jsonElement with Gson:%s%s", System.lineSeparator(), json));
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private void addJsonAsField(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            final JsonObject innerObject = jsonElement.getAsJsonObject();
            final String innerObjectJson = gson.toJson(innerObject);
            innerObject.addProperty(IntRestComponent.FIELD_NAME_JSON, innerObjectJson);
            for (final Map.Entry<String, JsonElement> innerObjectFields : innerObject.entrySet()) {
                addJsonAsField(innerObjectFields.getValue());
            }
        } else if (jsonElement.isJsonArray()) {
            for (final JsonElement arrayElement : jsonElement.getAsJsonArray()) {
                addJsonAsField(arrayElement);
            }
        }
    }

}
