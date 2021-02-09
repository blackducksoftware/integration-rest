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

import com.google.gson.JsonElement;
import com.synopsys.integration.util.Stringable;

/**
 * A base class for any object that can interact with a REST API.
 */
public class IntRestComponent extends Stringable {
    public static final String FIELD_NAME_JSON = "json";

    private String json;
    private transient JsonElement jsonElement;

    public String getJson() {
        return json;
    }

    public void setJson(final String json) {
        this.json = json;
    }

    public JsonElement getJsonElement() {
        return jsonElement;
    }

    public void setJsonElement(final JsonElement jsonElement) {
        this.jsonElement = jsonElement;
    }

}
