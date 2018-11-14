/**
 * Hub Common Rest
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
 * under the License.*/
package com.synopsys.integration.rest.it

import com.synopsys.integration.exception.IntegrationException
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.LogLevel
import com.synopsys.integration.log.PrintStreamIntLogger
import com.synopsys.integration.rest.certificate.CertificateHandler
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.TempDirectory

import java.nio.file.Path
import java.security.cert.Certificate

import static org.junit.jupiter.api.Assertions.*
import static org.junit.jupiter.api.Assumptions.assumeTrue

/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */

@Tag("integration")
class CertificateHandlerTestIT {
    private static final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.TRACE)

    private static final CertificateHandler CERT_HANDLER = new CertificateHandler(logger, null)
    private static URL url
    private static Certificate originalCertificate

    @BeforeAll
    static void init() throws Exception {
        RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper()
        final String urlString = restConnectionTestHelper.getProperty("TEST_HTTPS_SERVER_URL")
        logger.info("Using Hub server ${urlString}")
        url = new URL(urlString)
        try {
            final boolean isCertificateInKeystore = CERT_HANDLER.isCertificateInTrustStore(url)
            if (isCertificateInKeystore) {
                originalCertificate = CERT_HANDLER.retrieveHttpsCertificateFromTrustStore(url)
                CERT_HANDLER.removeHttpsCertificate(url)
            } else {
                logger.error(String.format("Certificate for %s is not in the keystore.", url.getHost()))
            }
        } catch (final IntegrationException e) {
            logger.error(e.getMessage())
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (originalCertificate != null) {
            CERT_HANDLER.importHttpsCertificate(url, originalCertificate)
        }
    }

    @Test
    void testCertificateRetrieval() throws Exception {
        final CertificateHandler certificateHandler = new CertificateHandler(logger, null)
        certificateHandler.setTimeout(1000)
        final Certificate output = certificateHandler.retrieveHttpsCertificateFromURL(url)
        assertNotNull(output)
    }

    @Test
    void testRetrieveAndImportHttpsCertificate() throws Exception {
        final CertificateHandler certificateHandler = new CertificateHandler(logger, null)
        certificateHandler.setTimeout(1000)
        certificateHandler.retrieveAndImportHttpsCertificate(url)
        assertTrue(certificateHandler.isCertificateInTrustStore(url))
        assertNotNull(certificateHandler.retrieveHttpsCertificateFromTrustStore(url))
        certificateHandler.removeHttpsCertificate(url)
        assertFalse(certificateHandler.isCertificateInTrustStore(url))
    }

    @Test
    @ExtendWith(TempDirectory.class)
    void testKeystoreSetBySystemProperty(@TempDirectory.TempDir Path folder) throws Exception {
        final File tmpTrustStore = folder.resolve("trustStore.tmp").toFile()
        tmpTrustStore.createNewFile()
        assertTrue(tmpTrustStore.length() == 0)
        try {
            System.setProperty("javax.net.ssl.trustStore", tmpTrustStore.getAbsolutePath())
            final CertificateHandler certificateHandler = new CertificateHandler(logger, null)
            certificateHandler.setTimeout(1000)
            certificateHandler.retrieveAndImportHttpsCertificate(url)
            assertTrue(certificateHandler.isCertificateInTrustStore(url))
            assertNotNull(certificateHandler.retrieveHttpsCertificateFromTrustStore(url))
            assertTrue(tmpTrustStore.isFile())
            assertTrue(tmpTrustStore.length() > 0)
        } finally {
            if (tmpTrustStore.exists()) {
                tmpTrustStore.delete()
            }
        }
    }

    @Test
    void testRetrieveAndImportHttpsCertificateForSpecificJavaHome() throws Exception {
        final String javaHomeToManipulate = System.getProperty("JAVA_TO_MANIPULATE")
        assumeTrue(StringUtils.isNotBlank(javaHomeToManipulate))

        final CertificateHandler certificateHandlerDefault = new CertificateHandler(logger, null)
        final CertificateHandler certificateHandler = new CertificateHandler(logger, new File(javaHomeToManipulate))

        Certificate original = null
        if (certificateHandler.isCertificateInTrustStore(url)) {
            original = certificateHandler.retrieveHttpsCertificateFromTrustStore(url)
            certificateHandler.removeHttpsCertificate(url)
        }

        try {
            assertFalse(certificateHandler.isCertificateInTrustStore(url))
            assertFalse(certificateHandlerDefault.isCertificateInTrustStore(url))

            certificateHandler.retrieveAndImportHttpsCertificate(url)

            assertTrue(certificateHandler.isCertificateInTrustStore(url))
            assertFalse(certificateHandlerDefault.isCertificateInTrustStore(url))

            certificateHandler.removeHttpsCertificate(url)
        } finally {
            if (original != null) {
                certificateHandler.importHttpsCertificate(url, original)
            }
        }
    }
}
