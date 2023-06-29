package pers.winter.server.grpc;

import pers.winter.service.proto.Grpc;
import pers.winter.service.proto.TestServiceGrpc;

public class GrpcService extends TestServiceGrpc.TestServiceImplBase {
    private static final String responseData = "curl https://api.openai.com/v1/chat/completions   -H \"Content-Type: application/json\"   -H \"Authorization: Bearer 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\"   -d '{\"model\":\"gpt-3.5-turbo\",\"messages\":[{\"role\":\"user\",\"content\":\"What does PreparedStatement.executeUpdate() return when I execute an insert sql?\"}]}'>/Users/admin/Downloads/FireShot/test.log";

    @Override
    public void test(Grpc.RpcRequest request,
                     io.grpc.stub.StreamObserver<Grpc.RpcResponse> responseObserver) {
        Grpc.RpcResponse response = Grpc.RpcResponse.newBuilder().setData(responseData).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}