package topicmanagement;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.*;
import topicmanagement.notification.*;
class EmailDeliveryTest {
    EmailNotificationRepository repository;
    MailSender sender;
    EmailNotification email;
    EmailDeliveryService delivery;
    @BeforeEach void setup() {
        repository=mock(EmailNotificationRepository.class);sender=mock(MailSender.class);
        email=new EmailNotification();email.id=1L;email.recipient="student@example.test";
        email.subject="Kết quả";email.body="Điểm: 8.50";email.nextAttemptAt=LocalDateTime.now().minusMinutes(1);
        when(repository.lockById(1L)).thenReturn(Optional.of(email));
        delivery=new EmailDeliveryService(repository,sender,true,"faculty@example.test");
    }
    @Test void successfulDeliveryStoresTimestampAndWillNotSendAgain() {
        delivery.deliver(1L);delivery.deliver(1L);
        var captor=ArgumentCaptor.forClass(SimpleMailMessage.class);verify(sender,times(1)).send(captor.capture());
        assertEquals("faculty@example.test",captor.getValue().getFrom());
        assertArrayEquals(new String[]{"student@example.test"},captor.getValue().getTo());
        assertEquals("Điểm: 8.50",captor.getValue().getText());
        assertEquals(EmailNotification.Status.SENT,email.status);assertNotNull(email.sentAt);assertEquals(1,email.attempts);
    }
    @Test void disabledDeliveryDoesNotContactSmtpOrConsumeAnAttempt() {
        new EmailDeliveryService(repository,sender,false,"faculty@example.test").deliver(1L);
        verifyNoInteractions(sender,repository);assertEquals(0,email.attempts);
    }
    @Test void failureRetriesWithBackoffAndStopsAfterFiveAttempts() {
        doThrow(new MailSendException("secret password must not be stored")).when(sender).send(any(SimpleMailMessage.class));
        for(int i=1;i<=5;i++) {
            email.nextAttemptAt=LocalDateTime.now().minusMinutes(1);delivery.deliver(1L);
            assertEquals(i,email.attempts);assertTrue(email.nextAttemptAt.isAfter(LocalDateTime.now()));
        }
        assertEquals(EmailNotification.Status.FAILED,email.status);assertFalse(email.lastError.contains("secret"));
        delivery.deliver(1L);verify(sender,times(5)).send(any(SimpleMailMessage.class));
    }
    @Test void pendingEmailWaitsUntilItsRetryTime() {
        email.nextAttemptAt=LocalDateTime.now().plusMinutes(5);delivery.deliver(1L);
        verifyNoInteractions(sender);assertEquals(0,email.attempts);
    }
    @Test void onlyFailedMessagesCanBeManuallyRetried() {
        assertThrows(IllegalArgumentException.class,()->delivery.retry(1L));
        email.status=EmailNotification.Status.SENT;assertThrows(IllegalArgumentException.class,()->delivery.retry(1L));
        email.status=EmailNotification.Status.FAILED;email.attempts=5;email.lastError="old error";
        delivery.retry(1L);assertEquals(EmailNotification.Status.PENDING,email.status);
        assertEquals(0,email.attempts);assertNull(email.lastError);assertFalse(email.nextAttemptAt.isAfter(LocalDateTime.now()));
    }
    @Test void testMessageIsClearlyIllustrativeAndOnlyGoesToConfiguredSender() {
        delivery.sendTest();
        var captor=ArgumentCaptor.forClass(SimpleMailMessage.class);verify(sender).send(captor.capture());
        var message=captor.getValue();
        assertArrayEquals(new String[]{"faculty@example.test"},message.getTo());
        assertTrue(message.getSubject().contains("THỬ NGHIỆM"));
        assertTrue(message.getText().contains("không phải kết quả học tập chính thức"));
        assertEquals("faculty@example.test",delivery.testRecipient());
    }
    @Test void disabledMailCannotSendTestMessage() {
        assertThrows(IllegalStateException.class,()->new EmailDeliveryService(repository,sender,false,"faculty@example.test").sendTest());
        verifyNoInteractions(sender);
    }
}
