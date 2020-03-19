/**
 * integration-rest
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatch;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.component.IntRestComponent;
import com.synopsys.integration.rest.component.IntRestResponse;
import com.synopsys.integration.rest.response.Response;

public class IntJsonTransformer {
    private final Gson gson;
    private final IntLogger logger;

    public IntJsonTransformer(final Gson gson, final IntLogger logger) {
        this.gson = gson;
        this.logger = logger;
    }

    // =============================
    // Basic Response Transformation
    // =============================

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

            // Set the JsonElement of only the root object
            transformedResponse.setJsonElement(jsonObject);
            return transformedResponse;
        } catch (final JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided jsonElement with Gson:%s%s", System.lineSeparator(), json));
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    // Recursively add json as field of every inner object
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

    // ==========
    // Patch Json
    // ==========

    public void setPatch(IntRestResponse intRestResponse, ObjectMapper objectMapper) {
        String lossyJson = gson.toJson(intRestResponse);

        try {
            JsonNode source = objectMapper.readTree(lossyJson);
            JsonNode target = objectMapper.readTree(intRestResponse.getJson());
            JsonNode patch = JsonDiff.asJson(source, target);
            intRestResponse.setPatch(patch);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String producePatchedJson(IntRestResponse intRestResponse, ObjectMapper objectMapper) {
        String lossyJson = gson.toJson(intRestResponse);
        try {
            JsonNode target = objectMapper.readTree(lossyJson);
            JsonNode patch = intRestResponse.getPatch();

            List<JsonNode> listOfPatches = transformPatchToListOfPatches(patch);
            for (JsonNode singleChangePatch : listOfPatches) {
                try {
                    target = JsonPatch.apply(singleChangePatch, target);
                } catch (Exception e) {
                    logger.warn("Could not apply a particular change - this may not be an issue if change involves an object that wasn't being updated: " + e.getMessage());
                }
            }

            StringWriter stringWriter = new StringWriter();
            objectMapper.writeValue(stringWriter, target);

            return stringWriter.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<JsonNode> transformPatchToListOfPatches(JsonNode patch) {
        List<JsonNode> listOfPatches = new ArrayList<>();

        Iterator<JsonNode> patchIterator = patch.iterator();
        while (patchIterator.hasNext()) {
            JsonNode change = patchIterator.next();

            ArrayNode changeArray = JsonNodeFactory.instance.arrayNode(1);
            changeArray.add(change);

            listOfPatches.add(changeArray);
        }

        return listOfPatches;
    }

}
