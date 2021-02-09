/*
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
package com.synopsys.integration.rest.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;

/**
 * All Rest API JSON Responses should be marshaled to instances of this class.
 */
public class IntRestResponse extends IntRestComponent {
    // these are transient to prevent gson serialization
    private transient Gson gson;
    private transient JsonNode patch;

    public boolean hasSubclasses() {
        return false;
    }

    public Class<? extends IntRestResponse> getSubclass() {
        throw new UnsupportedOperationException("A subclass must implement this with its specific behavior");
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(final Gson gson) {
        this.gson = gson;
    }

    public JsonNode getPatch() {
        return patch;
    }

    public void setPatch(final JsonNode patch) {
        this.patch = patch;
    }

}
