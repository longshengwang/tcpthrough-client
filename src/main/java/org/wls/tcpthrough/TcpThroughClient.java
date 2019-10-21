package org.wls.tcpthrough;

import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.manager.ManagerClient;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;
import org.wls.tcpthrough.model.RegisterException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by wls on 2019/10/16.
 */
public class TcpThroughClient {

    private static HelpFormatter formatter;
    private static Options options;
    private static final String CLIENT_CMD_STR = "tcpthrough-client [-f configFilePath]";
    private static final String EMPTY_STR = "";

    private static final Logger LOG = LogManager.getLogger(TcpThroughClient.class);

    private NioEventLoopGroup clientGroup;
    private RegisterProtocol registerProtocol;

    public TcpThroughClient(RegisterProtocol registerProtocol) {
        this.registerProtocol = registerProtocol;
    }

    public void run() {
        LOG.info("Tcp Throught client is start");
        ManagerClient client;
        try {
            while (true) {
                clientGroup = new NioEventLoopGroup();
                client = null;
                try {
                    client = new ManagerClient(registerProtocol);
                    client.getManagerHandler().setClientGroup(clientGroup);
                    client.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    clientGroup.shutdownGracefully();
                }
                if (client != null && client.registerInfo.isRegisterError()) {
                    throw new RegisterException(client.registerInfo.getErrorReason());
                }

                LOG.info("Manage Server may be down. So re-connect after 10 seconds");
                TimeUnit.SECONDS.sleep(10);
                LOG.info("---------------------------------------------------------");
            }
        } catch (RegisterException e) {
            LOG.error("Register Error, The client will be closed! Close reason : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toUpperCase().contains("WINDOWS");
    }

    public static String getDefaultPath(String userHomeDir) {
        return userHomeDir + "/.tcpthrough.properties";
    }

    public static String tranArgsStr(String o) {
        return o == null ? EMPTY_STR : o;
    }

    public static boolean tranArgsBool(String o) {
        return o != null && Boolean.parseBoolean(o);
    }

    public static int tranArgsInt(String o) {
        return o == null ? -1 : Integer.parseInt(o);
    }


    public static RegisterProtocol loadFromPro(Properties prop) {
        String name = prop.getProperty("name");
        String password = prop.getProperty("password");
        String remoteProxyPort = prop.getProperty("remote_proxy_port");
        String isAuth = prop.getProperty("is_auth");
        String isEncrypt = prop.getProperty("is_encrypt");
        String privateKey = prop.getProperty("private_key");
        String localHost = prop.getProperty("local_host");
        String localPort = prop.getProperty("local_port");
        String remoteHost = prop.getProperty("remote_host");
        String remoteDataPort = prop.getProperty("remote_data_port");
        String remoteManagerPort = prop.getProperty("remote_manager_port");
        String publicKey = prop.getProperty("public_key");
        String isRemoteManage = prop.getProperty("is_remote_manage");
        return RegisterProtocol.newBuilder()
                .setName(tranArgsStr(name))
                .setPassword(tranArgsStr(password))
                .setRemoteProxyPort(tranArgsInt(remoteProxyPort))
                .setRemoteDataPort(tranArgsInt(remoteDataPort))
                .setRemoteManagerPort(tranArgsInt(remoteManagerPort))
                .setLocalPort(tranArgsInt(localPort))
                .setIsAuth(tranArgsBool(isAuth))
                .setIsEncrypt(tranArgsBool(isEncrypt))
                .setIsRemoteManage(tranArgsBool(isRemoteManage))
                .setRemoteHost(tranArgsStr(remoteHost))
                .setLocalHost(tranArgsStr(localHost))
                .setPublicKey(tranArgsStr(publicKey))
                .setPrivateKey(tranArgsStr(privateKey))
                .build();
    }

    public static RegisterProtocol loadFromFilePath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            LOG.error("Cannot find the file : " + path);
            return null;
        } else {
            try (InputStream inputStream = new FileInputStream(file);) {
                Properties prop = new Properties();
                prop.load(inputStream);
                return loadFromPro(prop);
            } catch (Exception e) {
                LOG.error("", e);
                return null;
            }
        }
    }

    public static RegisterProtocol argsParse(String[] args) {
        options = new Options();
        options.addRequiredOption("f", "file", true,
                "The path of config file! Default path is .tcpthrough.properties in the user.home");
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("show this help message and exit program")
                .build());

        CommandLineParser parser = new DefaultParser();
        formatter = new HelpFormatter();

        if (args.length == 0) {
            String userHomeDir = System.getProperty("user.home");
            return loadFromFilePath(getDefaultPath(userHomeDir));
        } else {;
            CommandLine cmd = null;
            try {
                cmd = parser.parse(options, args);
            } catch (ParseException e) {
                return null;
            }
            if (cmd.hasOption('h') || cmd.hasOption("--help")) {
                return null;
            }

            String filePath = cmd.getOptionValue("f");
            return loadFromFilePath(getDefaultPath(filePath));
        }
    }

    public static void main(String[] args) throws Exception {

        RegisterProtocol registerProtocol = argsParse(args);
        if (registerProtocol == null) {
            formatter.printHelp(CLIENT_CMD_STR, options, false);
            return;
        }

        if (registerProtocol.getName().equals(EMPTY_STR)) {
            System.out.println("Option Required: Name cannot be empty");
            formatter.printHelp(CLIENT_CMD_STR, options, false);
            return;
        }

        if (registerProtocol.getRemoteDataPort() == -1) {
            System.out.println("Option Required: Remote data port cannot be empty");
            formatter.printHelp(CLIENT_CMD_STR, options, false);
            return;
        }


        if (registerProtocol.getRemoteManagerPort() == -1) {
            System.out.println("Option Required: Remote manage port cannot be empty");
            formatter.printHelp(CLIENT_CMD_STR, options, false);
            return;
        }


        if (registerProtocol.getRemoteHost().equals(EMPTY_STR)) {
            System.out.println("Option Required: Remote host name cannot be empty");
            formatter.printHelp(CLIENT_CMD_STR, options, false);
            return;
        }

//        System.out.println(registerProtocol.getIsRemoteManage());
        if(registerProtocol.getLocalHost().equals(EMPTY_STR)
                        || registerProtocol.getLocalPort() == -1
                        || registerProtocol.getRemoteProxyPort() == -1) {
            System.out.println("Option Error. Local setting('remote proxy port'/'local host'/'local port') is need!");
            formatter.printHelp(CLIENT_CMD_STR, options, false);
            return;
        }

        LOG.info("========================================");
        LOG.info(Tools.protocolToString(registerProtocol));
        LOG.info("========================================");

//        RegisterProtocol registerProtocol1 = RegisterProtocol.newBuilder()
//                .setName("hcj1")
//                .setIsAuth(false)
//                .setIsEncrypt(false)
//                .setLocalHost("localhost")
//                .setNameMd5(Tools.getMD5("wls"))
//                .setLocalPort(22)
//                .setRemoteHost("localhost")
//                .setIsRemoteManage(true)
//                .setRemoteDataPort(9009)
//                .setRemoteManagerPort(9000)
//                .setRemoteProxyPort(8002)
//                .build();

        TcpThroughClient client = new TcpThroughClient(registerProtocol);
        client.run();
    }

}
