package co.appointment.service.grpc.client;

import co.appointment.grpc.AuthServiceGrpc;
import co.appointment.shared.service.grcp.AbstractGrpcAuthClient;
import org.springframework.stereotype.Service;

@Service
public class GrpcAuthService extends AbstractGrpcAuthClient {

    public GrpcAuthService(final AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub) {
        super(authServiceBlockingStub);
    }
}
