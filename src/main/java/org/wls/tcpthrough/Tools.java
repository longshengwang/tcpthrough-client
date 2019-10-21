package org.wls.tcpthrough;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by wls on 2019/10/16.
 */
public class Tools {
    public static final Logger LOG = LogManager.getLogger(Tools.class);

    public static String getMD5(String str){
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            LOG.error("Generate MD5 Error", e);
            return null;
        }
    }

    public static RegisterProtocol getDefaultRegisterProtocol(){
        return RegisterProtocol.newBuilder()
                .setIsAuth(false)
                .setIsEncrypt(false)
                .setPrivateKey("")
                .setPublicKey("")
                .setSecretKey("")
                .setPassword("")
                .setName("")
                .setRemoteHost("")
                .setRemoteProxyPort(-1)
                .setRemoteManagerPort(-1)
                .setRemoteDataPort(-1)
                .setLocalPort(-1)
                .setNameMd5("")
                .setSecretKeyMd5("")
                .build();

    }

    public static String protocolToString(RegisterProtocol registerProtocol){
        StringBuilder builder = new StringBuilder();
        builder.append("\nRegisterProtocol:\n")
                .append("    RemoteProxyPort : "  + registerProtocol.getRemoteProxyPort() + "\n")
                .append("    Name : "  + registerProtocol.getName() + "\n")
                .append("    LocalHost : " + registerProtocol.getLocalHost() + "\n")
                .append("    LocalPort : " + registerProtocol.getLocalPort() + "\n")
                .append("    IsRemoteManage : " + registerProtocol.getIsRemoteManage() + "\n")
                .append("    IsAuth : " + registerProtocol.getIsAuth() + "\n")
                .append("    IsEncrypt : " + registerProtocol.getIsEncrypt() + "\n");
        return builder.toString();

    }
}
