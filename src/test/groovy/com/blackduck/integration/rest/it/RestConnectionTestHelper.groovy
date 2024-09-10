package com.blackduck.integration.rest.it

import com.google.gson.Gson
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.LogLevel
import com.synopsys.integration.log.PrintStreamIntLogger
import com.blackduck.integration.rest.HttpUrl
import com.blackduck.integration.rest.client.IntHttpClient
import com.blackduck.integration.rest.proxy.ProxyInfo
import okhttp3.OkHttpClient
import org.apache.commons.lang3.math.NumberUtils

import java.util.logging.Level
import java.util.logging.Logger

import static org.junit.jupiter.api.Assertions.fail

class RestConnectionTestHelper {
    public static final LogLevel DEFAULT_LOGGING_LEVEL = LogLevel.TRACE;

    private final HttpUrl serverUrl
    private final IntLogger logger = new PrintStreamIntLogger(System.out, DEFAULT_LOGGING_LEVEL)
    private final Gson gson = new Gson();

    private Properties testProperties

    RestConnectionTestHelper() {
        initProperties()
        this.serverUrl = new HttpUrl(getProperty(TestingPropertyKey.TEST_HTTPS_SERVER_URL))
    }

    RestConnectionTestHelper(final String serverUrlPropertyName) {
        initProperties()
        this.serverUrl = new HttpUrl(testProperties.getProperty(serverUrlPropertyName))
    }

    private void initProperties() {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE)
        testProperties = new Properties()
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
        InputStream is = null
        try {
            is = classLoader.getResourceAsStream("test.properties")
            testProperties.load(is)
        } catch (final Exception ignored) {
            System.err.println("reading test.properties failed!")
        } finally {
            if (is != null) {
                is.close()
            }
        }

        if (testProperties.isEmpty()) {
            try {
                loadOverrideProperties(TestingPropertyKey.values())
            } catch (final Exception ignored) {
                System.err.println("reading properties from the environment failed")
            }
        }
    }

    private void loadOverrideProperties(final TestingPropertyKey[] keys) {
        for (final TestingPropertyKey key : keys) {
            final String prop = System.getenv(key.toString())
            if (prop != null && !prop.isEmpty()) {
                testProperties.setProperty(key.toString(), prop)
            }
        }
    }

    String getProperty(final TestingPropertyKey key) {
        return getProperty(key.toString())
    }

    String getProperty(final String key) {
        return testProperties.getProperty(key)
    }

    HttpUrl getIntegrationServerUrl() {
        return serverUrl
    }

    int getTimeout() {
        int timeout = NumberUtils.toInt(getProperty(TestingPropertyKey.TEST_TIMEOUT), 300)
        return timeout
    }

    File getFile(final String classpathResource) {
        try {
            final URL url = Thread.currentThread().getContextClassLoader().getResource(classpathResource)
            final File file = new File(url.toURI().getPath())
            return file
        } catch (final Exception e) {
            fail("Could not get file: " + e.getMessage())
            return null
        }
    }

    IntHttpClient getRestConnection() {
        return getRestConnection(ProxyInfo.NO_PROXY_INFO)
    }

    IntHttpClient getRestConnection(ProxyInfo proxyInfo) {
        return new IntHttpClient(logger, gson, getTimeout(), true, proxyInfo)
    }

}
