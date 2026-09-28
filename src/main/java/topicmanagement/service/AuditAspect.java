package topicmanagement.service;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;
@Aspect @Component
public class AuditAspect {
    private static final Logger log=LoggerFactory.getLogger(AuditAspect.class);
    /** Operation metadata only: never log passwords, request bodies, files or grades. */
    @Around("execution(public * topicmanagement.student.StudentService.*(..)) || execution(public * topicmanagement.council.CouncilService.*(..))")
    public Object measure(ProceedingJoinPoint point) throws Throwable {
        long started=System.nanoTime();
        try {return point.proceed();}
        finally {log.debug("operation={} durationMs={}",point.getSignature().toShortString(),(System.nanoTime()-started)/1_000_000);}
    }
}
