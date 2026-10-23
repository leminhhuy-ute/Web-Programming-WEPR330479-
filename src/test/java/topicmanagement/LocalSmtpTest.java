package topicmanagement;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
class LocalSmtpTest {
    @Test void mimeEncodingPreservesVietnameseSubjectAndBodyWithoutNetwork() throws Exception {
        var sender=new CapturingMailSender();
        sender.setDefaultEncoding("UTF-8");
        var message=new SimpleMailMessage();message.setFrom("faculty@example.test");message.setTo("student@example.test");
        message.setSubject("Kết quả đề tài");message.setText("Chào Lê Minh Huy\nĐiểm cuối cùng: 8.50");
        sender.send(message);

        assertEquals(1,sender.captured.length);
        var bytes=new ByteArrayOutputStream();
        sender.captured[0].writeTo(bytes);
        var parsed=new MimeMessage(Session.getInstance(new Properties()),new ByteArrayInputStream(bytes.toByteArray()));
        assertEquals(message.getSubject(),parsed.getSubject());
        assertTrue(parsed.getContent().toString().contains("Chào Lê Minh Huy"));
        assertTrue(parsed.getContent().toString().contains("Điểm cuối cùng: 8.50"));
    }

    private static final class CapturingMailSender extends JavaMailSenderImpl {
        private MimeMessage[] captured;

        @Override
        protected void doSend(MimeMessage[] mimeMessages,Object[] originalMessages) throws MailException {
            captured=mimeMessages;
        }
    }
}
