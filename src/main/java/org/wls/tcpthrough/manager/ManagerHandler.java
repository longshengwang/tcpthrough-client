package org.wls.tcpthrough.manager;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import org.wls.tcpthrough.data.DataClient;
import org.wls.tcpthrough.model.GlobalObject;
import org.wls.tcpthrough.model.ManagerProtocolBuf.ManagerResponse;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;
import org.wls.tcpthrough.model.ResponseType;

public class ManagerHandler extends SimpleChannelInboundHandler<ManagerResponse> {

    private String name;
    private RegisterProtocol registerProtocol;
    private NioEventLoopGroup clientGroup;

    public ManagerHandler(RegisterProtocol registerProtocol) {
        this.registerProtocol = registerProtocol;
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
    protected void channelRead0(ChannelHandlerContext ctx, ManagerResponse msg) throws Exception {
        System.out.println(msg.getType());
        System.out.println(msg.getChannel());
        if (msg.getType() == ResponseType.REGISTER_RESPONSE.get()) {
            System.out.println("注册成功");
        } else if (msg.getType() == ResponseType.NEW_CONN_RESPONSE.get()) {
            System.out.println("有新连接过来了");
            String channelId = msg.getChannel();
            DataClient dataClient = new DataClient(registerProtocol, channelId,clientGroup);
            dataClient.run();

            GlobalObject.dataClientMap.put(channelId, dataClient);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client channel active");
//        RegisterProtocol registerProtocol = RegisterProtocol.newBuilder()
//                .setName("client_wls")
//                .setNameMd5(Tools.getMD5("client_wls"))
//                .setIsAuth(false)
//                .build();
        ctx.channel().writeAndFlush(registerProtocol);
    }
}

