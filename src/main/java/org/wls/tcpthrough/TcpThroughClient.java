package org.wls.tcpthrough;

import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.manager.ManagerClient;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;

import java.util.concurrent.TimeUnit;

/**
 * Created by wls on 2019/10/16.
 */
public class TcpThroughClient {

    private static final Logger LOG = LogManager.getLogger(TcpThroughClient.class);

    private NioEventLoopGroup clientGroup;
    private RegisterProtocol registerProtocol;

    public TcpThroughClient(RegisterProtocol registerProtocol){
        this.registerProtocol = registerProtocol;
        this.clientGroup = new NioEventLoopGroup();

    }

    public void run(){
        LOG.info("Tcp Throught client is start");
        try{
            for(;;){
                try{
                    ManagerClient client = new ManagerClient(registerProtocol);
                    client.getManagerHandler().setClientGroup(clientGroup);
                    client.run();
                }catch (Exception e){

                } finally {
                    clientGroup.shutdownGracefully();
                }
                LOG.info("Manage Server may be down. So re-connect after 10 seconds");
                TimeUnit.SECONDS.sleep(10);
                LOG.info("---------------------------------------------------------");
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {

        }
    }

    public static void main(String[] args) throws Exception {
        RegisterProtocol registerProtocol = RegisterProtocol.newBuilder()
                .setName("hcj")
                .setIsAuth(false)
                .setIsEncrypt(false)
                .setLocalHost("localhost")
                .setNameMd5(Tools.getMD5("wls"))
                .setLocalPort(22)
                .setRemoteHost("localhost")
                .setRemoteDataPort(9009)
                .setRemoteManagerPort(9000)
                .setRemoteProxyPort(8003)
                .build();

        TcpThroughClient client = new TcpThroughClient(registerProtocol);
        client.run();
    }

}
