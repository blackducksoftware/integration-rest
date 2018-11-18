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

@Tag("integration")
class CertificateHandlerTestIT {
    private static final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.TRACE)

    private static final CertificateHandler CERT_HANDLER = new CertificateHandler(logger, null)
    private static URL url
    private static Certificate originalCertificate

    @BeforeAll
    public static void init() throws Exception {
        RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper()
        final String urlString = restConnectionTestHelper.getProperty("TEST_HTTPS_SERVER_URL")
        logger.info("Using Black Duck server ${urlString}")
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
    public static void tearDown() throws Exception {
        if (originalCertificate != null) {
            CERT_HANDLER.importHttpsCertificate(url, originalCertificate)
        }
    }

    @Test
    public void testCertificateRetrieval() throws Exception {
        final CertificateHandler certificateHandler = new CertificateHandler(logger, null)
        final Certificate output = certificateHandler.retrieveHttpsCertificateFromURL(url)
        assertNotNull(output)
    }

    @Test
    public void testRetrieveAndImportHttpsCertificate() throws Exception {
        final CertificateHandler certificateHandler = new CertificateHandler(logger, null)
        certificateHandler.retrieveAndImportHttpsCertificate(url)
        assertTrue(certificateHandler.isCertificateInTrustStore(url))
        assertNotNull(certificateHandler.retrieveHttpsCertificateFromTrustStore(url))
        certificateHandler.removeHttpsCertificate(url)
        assertFalse(certificateHandler.isCertificateInTrustStore(url))
    }

    @Test
    @ExtendWith(TempDirectory.class)
    public void testKeystoreSetBySystemProperty(@TempDirectory.TempDir Path folder) throws Exception {
        final File tmpTrustStore = folder.resolve("trustStore.tmp").toFile()
        tmpTrustStore.createNewFile()
        assertTrue(tmpTrustStore.length() == 0)
        try {
            System.setProperty("javax.net.ssl.trustStore", tmpTrustStore.getAbsolutePath())
            final CertificateHandler certificateHandler = new CertificateHandler(logger, null)
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
    public void testRetrieveAndImportHttpsCertificateForSpecificJavaHome() throws Exception {
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
