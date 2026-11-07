package co.appointment.config;

import co.appointment.grpc.AuthServiceGrpc;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;

@Configuration
@ImportGrpcClients(
        target = "auth-service",
        types = {AuthServiceGrpc.AuthServiceBlockingStub.class})
public class GrpcClientConfig {}
