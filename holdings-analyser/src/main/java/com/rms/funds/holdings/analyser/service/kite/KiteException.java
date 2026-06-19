package com.rms.funds.holdings.analyser.service.kite;

public class KiteException extends RuntimeException {

    private final int statusCode;
    private final String kiteStatus;
    private final String errorType;

    public KiteException(int statusCode, String kiteStatus, String errorType, String message) {
        super(message);
        this.statusCode = statusCode;
        this.kiteStatus = kiteStatus;
        this.errorType = errorType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getKiteStatus() {
        return kiteStatus;
    }

    public String getErrorType() {
        return errorType;
    }

    public boolean isSessionExpired() {
        return statusCode == 401;
    }
}
