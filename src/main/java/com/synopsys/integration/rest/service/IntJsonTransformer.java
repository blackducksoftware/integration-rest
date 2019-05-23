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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatch;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.component.IntRestResponse;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.rest.response.PageResponse;
import com.synopsys.integration.rest.response.PageResponseCreator;

public class IntJsonTransformer {
    private final Gson gson;
    private final ObjectMapper objectMapper;
    private final IntLogger logger;

    public IntJsonTransformer(final Gson gson, final ObjectMapper objectMapper, final IntLogger logger) {
        this.gson = gson;
        this.objectMapper = objectMapper;
        this.logger = logger;
    }

    public <T extends IntRestResponse> T getResponse(final Response response, final Class<T> clazz) throws IntegrationException {
        final String json = response.getContentString();
        return getResponseAs(json, clazz);
    }

    public <T extends IntRestResponse> T getResponseAs(final String json, final Class<T> clazz) throws IntegrationException {
        try {
            final JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
            return getResponseAs(jsonElement, clazz);
        } catch (final JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided json with Gson:%s%s", System.lineSeparator(), json));
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public <T extends IntRestResponse> T getResponseAs(final JsonElement jsonElement, final Class<T> clazz) throws IntegrationException {
        final String json = gson.toJson(jsonElement);
        try {
            T restResponse = gson.fromJson(jsonElement, clazz);

            if (restResponse.hasSubclasses()) {
                // when a response can be subclassed, it will use its own state to
                // determine the specific subclass that should be used
                final Class<? extends IntRestResponse> subclass = restResponse.getSubclass();
                final IntRestResponse subclassResponse = gson.fromJson(jsonElement, subclass);
                restResponse = (T) subclassResponse;
            }

            restResponse.setGson(gson);
            restResponse.setJsonElement(jsonElement);
            restResponse.setJson(json);
            setPatch(restResponse);

            return restResponse;
        } catch (final JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided jsonElement with Gson:%s%s", System.lineSeparator(), json));
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public <T extends IntRestResponse> PageResponse<T> getResponses(final String json, final PageResponseCreator<T> createPageResponse, final Class<T> clazz) throws IntegrationException {
        try {
            final JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            final int totalCount = jsonObject.get("totalCount").getAsInt();
            final JsonArray items = jsonObject.get("items").getAsJsonArray();
            final List<T> itemList = new ArrayList<>();
            for (final JsonElement jsonElement : items) {
                itemList.add(getResponseAs(jsonElement, clazz));
            }

            return createPageResponse.apply(totalCount, itemList);
        } catch (final JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided json responses with Gson:%s%s", System.lineSeparator(), json));
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public String producePatchedJson(final IntRestResponse restResponse) {
        final String lossyJson = gson.toJson(restResponse);
        try {
            JsonNode target = objectMapper.readTree(lossyJson);
            final JsonNode patch = restResponse.getPatch();

            final List<JsonNode> listOfPatches = transformPatchToListOfPatches(patch);
            for (final JsonNode singleChangePatch : listOfPatches) {
                try {
                    target = JsonPatch.apply(singleChangePatch, target);
                } catch (final Exception e) {
                    logger.warn("Could not apply a particular change - this may not be an issue if change involves an object that wasn't being updated: " + e.getMessage());
                }
            }

            final StringWriter stringWriter = new StringWriter();
            objectMapper.writeValue(stringWriter, target);

            return stringWriter.toString();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<JsonNode> transformPatchToListOfPatches(final JsonNode patch) {
        final List<JsonNode> listOfPatches = new ArrayList<>();

        final Iterator<JsonNode> patchIterator = patch.iterator();
        while (patchIterator.hasNext()) {
            final JsonNode change = patchIterator.next();

            final ArrayNode changeArray = JsonNodeFactory.instance.arrayNode(1);
            changeArray.add(change);

            listOfPatches.add(changeArray);
        }

        return listOfPatches;
    }

    private void setPatch(final IntRestResponse restResponse) {
        final String lossyJson = gson.toJson(restResponse);

        try {
            final JsonNode source = objectMapper.readTree(lossyJson);
            final JsonNode target = objectMapper.readTree(restResponse.getJson());
            final JsonNode patch = JsonDiff.asJson(source, target);
            restResponse.setPatch(patch);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
