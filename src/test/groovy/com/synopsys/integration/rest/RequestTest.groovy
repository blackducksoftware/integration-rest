package com.synopsys.integration.rest

import com.synopsys.integration.rest.request.Request
import org.apache.commons.codec.Charsets
import org.apache.http.entity.ContentType
import org.junit.jupiter.api.Test

import java.nio.charset.Charset

class RequestTest {
    @Test
    public void testRequest() {
        String uri = 'URI'
        Map<String, String> queryParametes = [test: "one", query: "two"]
        String q = 'q'
        HttpMethod method = HttpMethod.DELETE
        String mimeType = 'mime'
        Charset bodyEncoding = Charsets.UTF_8
        Map<String, String> additionalHeaders = [header: "one", thing: "two"]

        Request request = new Request(new Request.Builder())
        assert HttpMethod.GET == request.method
        assert Charsets.UTF_8 == request.bodyEncoding
        assert ContentType.APPLICATION_JSON.getMimeType() == request.mimeType
        assert null == request.uri
        assert null == request.additionalHeaders
        assert request.getPopulatedQueryParameters().isEmpty()

        request = new Request(new Request.Builder(uri))
        assert HttpMethod.GET == request.method
        assert Charsets.UTF_8 == request.bodyEncoding
        assert ContentType.APPLICATION_JSON.getMimeType() == request.mimeType
        assert uri == request.uri
        assert null == request.additionalHeaders
        assert request.getPopulatedQueryParameters().isEmpty()

        request = new Request(null, null, null, null, null, null, null)
        assert null == request.method
        assert null == request.bodyEncoding
        assert null == request.mimeType
        assert null == request.uri
        assert null == request.additionalHeaders
        assert request.getPopulatedQueryParameters().isEmpty()
    }
}
