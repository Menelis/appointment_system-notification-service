package co.appointment.service;

import co.appointment.service.grpc.client.GrpcAppointmentService;
import co.appointment.service.grpc.client.GrpcAuthService;
import co.appointment.service.grpc.client.GrpcBranchService;
import co.appointment.shared.event.AppointmentEvent;
import co.appointment.shared.record.AppointmentSummary;
import co.appointment.shared.record.BranchSummary;
import co.appointment.shared.record.UserSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentService {

    private final JsonMapper jsonMapper;
    private final GrpcBranchService grpcBranchService;
    private final GrpcAppointmentService grpcAppointmentService;
    private final GrpcAuthService grpcAuthService;

    public void handleAppointment(
            final ConsumerRecord<String, byte[]> record, final String eventType, final String xCorrelationId) {
        AppointmentEvent appointmentEvent = jsonMapper.readValue(record.value(), AppointmentEvent.class);
        if (log.isTraceEnabled()) {
            log.trace("Appointment Event=[{}] with correlationId=[{}]", appointmentEvent, xCorrelationId);
        }

        AppointmentSummary appointmentSummary =
                grpcAppointmentService.getAppointmentByRefNo(appointmentEvent.getReferenceNo());
        if (log.isTraceEnabled()) {
            log.trace("Event Type=[{}] Appointment Summary=[{}]", eventType, appointmentSummary);
        }
        BranchSummary branchSummary = grpcBranchService.getBranchById(appointmentSummary.branchId());
        if (log.isTraceEnabled()) {
            log.trace("Event Type=[{}] Branch Summary=[{}]", eventType, branchSummary);
        }
        UserSummary userSummary = grpcAuthService.getUserById(appointmentSummary.customerId());
        if (log.isTraceEnabled()) {
            log.trace("Event Type=[{}] User Summary=[{}]", eventType, userSummary);
        }
    }
}
