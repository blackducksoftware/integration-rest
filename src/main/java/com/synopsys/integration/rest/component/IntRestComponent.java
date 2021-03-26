/*
 * integration-rest
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
