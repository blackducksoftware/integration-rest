package com.synopsys.integration.rest.it

import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.LogLevel
import com.synopsys.integration.log.PrintStreamIntLogger
import com.synopsys.integration.rest.connection.RestConnection
import com.synopsys.integration.rest.proxy.ProxyInfo
import okhttp3.OkHttpClient
import org.apache.commons.lang3.math.NumberUtils

import java.util.logging.Level
import java.util.logging.Logger

import static org.junit.jupiter.api.Assertions.fail

public class RestConnectionTestHelper {
    private Properties testProperties;

    private final String serverUrl
    private final IntLogger logger = new PrintStreamIntLogger(System.out, DEFAULT_LOGGING_LEVEL)

    private Properties testProperties

    RestConnectionTestHelper() {
        initProperties()
        this.serverUrl = getProperty(TestingPropertyKey.TEST_HTTPS_SERVER_URL)
    }

    RestConnectionTestHelper(final String serverUrlPropertyName) {
        initProperties()
        this.serverUrl = testProperties.getProperty(serverUrlPropertyName)
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

    String getIntegrationServerUrlString() {
        return serverUrl
    }

    URL getIntegrationServerUrl() {
        URL url
        try {
            url = new URL(getIntegrationServerUrlString())
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e)
        }
        return url
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

    RestConnection getRestConnection() {
        return getRestConnection(ProxyInfo.NO_PROXY_INFO)
    }

    RestConnection getRestConnection(ProxyInfo proxyInfo) {
        return new RestConnection(logger, getTimeout(), true, proxyInfo)
    }

}
