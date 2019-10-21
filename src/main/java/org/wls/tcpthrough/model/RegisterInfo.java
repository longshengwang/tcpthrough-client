package org.wls.tcpthrough.model;

/**
 * Created by wls on 2019/10/21.
 */
public class RegisterInfo {
    private boolean isRegisterError;
    private String errorReason;

    public boolean isRegisterError() {
        return isRegisterError;
    }

    public void setRegisterError(boolean registerError) {
        isRegisterError = registerError;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }
}
