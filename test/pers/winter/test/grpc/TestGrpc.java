package pers.winter.test.grpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.config.ApplicationConfig;
import pers.winter.config.ConfigManager;
import pers.winter.service.proto.Grpc;
import pers.winter.service.proto.TestServiceGrpc;

public class TestGrpc {
    private static final Logger logger = LogManager.getLogger(TestGrpc.class);
    public static final String requestData = "curl https://api.openai.com/v1/chat/completions";
//            "   -H \"Content-Type: application/json\"   -H \"Authorization: Bearer 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\"   -d '{\"model\":\"gpt-3.5-turbo\",\"messages\":[{\"role\":\"user\",\"content\":\"What does PreparedStatement.executeUpdate() return when I execute an insert sql?\"}]}'>/Users/admin/Downloads/FireShot/test.log";
    public static void main(String[] args) throws Exception {
        ConfigManager.INSTANCE.init();
        final String host = "192.168.6.13";
        final int port = ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getGrpcPort();
        ManagedChannel channel = NettyChannelBuilder.forAddress(host,port).usePlaintext().build();
        TestServiceGrpc.TestServiceBlockingStub blockingStub = TestServiceGrpc.newBlockingStub(channel);
        TestServiceGrpc.TestServiceStub sub = TestServiceGrpc.newStub(channel);
        while(true){
            long t1 = System.currentTimeMillis();
            Grpc.RpcRequest request = Grpc.RpcRequest.newBuilder().setData(requestData).build();
//            Grpc.RpcResponse response = blockingStub.test(request);
            sub.test(request, new StreamObserver<Grpc.RpcResponse>() {
                @Override
                public void onNext(Grpc.RpcResponse rpcResponse) {
                    long t2 = System.currentTimeMillis();
                    if(!rpcResponse.getData().equals(requestData)){
                        System.out.println("error");
                    }
                    System.out.println("grpc cost:"+(t2-t1));
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onCompleted() {

                }
            });
            Thread.sleep(1000);
        }
    }
}
