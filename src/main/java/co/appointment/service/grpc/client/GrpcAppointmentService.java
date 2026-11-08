package co.appointment.service.grpc.client;

import co.appointment.grpc.AppointmentServiceGrpc;
import co.appointment.shared.service.grcp.AbstractGrpcAppointmentClient;
import org.springframework.stereotype.Service;

@Service
public class GrpcAppointmentService extends AbstractGrpcAppointmentClient {

    public GrpcAppointmentService(
            final AppointmentServiceGrpc.AppointmentServiceBlockingStub appointmentServiceBlockingStub) {
        super(appointmentServiceBlockingStub);
    }
}
