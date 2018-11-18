package com.synopsys.integration.rest

import com.synopsys.integration.exception.IntegrationException
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.LogLevel
import com.synopsys.integration.log.PrintStreamIntLogger
import com.synopsys.integration.rest.connection.RestConnection
import com.synopsys.integration.rest.exception.IntegrationRestException
import com.synopsys.integration.rest.proxy.ProxyInfo
import com.synopsys.integration.rest.request.Request
import com.synopsys.integration.rest.request.Response
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.apache.commons.codec.Charsets
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.ContentType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.charset.Charset

import static org.junit.jupiter.api.Assertions.fail

class RestConnectionTest {
    public static final int CONNECTION_TIMEOUT = 213

    private final MockWebServer server = new MockWebServer()
    private final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG)

    @BeforeEach
    void setUp() throws Exception {
        server.start()
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown()
    }

    private String getValidUri() {
        return server.url("www.synopsys.com").uri()
    }

    private RestConnection getRestConnection() {
        getRestConnection(new MockResponse().setResponseCode(200))
    }

    private RestConnection getRestConnection(MockResponse response) {
        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                response
            }
        }
        server.setDispatcher(dispatcher)

        return new RestConnection(logger, CONNECTION_TIMEOUT, false, ProxyInfo.NO_PROXY_INFO)
    }

    @Test
    void testRestConnectionNoProxy() {
        int timeoutSeconds = 213

        try {
            RestConnection restConnection = new RestConnection(logger, timeoutSeconds, true, null)
            restConnection.initialize()
            fail('Should have thrown exception')
        } catch (IllegalArgumentException e) {
            assert RestConnection.ERROR_MSG_PROXY_INFO_NULL == e.getMessage()
        }
    }

    @Test
    void testHandleExecuteClientCallSuccessful() {
        RestConnection restConnection = getRestConnection()
        restConnection.commonRequestHeaders.put("Common", "Header")
        RequestBuilder requestBuilder = restConnection.createRequestBuilder(HttpMethod.DELETE)
        requestBuilder.setUri(getValidUri())
        assert null != requestBuilder.getHeaders("Common")

        Response response = restConnection.executeRequest(requestBuilder.build())

        assert 200 == response.getStatusCode()
    }

    @Test
    void testHandleExecuteClientCallFail() {
        RestConnection restConnection = getRestConnection()
        RequestBuilder requestBuilder = restConnection.createRequestBuilder(HttpMethod.GET)
        requestBuilder.setUri(getValidUri())
        HttpUriRequest request = requestBuilder.build()
        restConnection.initialize()

        restConnection = getRestConnection(new MockResponse().setResponseCode(404))
        try {
            final Response response = restConnection.executeRequest(request)
            assert 404 == response.getStatusCode()
        } catch (IntegrationRestException e) {
            fail('Should NOT have thrown exception')
        }

        restConnection = getRestConnection(new MockResponse().setResponseCode(401))
        try {
            final Response response = restConnection.executeRequest(request)
            assert 401 == response.getStatusCode()
        } catch (IntegrationRestException e) {
            fail('Should NOT have thrown exception')
        }
    }

    @Test
    void testHandleExecuteWithExceptionClientCallFail() {
        RestConnection restConnection = getRestConnection()
        RequestBuilder requestBuilder = restConnection.createRequestBuilder(HttpMethod.GET)
        requestBuilder.setUri(getValidUri())
        HttpUriRequest request = requestBuilder.build()
        restConnection.initialize()

        restConnection = getRestConnection(new MockResponse().setResponseCode(404))
        try {
            restConnection.executeRequestWithException(request)
            fail('Should have thrown exception')
        } catch (IntegrationRestException e) {
            assert 404 == e.httpStatusCode
        }

        restConnection = getRestConnection(new MockResponse().setResponseCode(401))
        try {
            restConnection.executeRequestWithException(request)
            fail('Should have thrown exception')
        } catch (IntegrationRestException e) {
            assert 401 == e.httpStatusCode
        }
    }

    @Test
    void testCreateHttpRequestNoURI() {
        RestConnection restConnection = new RestConnection(logger, 300, true, ProxyInfo.NO_PROXY_INFO)
        Request request = new Request.Builder().build()
        try {
            request.createHttpUriRequest(restConnection.getCommonRequestHeaders())
            fail('Should have thrown exception')
        } catch (IntegrationException e) {
            assert "Missing the URI" == e.getMessage()
        }
    }

    @Test
    void testCreateHttpRequest() {
        RestConnection restConnection = getRestConnection()

        final String uri = getValidUri()
        Map<String, String> queryParametes = [test: "one", query: "two"]
        String q = 'q'
        String mimeType = 'mime'
        Charset bodyEncoding = Charsets.UTF_8

        Request request = new Request.Builder(uri).build()
        HttpUriRequest uriRequest = request.createHttpUriRequest(restConnection.getCommonRequestHeaders())
        assert HttpMethod.GET.name() == uriRequest.method
        assert ContentType.APPLICATION_JSON.getMimeType() == uriRequest.getFirstHeader(HttpHeaders.ACCEPT).getValue()
        assert null != uriRequest.getURI()
        assert uriRequest.getURI().toString().contains(uri)

        request = new Request.Builder(uri).build()
        uriRequest = request.createHttpUriRequest(restConnection.getCommonRequestHeaders())
        assert HttpMethod.GET.name() == uriRequest.method
        assert ContentType.APPLICATION_JSON.getMimeType() == uriRequest.getFirstHeader(HttpHeaders.ACCEPT).getValue()
        assert null != uriRequest.getURI()
        assert uriRequest.getURI().toString().contains(uri)

        request = new Request.Builder(uri).queryParameters([offset: ['0'] as Set, limit: ['100'] as Set]).build()
        uriRequest = request.createHttpUriRequest(restConnection.getCommonRequestHeaders())
        assert HttpMethod.GET.name() == uriRequest.method
        assert ContentType.APPLICATION_JSON.getMimeType() == uriRequest.getFirstHeader(HttpHeaders.ACCEPT).getValue()
        assert null != uriRequest.getURI()
        assert uriRequest.getURI().toString().contains(uri)
        assert uriRequest.getURI().toString().contains('offset=0')
        assert uriRequest.getURI().toString().contains('limit=100')

        request = new Request.Builder(uri).queryParameters([q: ['q'] as Set, test: ['one'] as Set, query: ['two'] as Set, offset: ['0'] as Set, limit: ['100'] as Set]).mimeType('mime').additionalHeaders([header: 'one', thing: 'two']).
            build()
        uriRequest = request.createHttpUriRequest(restConnection.getCommonRequestHeaders())
        assert HttpMethod.GET.name() == uriRequest.method
        assert 'one' == uriRequest.getFirstHeader('header').getValue()
        assert 'two' == uriRequest.getFirstHeader('thing').getValue()
        assert null != uriRequest.getURI()
        assert uriRequest.getURI().toString().contains(uri)
        assert uriRequest.getURI().toString().contains('offset=0')
        assert uriRequest.getURI().toString().contains('limit=100')

        Map headersMap = [header: 'one', thing: 'two']
        headersMap.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.getMimeType())
        request = new Request.Builder(uri).queryParameters([q: ['q'] as Set, test: ['one'] as Set, query: ['two'] as Set, offset: ['0'] as Set, limit: ['100'] as Set]).mimeType('mime').bodyEncoding(bodyEncoding).
            additionalHeaders(headersMap).build()
        uriRequest = request.createHttpUriRequest(restConnection.getCommonRequestHeaders())
        assert HttpMethod.GET.name() == uriRequest.method
        assert ContentType.APPLICATION_XML.getMimeType() == uriRequest.getFirstHeader(HttpHeaders.ACCEPT).getValue()
        assert null != uriRequest.getURI()
        assert uriRequest.getURI().toString().contains(uri)
        assert uriRequest.getURI().toString().contains('offset=0')
        assert uriRequest.getURI().toString().contains('limit=100')

        Request deleteRequest = new Request.Builder(uri).method(HttpMethod.DELETE).mimeType('mime').bodyEncoding(bodyEncoding).additionalHeaders([header: 'one', thing: 'two']).build()
        uriRequest = deleteRequest.createHttpUriRequest(restConnection.getCommonRequestHeaders())
        assert HttpMethod.DELETE.name() == uriRequest.method
        assert 'one' == uriRequest.getFirstHeader('header').getValue()
        assert 'two' == uriRequest.getFirstHeader('thing').getValue()
        assert 2 == uriRequest.getAllHeaders().size()
        assert null != uriRequest.getURI()
        assert uriRequest.getURI().toString().contains(uri)
    }

}
