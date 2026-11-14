package co.appointment.service;

import static co.appointment.shared.constant.SharedConstants.APPOINTMENT_SYSTEM_EMAIL_FOOTER;
import static co.appointment.shared.util.SharedObjectUtils.returnEmptyIfNullOrBlank;

import co.appointment.config.AppConfigProperties;
import co.appointment.service.grpc.client.GrpcAppointmentService;
import co.appointment.service.grpc.client.GrpcAuthService;
import co.appointment.service.grpc.client.GrpcBranchService;
import co.appointment.shared.event.AppointmentEvent;
import co.appointment.shared.record.AppointmentSummary;
import co.appointment.shared.record.BranchSummary;
import co.appointment.shared.record.UserSummary;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentService {

    private final JsonMapper jsonMapper;
    private final GrpcBranchService grpcBranchService;
    private final GrpcAppointmentService grpcAppointmentService;
    private final GrpcAuthService grpcAuthService;
    private final AppConfigProperties configProperties;
    private final EmailService emailService;

    public void handlePendingConfirmation(
            final ConsumerRecord<String, byte[]> record, final String eventType, final String xCorrelationId) {

        AppointmentEvent appointmentEvent = getAppointmentEvent(record, eventType, xCorrelationId);
        AppointmentSummary appointmentSummary = getAppointmentSummary(appointmentEvent.getReferenceNo(), eventType);
        UserSummary userSummary = getUserSummary(appointmentSummary.customerId(), eventType);

        String messageBody = getMessageBody(
                appointmentSummary,
                userSummary,
                configProperties.getMessageTemplate().getPendingConfirmationTemplate(),
                eventType);

        emailService.sendMail(userSummary.email(), getMessageSubject(appointmentEvent.getReferenceNo()), messageBody);
    }

    public void handleConfirmed(
            final ConsumerRecord<String, byte[]> record, final String eventType, final String xCorrelationId) {

        AppointmentEvent appointmentEvent = getAppointmentEvent(record, eventType, xCorrelationId);
        AppointmentSummary appointmentSummary = getAppointmentSummary(appointmentEvent.getReferenceNo(), eventType);
        UserSummary userSummary = getUserSummary(appointmentSummary.customerId(), eventType);

        String messageBody = getMessageBody(
                appointmentSummary,
                userSummary,
                configProperties.getMessageTemplate().getConfirmedTemplate(),
                eventType);

        emailService.sendMail(userSummary.email(), getMessageSubject(appointmentEvent.getReferenceNo()), messageBody);
    }

    static String getMessageSubject(final String referenceNo) {
        return "Appointment - %s".formatted(referenceNo);
    }

    public void handleCancellation(
            final ConsumerRecord<String, byte[]> record, final String eventType, final String xCorrelationId) {

        String cancelledMessageTemplate = configProperties.getMessageTemplate().getCancellationTemplate();
        AppointmentEvent appointmentEvent = getAppointmentEvent(record, eventType, xCorrelationId);
        AppointmentSummary appointmentSummary = getAppointmentSummary(appointmentEvent.getReferenceNo(), eventType);
        BranchSummary branchSummary = getBranchSummary(appointmentSummary.branchId(), eventType);
        UserSummary userSummary = getUserSummary(appointmentSummary.customerId(), eventType);

        String messageBody = cancelledMessageTemplate.formatted(
                userSummary.fullName(),
                branchSummary.name(),
                appointmentSummary.appointmentDate(),
                getAppointmentSlot(appointmentSummary),
                appointmentSummary.cancellationReason(),
                APPOINTMENT_SYSTEM_EMAIL_FOOTER,
                appendBranchDetails(branchSummary));

        if (log.isTraceEnabled()) {
            log.trace("Event Type=[{}] message Body=[{}]", eventType, messageBody);
        }

        emailService.sendMail(userSummary.email(), getMessageSubject(appointmentEvent.getReferenceNo()), messageBody);
    }

    String getMessageBody(
            final AppointmentSummary appointmentSummary,
            final UserSummary userSummary,
            final String messageTemplate,
            final String eventType) {
        BranchSummary branchSummary = getBranchSummary(appointmentSummary.branchId(), eventType);

        String messageBody = messageTemplate.formatted(
                userSummary.fullName(),
                branchSummary.name(),
                appointmentSummary.appointmentDate(),
                getAppointmentSlot(appointmentSummary),
                appointmentSummary.referenceNo(),
                appointmentSummary.status(),
                APPOINTMENT_SYSTEM_EMAIL_FOOTER,
                appendBranchDetails(branchSummary));

        if (log.isTraceEnabled()) {
            log.trace("Event Type=[{}] message Body=[{}]", eventType, messageBody);
        }
        return messageBody;
    }

    static String getAppointmentSlot(final AppointmentSummary appointmentSummary) {
        if (!StringUtils.hasText(appointmentSummary.slotStart())
                || !StringUtils.hasText(appointmentSummary.slotEnd())) {
            return "Any time";
        }
        return "%s - %s".formatted(appointmentSummary.slotStart(), appointmentSummary.slotEnd());
    }

    static String appendBranchDetails(final BranchSummary branchSummary) {
        String fullAddress = "%s %s %s  %s  %s %s"
                .formatted(
                        returnEmptyIfNullOrBlank(branchSummary.addressLine1()),
                        returnEmptyIfNullOrBlank(branchSummary.addressLine2()),
                        returnEmptyIfNullOrBlank(branchSummary.suburb()),
                        returnEmptyIfNullOrBlank(branchSummary.city()),
                        returnEmptyIfNullOrBlank(branchSummary.province()),
                        returnEmptyIfNullOrBlank(branchSummary.postalCode()));
        return "<b>Address:</b>%s<br/><b>Email:</b>%s<br/><b>Land Line:</b>%s<br/><b>Fax:</b>%s"
                .formatted(fullAddress, branchSummary.email(), branchSummary.landLine(), branchSummary.faxNo());
    }

    AppointmentEvent getAppointmentEvent(
            final ConsumerRecord<String, byte[]> record, final String eventType, final String xCorrelationId) {
        AppointmentEvent appointmentEvent = jsonMapper.readValue(record.value(), AppointmentEvent.class);
        if (log.isTraceEnabled()) {
            log.trace("Appointment Event=[{}] with correlationId=[{}]", appointmentEvent, xCorrelationId);
        }
        if (!StringUtils.hasText(appointmentEvent.getReferenceNo())) {
            throw new IllegalArgumentException(
                    "Event from topic=[%s], partition=[%s], offset=[%s] will not be processed with no reference no"
                            .formatted(record.topic(), record.partition(), record.offset()));
        }
        return appointmentEvent;
    }

    AppointmentSummary getAppointmentSummary(final String referenceNo, final String eventType) {
        AppointmentSummary appointmentSummary = grpcAppointmentService.getAppointmentByRefNo(referenceNo);
        if (log.isTraceEnabled()) {
            log.trace("Event Type=[{}] Appointment Summary=[{}]", eventType, appointmentSummary);
        }
        return appointmentSummary;
    }

    BranchSummary getBranchSummary(final UUID branchId, final String eventType) {
        BranchSummary branchSummary = grpcBranchService.getBranchById(branchId);
        if (log.isTraceEnabled()) {
            log.trace("Event Type=[{}] Branch Summary=[{}]", eventType, branchSummary);
        }
        return branchSummary;
    }

    UserSummary getUserSummary(final UUID userId, final String eventType) {
        UserSummary userSummary = grpcAuthService.getUserById(userId);
        if (log.isTraceEnabled()) {
            log.trace("Event Type=[{}] User Summary=[{}]", eventType, userSummary);
        }
        return userSummary;
    }
}
