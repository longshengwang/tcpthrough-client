package org.wls.tcpthrough.inner;

import io.netty.channel.*;
import org.wls.tcpthrough.model.ManagerProtocolBuf.ManagerResponse;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;

public class InnerHandler extends ChannelInboundHandlerAdapter {

//    private ChannelFuture dataChannelFuture;
    private Channel dataChannel;
    public InnerHandler(Channel dataChannel){
        this.dataChannel = dataChannel;
    }

//    public void setDataChannelFuture(ChannelFuture dataChannelFuture) {
//        this.dataChannelFuture = dataChannelFuture;
//        dataChannel = this.dataChannelFuture.channel();
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("INNER 有数据产生 =======》》》》》");
        dataChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
//                if(future.isSuccess()){
////                    System.out.println("INNER  is SUCCESS");
//                } else {
////                    System.out.println("INNER  is error");
//                }
            }
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("inner channel active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("INNER channelInactive");
        dataChannel.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("INNER channelReadComplete");
    }
}

