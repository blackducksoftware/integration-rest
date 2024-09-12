/*
 * integration-rest
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.rest.component;

import com.blackduck.integration.util.Stringable;
import com.google.gson.JsonElement;

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
