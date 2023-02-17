/*
 * integration-rest
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.rest.exception;

public class IntegrationCertificateException extends IllegalStateException {
    private static final long serialVersionUID = 6323746503432559576L;

    public IntegrationCertificateException() {
        super();
    }

    public IntegrationCertificateException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public IntegrationCertificateException(final String message) {
        super(message);
    }

    public IntegrationCertificateException(final Throwable cause) {
        super(cause);
    }

}
