package org.wls.tcpthrough.model;

/**
 * Created by wls on 2019/10/16.
 */
public class RegisterModel {

//    String name;
//    String password;
//    String remoteProxyPort
//    String isAuth;
//    String isEncrypt ;
//    String privateKey;
//    String secretKey;
//    String nameMd5 ;
//    String secretKeyMd5;
//    String localHost;
//    String localPort;
//    String remoteHost
//    String remoteDataPort;
//    String remoteManagerPort = prop.getProperty("remoteManagerPort");
//    String publicKey = prop.getProperty("publicKey");
//    String isRemoteManage = prop.getProperty("isRemoteManage");

    private boolean isEncrypt; /* */
    private String publicKey;  /* */
    private String privateKey;
    private String secretKey;
    private boolean isAuth;
    private String name;
    private String password;
    private String serverSecretKey;
    private String remotePort;
//    private

    public boolean isEncrypt() {
        return isEncrypt;
    }

    public void setEncrypt(boolean encrypt) {
        isEncrypt = encrypt;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerSecretKey() {
        return serverSecretKey;
    }

    public void setServerSecretKey(String serverSecretKey) {
        this.serverSecretKey = serverSecretKey;
    }

    public String getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(String remotePort) {
        this.remotePort = remotePort;
    }
}
