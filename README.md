# Appointment Notification Service

- The service responsible for sending notifications in and out of appointment system.
- It consumes the events from event streaming service [Kafka](https://kafka.apache.org/) topic.
- It consumes events with the following headers values:
  - header-key - ``eventType``
  - header-values:
    - ``verifyEmail`` - This email content sent to user during registration process to verify email.
    - ``bookingConfirmed`` - The mail content sent to customer once the booking has been confirmed by the branch admin.
    - ``bookingPendingConfirmed`` - The mail send to customer after making an appointment to confirm the booking.
    - ``bookingCancelled`` - The email send to customer when the booking is cancelled either by user or admin
- The structure of email event that is sent by microservices look like this:
  - ```json 
    {
     "recipientEmail": "1@email.com,2@email.com",
      "subject": "Booking - {reference No}",
      "body": "...................",
      "isBodyEncrypted": false
    } ```
- Config file(YAML)
```yaml
infrastructure:
  env: dev
spring:
  kafka:
    bootstrap-servers:
      - broker-1
      - broker-2
      - ......
    consumer:
      enable-auto-commit: false
      isolation-level: read_committed
      group-id: ${infrastructure.env}-appointment-notification-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      auto-offset-reset: earliest
      max-poll-records: 10
      properties:
        max.poll.interval.ms: 600000
    listener:
      ack-mode: manual_immediate
      concurrency: 1
      auto-startup: true
    properties:
      security.protocol: PLAINTEXT
      sasl.mechanism: PLAIN
  mail:
    host: {your host here}
    port: {your port here}
    # The below might not be needed of you are using outgoing mail. Not tested it yet
    username: "{your mail usernane}"
    password: "{your mail password}"
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.debug: true
app:
  kafka:
    consumer:
      topic:
        name: ${infrastructure.env}-appointment-system-notifications
        topic-header-filter:
          header-key: eventType
          header-values:
            - verifyEmail
            - bookingConfirmed
            - bookingPendingConfirmed
            - bookingCancelled
  # This must be stored in vault store
  encryption-key: {Your encryption key her} # AES 256 Encryption Key
  mail:
    from:
      from-address: ${spring.mail.username}
```