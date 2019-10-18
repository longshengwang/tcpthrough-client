package org.wls.tcpthrough.inner;

import io.netty.channel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InnerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LogManager.getLogger(InnerHandler.class);

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
        dataChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()){
                } else {
                    LOG.error("Send the data to dataServer error!");
                    dataChannel.close();
                    ctx.channel().close();
                }
            }
        });
    }

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("inner channel active");
//    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Inner connection to local service is inactive");
        dataChannel.close();
    }

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//    }
}

