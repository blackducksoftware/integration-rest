/*
 * integration-rest
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
