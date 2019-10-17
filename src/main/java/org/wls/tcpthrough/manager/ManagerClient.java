package org.wls.tcpthrough.manager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;
import org.wls.tcpthrough.model.ManagerProtocolBuf.ManagerResponse;

import java.util.logging.Logger;

/**
 * Created by wls on 2019/10/15.
 */
public class ManagerClient implements Runnable {

    private static final Logger LOGGER = Logger.getLogger("ManagerClient");
    private RegisterProtocol registerProtocol;
    private ManagerHandler managerHandler;

    public ManagerClient(RegisterProtocol registerProtocol) {
        this.registerProtocol = RegisterProtocol.newBuilder(registerProtocol).build();
        this.managerHandler = new ManagerHandler(registerProtocol);
    }

    public ManagerHandler getManagerHandler(){
        return managerHandler;
    }

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //解码器，通过Google Protocol Buffers序列化框架动态的切割接收到的ByteBuf
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            //将接收到的二进制文件解码成具体的实例，这边接收到的是服务端的ResponseBank对象实列
                            pipeline.addLast(new ProtobufDecoder(ManagerResponse.getDefaultInstance()));
                            //Google Protocol Buffers编码器
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            //Google Protocol Buffers编码器
                            pipeline.addLast(new ProtobufEncoder());

                            pipeline.addLast(managerHandler);
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY, true);

            // Make the connection attempt.
            ChannelFuture channelFuture = b.connect(registerProtocol.getRemoteHost(), registerProtocol.getRemoteManagerPort());
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        LOGGER.info("operation complete");
                    } else {
                        LOGGER.info("operation fail");
                    }
                }
            });
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
//        ManagerClient client = new ManagerClient("localhost", 1099);
//        new Thread(client).start();
    }
}
