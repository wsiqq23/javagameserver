package pers.winter.test.grpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import pers.winter.config.ApplicationConfig;
import pers.winter.config.ConfigManager;
import pers.winter.service.proto.Grpc;
import pers.winter.service.proto.TestServiceGrpc;

public class TestGrpc {
    private static final String requestData = "curl https://api.openai.com/v1/chat/completions   -H \"Content-Type: application/json\"   -H \"Authorization: Bearer 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\"   -d '{\"model\":\"gpt-3.5-turbo\",\"messages\":[{\"role\":\"user\",\"content\":\"What does PreparedStatement.executeUpdate() return when I execute an insert sql?\"}]}'>/Users/admin/Downloads/FireShot/test.log";
    public static void main(String[] args) throws Exception {
        ConfigManager.INSTANCE.init();
        final String host = "127.0.0.1";
        final int port = ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getGrpcPort();
        ManagedChannel channel = NettyChannelBuilder.forAddress(host,port).usePlaintext().build();
        TestServiceGrpc.TestServiceBlockingStub blockingStub = TestServiceGrpc.newBlockingStub(channel);
        Grpc.RpcRequest request = Grpc.RpcRequest.newBuilder().setData(requestData).build();
        Grpc.RpcResponse response = blockingStub.test(request);
        if(!response.getData().equals(requestData)){
            System.out.println("error");
        }
        System.out.println(response.getData().length());
    }
}
