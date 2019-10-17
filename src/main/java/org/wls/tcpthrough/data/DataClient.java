package org.wls.tcpthrough.data;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.wls.tcpthrough.inner.InnerClient;
import org.wls.tcpthrough.inner.InnerHandler;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;

/**
 * Created by wls on 2019/10/16.
 */
public class DataClient implements Runnable{
    RegisterProtocol registerProtocol;
//    DataHandler handler;
    public ChannelFuture channelFuture;
    private EventLoopGroup eventLoopGroup;
    private String channelId;

    public DataClient(RegisterProtocol registerProtocol, String channelId, EventLoopGroup eventLoopGroup){
        this.registerProtocol = registerProtocol;
        this.channelId = channelId;
        this.eventLoopGroup = eventLoopGroup;
    }

    @Override
    public void run() {
        try {
            Bootstrap b = new Bootstrap();
//            handler = new DataHandler();
//            handler.setChannelId(channelId);

            b.group(this.eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new DataHandler(channelId, registerProtocol));
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.AUTO_READ, false);

            channelFuture = b.connect(registerProtocol.getRemoteHost(), registerProtocol.getRemoteDataPort());
//            handler.setChannelFuture(channelFuture);
            channelFuture.addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {

                } else {
                }
            });
//            channelFuture.channel().closeFuture().sync();
        } finally {

        }
    }
}
