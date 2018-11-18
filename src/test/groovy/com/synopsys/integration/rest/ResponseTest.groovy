package com.synopsys.integration.rest

import com.synopsys.integration.rest.request.Response
import org.apache.commons.codec.Charsets
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.ProtocolVersion
import org.apache.http.StatusLine
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicStatusLine
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class ResponseTest {
    @Test
    public void testGetStatusCode() throws Exception {
        CloseableHttpResponse closeableHttpResponse = [getStatusLine: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new Response(closeableHttpResponse)
            assertNull(response.getStatusCode())
        } finally {
            if (response != null) {
                response.close()
            }
        }
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("test", 1, 0), 200, "Everything went well")
        closeableHttpResponse = [getStatusLine: { -> return statusLine }, close: {}] as CloseableHttpResponse
        try {
            response = new Response(closeableHttpResponse)
            assertEquals(200, response.getStatusCode())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    public void testGetStatusMessage() throws Exception {
        CloseableHttpResponse closeableHttpResponse = [getStatusLine: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new Response(closeableHttpResponse)
            assertNull(response.getStatusMessage())
        } finally {
            if (response != null) {
                response.close()
            }
        }
        StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("test", 1, 0), 200, "Everything went well")
        closeableHttpResponse = [getStatusLine: { -> return statusLine }, close: {}] as CloseableHttpResponse
        try {
            response = new Response(closeableHttpResponse)
            assertEquals("Everything went well", response.getStatusMessage())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    public void testGetContent() throws Exception {
        CloseableHttpResponse closeableHttpResponse = [getEntity: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new Response(closeableHttpResponse)
            assertNull(response.getContent())
        } finally {
            if (response != null) {
                response.close()
            }
        }
        InputStream stream = new ByteArrayInputStream();
        HttpEntity entity = [getContent: { return stream }] as HttpEntity
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new Response(closeableHttpResponse)
            assertEquals(stream, response.getContent())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    public void testGetContentString() throws Exception {
        CloseableHttpResponse closeableHttpResponse = [getEntity: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new Response(closeableHttpResponse)
            assertNull(response.getContentString())
        } finally {
            if (response != null) {
                response.close()
            }
        }
        String expectedString = 'Test String'
        InputStream stream = new ByteArrayInputStream(expectedString.bytes)
        HttpEntity entity = [getContent: { return stream }] as HttpEntity
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse

        try {
            response = new Response(closeableHttpResponse)
            assertEquals(expectedString, response.getContentString())
        } finally {
            if (response != null) {
                response.close()
            }
        }

        stream = new ByteArrayInputStream(expectedString.bytes)
        entity = [getContent: { return stream }] as HttpEntity
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new Response(closeableHttpResponse)
            assertEquals(expectedString, response.getContentString(Charsets.UTF_8))
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    public void testGetContentLength() throws Exception {
        CloseableHttpResponse closeableHttpResponse = [getEntity: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new Response(closeableHttpResponse)
            assertNull(response.getContentLength())
        } finally {
            if (response != null) {
                response.close()
            }
        }
        HttpEntity entity = [getContentLength: { return 11L }] as HttpEntity
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new Response(closeableHttpResponse)
            assertEquals(11L, response.getContentLength())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    public void testGetContentEncoding() throws Exception {
        CloseableHttpResponse closeableHttpResponse = [getEntity: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new Response(closeableHttpResponse)
            assertNull(response.getContentEncoding())
        } finally {
            if (response != null) {
                response.close()
            }
        }
        HttpEntity entity = [getContentEncoding: { return null }] as HttpEntity
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new Response(closeableHttpResponse)
            assertNull(response.getContentEncoding())
        } finally {
            if (response != null) {
                response.close()
            }
        }

        entity = [getContentEncoding: { return new BasicHeader("TestName", "Value") }] as HttpEntity
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new Response(closeableHttpResponse)
            assertEquals("Value", response.getContentEncoding())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    public void testGetContentType() throws Exception {
        CloseableHttpResponse closeableHttpResponse = [getEntity: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new Response(closeableHttpResponse)
            assertNull(response.getContentType())
        } finally {
            if (response != null) {
                response.close()
            }
        }
        HttpEntity entity = [getContentType: { return null }] as HttpEntity
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new Response(closeableHttpResponse)
            assertNull(response.getContentType())
        } finally {
            if (response != null) {
                response.close()
            }
        }

        entity = [getContentType: { return new BasicHeader("TestName", "Value") }] as HttpEntity
        closeableHttpResponse = [getEntity: { return entity }, close: {}] as CloseableHttpResponse
        try {
            response = new Response(closeableHttpResponse)
            assertEquals("Value", response.getContentType())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    @Test
    public void testGetHeaders() throws Exception {
        CloseableHttpResponse closeableHttpResponse = [containsHeader: { return false }, getAllHeaders: { return null }, close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new Response(closeableHttpResponse)
            assertEquals(Collections.emptyMap(), response.getHeaders())
            assertNull(response.getHeaderValue("TestName"))
        } finally {
            if (response != null) {
                response.close()
            }
        }

        Header[] headers = new Header[1]
        headers[0] = new BasicHeader("TestName", "Value")
        closeableHttpResponse = [getFirstHeader: { return headers[0] }, containsHeader: { return true }, getAllHeaders: { return headers }, close: {}] as CloseableHttpResponse
        try {
            response = new Response(closeableHttpResponse)
            assertTrue(!response.getHeaders().isEmpty())
            assertEquals("Value", response.getHeaderValue("TestName"))
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }


    @Test
    public void testGetActualResponse() throws Exception {
        CloseableHttpResponse closeableHttpResponse = [close: {}] as CloseableHttpResponse
        Response response = null
        try {
            response = new Response(closeableHttpResponse)
            assertEquals(closeableHttpResponse, response.getActualResponse())
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }
}
