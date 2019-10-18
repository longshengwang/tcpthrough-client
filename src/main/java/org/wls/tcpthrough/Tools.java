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
                .setPrivateKey(null)
                .setPublicKey(null)
                .setSecretKey(null)
                .setPassword(null)
                .setName(null)
                .setRemoteHost(null)
                .setRemoteProxyPort(-1)
                .setRemoteManagerPort(-1)
                .setRemoteDataPort(-1)
                .setLocalPort(-1)
                .setNameMd5(null)
                .setSecretKeyMd5(null)
                .build();

    }

    public static String protocolToString(RegisterProtocol registerProtocol){
        StringBuilder builder = new StringBuilder();
        builder.append("\nRegisterProtocol:\n")
                .append("    RemoteProxyPort : "  + registerProtocol.getRemoteProxyPort())
                .append("    Name : "  + registerProtocol.getName())
                .append("    LocalHost : " + registerProtocol.getLocalHost())
                .append("    LocalPort : " + registerProtocol.getLocalPort())
                .append("    IsRemoteManage : " + registerProtocol.getIsRemoteManage())
                .append("    IsAuth : " + registerProtocol.getIsAuth())
                .append("    IsEncrypt : " + registerProtocol.getIsEncrypt())
                .append("\n");
        return builder.toString();

    }
}
