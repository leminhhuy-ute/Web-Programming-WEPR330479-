package topicmanagement;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
class LocalSmtpTest {
    @Test void smtpMessagePreservesVietnameseSubjectAndBody() throws Exception {
        try(var server=new ServerSocket(0,1,InetAddress.getLoopbackAddress())) {
            server.setSoTimeout(5000);
            var received=new CompletableFuture<byte[]>();
            var thread=new Thread(()->{
                try(var socket=server.accept()) {
                    socket.setSoTimeout(5000);
                    var input=new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.US_ASCII));
                    var output=new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.US_ASCII),true);
                    output.println("220 localhost test SMTP");
                    boolean data=false;var raw=new StringBuilder();String line;
                    while((line=input.readLine())!=null) {
                        if(data) {
                            if(line.equals(".")) {data=false;received.complete(raw.toString().getBytes(StandardCharsets.US_ASCII));output.println("250 accepted");}
                            else raw.append(line.startsWith("..")?line.substring(1):line).append("\r\n");
                        } else if(line.startsWith("DATA")) {data=true;output.println("354 send message");}
                        else if(line.startsWith("QUIT")) {output.println("221 bye");break;}
                        else output.println("250 OK");
                    }
                } catch(Exception ex) {received.completeExceptionally(ex);}
            });
            thread.setDaemon(true);thread.start();
            var sender=new JavaMailSenderImpl();sender.setHost(server.getInetAddress().getHostAddress());sender.setPort(server.getLocalPort());
            sender.setDefaultEncoding("UTF-8");sender.getJavaMailProperties().setProperty("mail.smtp.timeout","5000");
            var message=new SimpleMailMessage();message.setFrom("faculty@example.test");message.setTo("student@example.test");
            message.setSubject("Kết quả đề tài");message.setText("Chào Lê Minh Huy\nĐiểm cuối cùng: 8.50");sender.send(message);
            var parsed=new MimeMessage(Session.getInstance(new Properties()),new ByteArrayInputStream(received.get(5,TimeUnit.SECONDS)));
            assertEquals(message.getSubject(),parsed.getSubject());
            assertTrue(parsed.getContent().toString().contains("Chào Lê Minh Huy"));
            assertTrue(parsed.getContent().toString().contains("8.50"));thread.join(5000);
        }
    }
}
