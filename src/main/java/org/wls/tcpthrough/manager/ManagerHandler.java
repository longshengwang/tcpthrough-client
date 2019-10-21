package org.wls.tcpthrough.manager;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.Tools;
import org.wls.tcpthrough.data.DataClient;
import org.wls.tcpthrough.model.GlobalObject;
import org.wls.tcpthrough.model.ManagerProtocolBuf.ManagerResponse;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;
import org.wls.tcpthrough.model.RegisterInfo;
import org.wls.tcpthrough.model.ResponseType;

import java.util.ArrayList;
import java.util.List;

public class ManagerHandler extends SimpleChannelInboundHandler<ManagerResponse> {
    public static final Logger LOG = LogManager.getLogger(ManagerHandler.class);

    private RegisterInfo registerInfo;
    private RegisterProtocol registerProtocol;
    private NioEventLoopGroup clientGroup;
    private List<RegisterProtocol> registerProtocolList;

    public ManagerHandler(RegisterProtocol registerProtocol) {
        this.registerProtocol = registerProtocol;
        this.registerProtocolList = new ArrayList<>();
    }

    public void setClientGroup(NioEventLoopGroup clientGroup) {
        this.clientGroup = clientGroup;
    }

    /*
        [写]
        注册协议   protocol code  1个字
                  length         4个字节
                  内容            由length决定

                  protocol—code
        登陆      1   + length
        ------------------------------------------------------
        [读]
        回复协议   protocol code  1个字节
                  channel id     16个字节

                  protocol code
        注册返回   1
        有访问     2  +  channel id
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ManagerResponse msg) {
        if (msg.getType() == ResponseType.REGISTER_RESPONSE.get()) {
            LOG.info("[ REGISTER_RESPONSE ] First register is successful.");
        } else if (msg.getType() == ResponseType.NEW_CONN_RESPONSE.get()) {
            DataClient dataClient;
            String protocolChannelId = msg.getValue();
            String protocolChannelIdMd5 = msg.getValueMd5();
            LOG.info("[ NEW_CONN_RESPONSE ] A new connection is comming! Channel id :" + protocolChannelId);
            if (isResponseValid(protocolChannelId, protocolChannelIdMd5)) {
                String[] mix = protocolChannelId.split(":");
                if (mix.length == 2) {
                    try {
                        String channelId = mix[1];
                        final int remoteProxyPort = Integer.parseInt(mix[0]);
                        if (registerProtocol.getRemoteProxyPort() == remoteProxyPort) {
                            //Origin register protocol from user
                            dataClient = new DataClient(registerProtocol, channelId, clientGroup);
                            dataClient.run();
                            GlobalObject.dataClientMap.put(channelId, dataClient);
                            LOG.info("Connection is finish [ Origin register protocol ]");
                        } else {
                            //Register protocol from server
                            RegisterProtocol findRegister = registerProtocolList.stream()
                                    .filter(rp -> rp.getRemoteProxyPort() == remoteProxyPort)
                                    .findFirst()
                                    .orElse(null);
                            if (findRegister != null) {
                                dataClient = new DataClient(findRegister, channelId, clientGroup);
                                dataClient.run();
                                GlobalObject.dataClientMap.put(channelId, dataClient);
                                LOG.info("Connection is finish [ Register protocol from server ]");
                            } else {
                                LOG.error("Cannot find remote proxy port register! port: " + remoteProxyPort);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("", e);
                    }
                } else {
                    LOG.error("Channel ID is not valid(Right format is 20:xx-xss-x-xxx)! Channel Id:" + protocolChannelId);
                }
            } else {
                LOG.error("Channel Data is not valid!\n " +
                        "    channel id:" + protocolChannelId + "\n" +
                        "    md5:" + protocolChannelIdMd5);
            }
        } else if (msg.getType() == ResponseType.NEW_CONF_RESPONSE.get()) {
            LOG.warn("[ NEW_CONF_RESPONSE ] New Configuration from server is coming!");
            if (!registerProtocol.getIsRemoteManage()) {
                LOG.warn("Client cannot be managed by server!Please check local register protocol(isRemoteManage).");
                return;
            }
            try {
                String mixtureStr = msg.getValue();
                String[] mixArr = mixtureStr.split(",");

                if (mixArr.length == 3) {
                    String local_host;
                    Integer remote_proxy_port, local_port;
                    remote_proxy_port = Integer.parseInt(mixArr[0]);
                    local_host = mixArr[1];
                    local_port = Integer.parseInt(mixArr[2]);
                    LOG.info("The remote configuration is: \n" +
                            "    remote proxy port:" + remote_proxy_port + "\n" +
                            "    local host       :" + local_host + "\n" +
                            "    local port       :" + local_port);

                    RegisterProtocol newRegisterProtocol = RegisterProtocol.newBuilder(Tools.getDefaultRegisterProtocol())
                            .setName(registerProtocol.getName())
                            .setNameMd5(registerProtocol.getNameMd5())
                            .setLocalPort(local_port)
                            .setLocalHost(local_host)
                            .setRemoteProxyPort(remote_proxy_port)
                            .setRemoteManagerPort(registerProtocol.getRemoteManagerPort())
                            .setRemoteDataPort(registerProtocol.getRemoteDataPort())
                            .setIsRemoteManage(registerProtocol.getIsRemoteManage())
                            .setPassword(registerProtocol.getPassword())
                            .build();
                    registerProtocolList.add(newRegisterProtocol);

                    ctx.channel().writeAndFlush(newRegisterProtocol);

                } else {
                    throw new Exception("NEW CONF RESPONSE data is not correct( " + mixtureStr + " )");
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        } else if(ResponseType.DELETE_CONF_RESPONSE.get() == msg.getType()){
            LOG.info("[ DELETE_CONF_RESPONSE ] Server is delete the configuration!");
            String proxyPort = msg.getValue();
            String proxyPortMd5 = msg.getValueMd5();
            try{
                final int proxyPortNum = Integer.parseInt(proxyPort);
                if(isResponseValid(proxyPort , proxyPortMd5)){
                    registerProtocolList.removeIf(rp -> rp.getRemoteProxyPort() == proxyPortNum);
                    LOG.info("[DELETE_CONF_RESPONSE] Proxy port: " + proxyPort + " has been remove from the register list!");
                } else {
                    LOG.error("[DELETE_CONF_RESPONSE] proxy port is not valid ( " + proxyPort + ", " + proxyPortMd5 + ")");
                }
            } catch (Exception e){
                LOG.error("", e);
            }
        } else if(ResponseType.REGISTER_FAIL.get() == msg.getType()){
            LOG.error("Register proxy server error! Error reason:" + msg.getValue());
            this.registerInfo.setRegisterError(true);
            this.registerInfo.setErrorReason(msg.getValue());
            ctx.channel().close();

        }
    }

    private boolean isResponseValid(String value, String valueMd5) {
        String realMd5 = Tools.getMD5(value);
        if (realMd5 != null && realMd5.equals(valueMd5)) {
            return true;
        }
        return false;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("Connect to manager server.And will send the initialize register protocol");
        ctx.channel().writeAndFlush(registerProtocol);
    }

    public void setRegisterInfo(RegisterInfo registerInfo) {
        this.registerInfo = registerInfo;
    }

    public static void main(String[] args) {
//        class A {
//            public String a;
//            public String b;
//
//            public A(String a, String b) {
//                this.a = a;
//                this.b = b;
//            }
//        }
//        List<A> li = new ArrayList<>();
//        li.add(new A("aa", "bb"));
//        System.out.println(li.size());
//        li.removeIf(i-> i.a.equals("aa"));
//        System.out.println(li.size());
//        A xxx = li.stream().filter(x -> x.a.equals("ss")).findFirst().orElse(null);
//        System.out.println(xxx);
//        if (xxx != null) {
//            System.out.println(xxx.a);
//        }
    }
}

