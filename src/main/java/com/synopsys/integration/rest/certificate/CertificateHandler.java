/**
 * integration-rest
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
 * under the License.
 */
package com.synopsys.integration.rest.certificate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.ssl.SSLContextBuilder;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.exception.IntegrationCertificateException;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class CertificateHandler {
    public static final String PEER_CERTIFICATES = "PEER_CERTIFICATES";

    private final IntLogger logger;
    private int timeout = 120;
    private ProxyInfo proxyInfo = ProxyInfo.NO_PROXY_INFO;

    private File javaHomeOverride;

    public CertificateHandler(final IntLogger intLogger) {
        logger = intLogger;
    }

    public CertificateHandler(final IntLogger intLogger, final File javaHomeOverride) {
        this(intLogger);
        this.javaHomeOverride = javaHomeOverride;
    }

    public void retrieveAndImportHttpsCertificate(final URL url) throws IntegrationException {
        if (url == null || !url.getProtocol().startsWith("https")) {
            return;
        }
        try {
            final Certificate certificate = retrieveHttpsCertificateFromURL(url);
            if (certificate == null) {
                throw new IntegrationCertificateException(String.format("Could not retrieve the Certificate from %s", url));
            }
            importHttpsCertificate(url, certificate);
        } catch (final IntegrationException e) {
            throw e;
        } catch (final Exception e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public Certificate retrieveHttpsCertificateFromURL(final URL url) throws IntegrationException {
        if (url == null || !url.getProtocol().startsWith("https")) {
            return null;
        }
        logger.info(String.format("Retrieving the certificate from %s", url));

        Certificate certificate = null;
        try {
            final HttpClient client = getHttpClient();
            final RequestBuilder requestBuilder = RequestBuilder.create("GET");
            requestBuilder.setUri(url.toURI());
            final HttpUriRequest request = requestBuilder.build();
            final HttpContext context = new BasicHttpContext();
            client.execute(request, context);
            final Certificate[] peerCertificates = (Certificate[]) context.getAttribute(PEER_CERTIFICATES);

            certificate = peerCertificates[0];
        } catch (final Exception e) {
            throw new IntegrationException(e);
        }
        return certificate;
    }

    protected HttpClient getHttpClient() throws IntegrationException {
        try {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
            final RequestConfig.Builder defaultRequestConfigBuilder = RequestConfig.custom();

            defaultRequestConfigBuilder.setConnectTimeout(timeout);
            defaultRequestConfigBuilder.setSocketTimeout(timeout);
            defaultRequestConfigBuilder.setConnectionRequestTimeout(timeout);

            if (proxyInfo == null) {
                throw new IllegalStateException("The proxy information can not be null.");
            }

            if (proxyInfo.shouldUseProxy()) {
                defaultRequestConfigBuilder.setProxy(new HttpHost(proxyInfo.getHost().orElse(null), proxyInfo.getPort()));
                try {
                    final org.apache.http.auth.Credentials creds = new NTCredentials(proxyInfo.getUsername().orElse(null), proxyInfo.getPassword().orElse(null), proxyInfo.getNtlmWorkstation().orElse(null),
                            proxyInfo.getNtlmDomain().orElse(null));
                    credentialsProvider.setCredentials(new AuthScope(proxyInfo.getHost().orElse(null), proxyInfo.getPort()), creds);
                } catch (final IllegalArgumentException ex) {
                    throw new IntegrationException(ex);
                }
            }
            clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            clientBuilder.setDefaultRequestConfig(defaultRequestConfigBuilder.build());

            final File trustStore = getTrustStore();
            final KeyStore keyStore;
            try {
                keyStore = getKeyStore(trustStore);
            } catch (final Exception e) {
                throw new IntegrationException(e);
            }
            final SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(keyStore, new TrustAllStrategy()).build();
            final HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
            final SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
            clientBuilder.setSSLSocketFactory(connectionFactory);

            final HttpResponseInterceptor certificateInterceptor = (httpResponse, context) -> {
                final ManagedHttpClientConnection routedConnection = (ManagedHttpClientConnection) context.getAttribute(HttpCoreContext.HTTP_CONNECTION);
                final SSLSession sslSession = routedConnection.getSSLSession();
                if (sslSession != null) {
                    // get the server certificates from the {@Link SSLSession}
                    final Certificate[] certificates = sslSession.getPeerCertificates();

                    // add the certificates to the context, where we can later grab it from
                    context.setAttribute(PEER_CERTIFICATES, certificates);
                }
            };
            clientBuilder.addInterceptorLast(certificateInterceptor);
            return clientBuilder.build();
        } catch (final KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public Certificate retrieveHttpsCertificateFromTrustStore(final URL url) throws IntegrationException {
        final File trustStore = getTrustStore();
        final String trustStorePath = trustStore.getAbsolutePath();
        logger.info(String.format("Removing the certificate from %s", trustStorePath));
        try {
            final KeyStore keyStore = getKeyStore(trustStore);
            if (keyStore.containsAlias(url.getHost())) {
                return keyStore.getCertificate(url.getHost());
            }
        } catch (final Exception e) {
            throw new IntegrationException(e);
        }
        return null;
    }

    public void importHttpsCertificate(final URL url, final Certificate certificate) throws IntegrationException {
        final File trustStore = getTrustStore();
        final String trustStorePath = trustStore.getAbsolutePath();
        logger.info(String.format("Importing the certificate from %s into keystore %s", url.getHost(), trustStorePath));
        try {
            final KeyStore keyStore = getKeyStore(trustStore);
            keyStore.setCertificateEntry(url.getHost(), certificate);
            try (final OutputStream stream = new BufferedOutputStream(new FileOutputStream(trustStore))) {
                keyStore.store(stream, getKeyStorePassword());
            }
        } catch (final Exception e) {
            throw new IntegrationException(e);
        }
    }

    public void removeHttpsCertificate(final URL url) throws IntegrationException {
        final File trustStore = getTrustStore();
        final String trustStorePath = trustStore.getAbsolutePath();
        logger.info(String.format("Removing the certificate from %s", trustStorePath));
        try {
            final KeyStore keyStore = getKeyStore(trustStore);
            if (keyStore.containsAlias(url.getHost())) {
                keyStore.deleteEntry(url.getHost());
                try (final OutputStream stream = new BufferedOutputStream(new FileOutputStream(trustStore))) {
                    keyStore.store(stream, getKeyStorePassword());
                }
            }
        } catch (final Exception e) {
            throw new IntegrationException(e);
        }
    }

    public boolean isCertificateInTrustStore(final URL url) throws IntegrationException {
        final File trustStore = getTrustStore();
        if (!trustStore.isFile()) {
            return false;
        }
        final String jssecacertsPath = trustStore.getAbsolutePath();
        logger.info(String.format("Checking for alias %s in keystore %s", url.getHost(), jssecacertsPath));
        try {
            final KeyStore keyStore = getKeyStore(trustStore);
            return keyStore.containsAlias(url.getHost());
        } catch (final Exception e) {
            throw new IntegrationException(e);
        }
    }

    public KeyStore getKeyStore(final File trustStore) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        // trustStore must be an existing file and it must not be empty,
        // otherwise we create a new empty keystore
        if (trustStore.isFile() && trustStore.length() > 0) {
            final PasswordProtection protection = new PasswordProtection(getKeyStorePassword());
            return KeyStore.Builder.newInstance(getTrustStoreType(), null, trustStore, protection).getKeyStore();
        }
        final KeyStore keyStore = KeyStore.getInstance(getTrustStoreType());
        keyStore.load(null, null);
        try (final OutputStream stream = new BufferedOutputStream(new FileOutputStream(trustStore))) {
            // to create a valid empty keystore file
            keyStore.store(stream, getKeyStorePassword());
        }
        return keyStore;
    }

    public File getTrustStore() {
        File trustStore;
        if (javaHomeOverride != null) {
            trustStore = resolveTrustStoreFile(javaHomeOverride);
        } else {
            trustStore = new File(System.getProperty("javax.net.ssl.trustStore", ""));
            if (!trustStore.isFile()) {
                final File javaHome = new File(System.getProperty("java.home"));
                trustStore = resolveTrustStoreFile(javaHome);
            }
        }

        return trustStore;
    }

    private String getTrustStoreType() {
        return System.getProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());
    }

    private char[] getKeyStorePassword() {
        return System.getProperty("javax.net.ssl.trustStorePassword", "changeit").toCharArray();
    }

    private File resolveTrustStoreFile(final File javaHome) {
        // first check for jssecacerts
        File trustStoreFile = new File(javaHome, "lib");
        trustStoreFile = new File(trustStoreFile, "security");
        trustStoreFile = new File(trustStoreFile, "jssecacerts");

        // if we can't find jssecacerts, look for cacerts
        if (!trustStoreFile.isFile()) {
            trustStoreFile = new File(javaHome, "lib");
            trustStoreFile = new File(trustStoreFile, "security");
            trustStoreFile = new File(trustStoreFile, "cacerts");
        }

        return trustStoreFile;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public void setProxyInfo(final ProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;
    }

}
