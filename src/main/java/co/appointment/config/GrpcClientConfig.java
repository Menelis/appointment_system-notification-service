package co.appointment.config;

import co.appointment.grpc.AppointmentServiceGrpc;
import co.appointment.grpc.AuthServiceGrpc;
import co.appointment.grpc.BranchServiceGrpc;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;

@Configuration
@ImportGrpcClients(
        target = "auth-service",
        types = {AuthServiceGrpc.AuthServiceBlockingStub.class})
@ImportGrpcClients(
        target = "branch-service",
        types = {BranchServiceGrpc.BranchServiceBlockingStub.class})
@ImportGrpcClients(
        target = "appointment-service",
        types = {AppointmentServiceGrpc.AppointmentServiceImplBase.class})
public class GrpcClientConfig {}
