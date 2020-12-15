package com.synopsys.integration.rest

import com.synopsys.integration.rest.response.DefaultResponse
import com.synopsys.integration.rest.response.Response
import org.apache.commons.codec.Charsets
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.ProtocolVersion
import org.apache.http.StatusLine
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicStatusLine
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class ResponseTest {
    @Test
    void testGetStatusCode() throws Exception {
        HttpUriRequest httpUriRequest = [] as HttpUriRequest
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("test", 1, 0), 200, "Everything went well")
        CloseableHttpClient closeableHttpClient = [close: {}] as CloseableHttpClient
        CloseableHttpResponse closeableHttpResponse = [getStatusLine: { -> return statusLine }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertEquals(200, response.getStatusCode())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    void testGetStatusMessage() throws Exception {
        HttpUriRequest httpUriRequest = [] as HttpUriRequest
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("test", 1, 0), 200, "Everything went well")
        CloseableHttpClient closeableHttpClient = [close: {}] as CloseableHttpClient
        CloseableHttpResponse closeableHttpResponse = [getStatusLine: { -> return statusLine }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertEquals("Everything went well", response.getStatusMessage())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    void testGetContent() throws Exception {
        HttpUriRequest httpUriRequest = [] as HttpUriRequest
        CloseableHttpClient closeableHttpClient = [close: {}] as CloseableHttpClient
        CloseableHttpResponse closeableHttpResponse = [getEntity: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertNull(response.getContent())
        } finally {
            if (response != null) {
                response.close()
            }
        }
        InputStream stream = new ByteArrayInputStream()
        HttpEntity entity = [getContent: { return stream }] as HttpEntity
        closeableHttpClient = [close: {}] as CloseableHttpClient
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertEquals(stream, response.getContent())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    void testGetContentString() throws Exception {
        HttpUriRequest httpUriRequest = [] as HttpUriRequest
        CloseableHttpClient closeableHttpClient = [close: {}] as CloseableHttpClient
        CloseableHttpResponse closeableHttpResponse = [getEntity: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertNull(response.getContentString())
        } finally {
            if (response != null) {
                response.close()
            }
        }
        String expectedString = 'Test String'
        InputStream stream = new ByteArrayInputStream(expectedString.bytes)
        HttpEntity entity = [getContent: { return stream }] as HttpEntity
        closeableHttpClient = [close: {}] as CloseableHttpClient
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse

        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertEquals(expectedString, response.getContentString())
        } finally {
            if (response != null) {
                response.close()
            }
        }

        stream = new ByteArrayInputStream(expectedString.bytes)
        entity = [getContent: { return stream }] as HttpEntity
        closeableHttpClient = [close: {}] as CloseableHttpClient
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertEquals(expectedString, response.getContentString(Charsets.UTF_8))
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    void testGetContentLength() throws Exception {
        HttpUriRequest httpUriRequest = [] as HttpUriRequest
        CloseableHttpClient closeableHttpClient = [close: {}] as CloseableHttpClient
        CloseableHttpResponse closeableHttpResponse = [getEntity: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertNull(response.getContentLength())
        } finally {
            if (response != null) {
                response.close()
            }
        }
        HttpEntity entity = [getContentLength: { return 11L }] as HttpEntity
        closeableHttpClient = [close: {}] as CloseableHttpClient
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertEquals(11L, response.getContentLength())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    void testGetContentEncoding() throws Exception {
        HttpUriRequest httpUriRequest = [] as HttpUriRequest
        CloseableHttpClient closeableHttpClient = [close: {}] as CloseableHttpClient
        CloseableHttpResponse closeableHttpResponse = [getEntity: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertNull(response.getContentEncoding())
        } finally {
            if (response != null) {
                response.close()
            }
        }
        HttpEntity entity = [getContentEncoding: { return null }] as HttpEntity
        closeableHttpClient = [close: {}] as CloseableHttpClient
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertNull(response.getContentEncoding())
        } finally {
            if (response != null) {
                response.close()
            }
        }

        entity = [getContentEncoding: { return new BasicHeader("TestName", "Value") }] as HttpEntity
        closeableHttpClient = [close: {}] as CloseableHttpClient
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertEquals("Value", response.getContentEncoding())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    void testGetContentType() throws Exception {
        HttpUriRequest httpUriRequest = [] as HttpUriRequest
        CloseableHttpClient closeableHttpClient = [close: {}] as CloseableHttpClient
        CloseableHttpResponse closeableHttpResponse = [getEntity: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertNull(response.getContentType())
        } finally {
            if (response != null) {
                response.close()
            }
        }
        HttpEntity entity = [getContentType: { return null }] as HttpEntity
        closeableHttpClient = [close: {}] as CloseableHttpClient
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertNull(response.getContentType())
        } finally {
            if (response != null) {
                response.close()
            }
        }

        entity = [getContentType: { return new BasicHeader("TestName", "Value") }] as HttpEntity
        closeableHttpClient = [close: {}] as CloseableHttpClient
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertEquals("Value", response.getContentType())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    void testGetHeaders() throws Exception {
        HttpUriRequest httpUriRequest = [] as HttpUriRequest
        CloseableHttpClient closeableHttpClient = [close: {}] as CloseableHttpClient
        CloseableHttpResponse closeableHttpResponse = [containsHeader: { return false }, getAllHeaders: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertEquals(Collections.emptyMap(), response.getHeaders())
            assertNull(response.getHeaderValue("TestName"))
        } finally {
            if (response != null) {
                response.close()
            }
        }

        Header[] headers = new Header[1]
        headers[0] = new BasicHeader("TestName", "Value")
        closeableHttpClient = [close: {}] as CloseableHttpClient
        closeableHttpResponse = [getFirstHeader: { return headers[0] }, containsHeader: { return true }, getAllHeaders: { return headers }, close: {}] as CloseableHttpResponse
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertTrue(!response.getHeaders().isEmpty())
            assertEquals("Value", response.getHeaderValue("TestName"))
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    void testGetActualResponse() throws Exception {
        HttpUriRequest httpUriRequest = [] as HttpUriRequest
        CloseableHttpClient closeableHttpClient = [close: {}] as CloseableHttpClient
        CloseableHttpResponse closeableHttpResponse = [close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new DefaultResponse(httpUriRequest, closeableHttpClient, closeableHttpResponse)
            assertEquals(closeableHttpResponse, response.getActualResponse())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

}
