package vn.edu.hcmute.student;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;

@Aspect @Component @org.springframework.core.annotation.Order(org.springframework.core.Ordered.LOWEST_PRECEDENCE-1)
class AuditAspect {
    private static final Logger log=LoggerFactory.getLogger(AuditAspect.class);
    @AfterReturning("execution(public * vn.edu.hcmute.student.StudentService.createGroup(..)) || execution(public * vn.edu.hcmute.student.StudentService.invite(..)) || execution(public * vn.edu.hcmute.student.StudentService.respond(..)) || execution(public * vn.edu.hcmute.student.StudentService.transfer(..)) || execution(public * vn.edu.hcmute.student.StudentService.register(..)) || execution(public * vn.edu.hcmute.student.StudentService.upload(..))")
    public void record(JoinPoint point) { log.info("STUDENT_ACTION completed operation={}",point.getSignature().getName()); }
}
