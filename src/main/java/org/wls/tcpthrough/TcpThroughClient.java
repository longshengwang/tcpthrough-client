package org.wls.tcpthrough;

import io.netty.channel.nio.NioEventLoopGroup;
import org.wls.tcpthrough.manager.ManagerClient;
import org.wls.tcpthrough.manager.ManagerHandler;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;

/**
 * Created by wls on 2019/10/16.
 */
public class TcpThroughClient {

    private NioEventLoopGroup clientGroup;
    private RegisterProtocol registerProtocol;

    public TcpThroughClient(RegisterProtocol registerProtocol){
        this.registerProtocol = registerProtocol;
        this.clientGroup = new NioEventLoopGroup();

    }

    public void run(){
        try{
            ManagerClient client = new ManagerClient(registerProtocol);
            client.getManagerHandler().setClientGroup(clientGroup);

            client.run();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            clientGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        RegisterProtocol registerProtocol = RegisterProtocol.newBuilder()
                .setName("wls")
                .setIsAuth(false)
                .setIsEncrypt(false)
                .setLocalHost("localhost")
                .setNameMd5(Tools.getMD5("wls"))
                .setLocalPort(5201)
                .setRemoteHost("localhost")
                .setRemoteDataPort(9009)
                .setRemoteManagerPort(9000)
                .setRemoteProxyPort(8002)
                .build();

        TcpThroughClient client = new TcpThroughClient(registerProtocol);
        client.run();
    }

}
