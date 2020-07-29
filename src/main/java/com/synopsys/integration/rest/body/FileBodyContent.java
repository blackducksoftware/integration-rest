/**
 * integration-rest
 * <p>
 * Copyright (c) 2020 Synopsys, Inc.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.rest.body;

import com.synopsys.integration.rest.request.Request;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

import java.io.File;

public class FileBodyContent implements BodyContent {
    private final File bodyContentFile;

    public FileBodyContent(File bodyContentFile) {
        this.bodyContentFile = bodyContentFile;
    }

    @Override
    public HttpEntity createEntity(Request request) {
        return new FileEntity(getBodyContentFile(), ContentType.create(request.getAcceptMimeType(), request.getBodyEncoding()));
    }

    public File getBodyContentFile() {
        return bodyContentFile;
    }
}
