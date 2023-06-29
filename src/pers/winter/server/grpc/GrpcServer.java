package pers.winter.server.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import pers.winter.server.socket.IServer;

import java.io.IOException;

public class GrpcServer implements IServer {
    Server server;
    @Override
    public void start(int port) {
        server = ServerBuilder.forPort(port).addService(new GrpcService()).build();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void stop() {
        server.shutdown();
    }
}
