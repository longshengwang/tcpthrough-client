package org.wls.tcpthrough.inner;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.data.DataHandler;
import org.wls.tcpthrough.manager.ManagerHandler;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;

/**
 * Created by wls on 2019/10/16.
 */
public class InnerClient{
    private static final Logger LOG = LogManager.getLogger(InnerClient.class);

    public ChannelFuture channelFuture;
    RegisterProtocol registerProtocol;
    Channel dataChannel;
    DataHandler dataHandler;

    public InnerClient(RegisterProtocol registerProtocol, Channel dataChannel, DataHandler dataHandler) {
        this.registerProtocol = registerProtocol;
        this.dataChannel = dataChannel;
        this.dataHandler = dataHandler;
    }

    public void run() throws InterruptedException {
        try {
            Bootstrap b = new Bootstrap();
            b.group(dataChannel.eventLoop())
                    .channel(dataChannel.getClass())
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new InnerHandler(dataChannel));
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY, true);

            channelFuture = b.connect(registerProtocol.getLocalHost(), registerProtocol.getLocalPort());
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        dataHandler.innerChannel = future.channel();
                        dataChannel.read();
                    } else {
                        LOG.error("Connect to the local service error!");
                        dataChannel.close();
                    }
                }
            });
        } finally {
        }
    }
}
