package com.example.expenseapp.audit;

import com.example.expenseapp.entity.Expense;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.security.LoginUsers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * {@link Audited} 付き Service メソッドの成功後に audit_logs へ INSERT する。
 * ビジネストランザクションと同一トランザクションで記録し、ロールバック時は監査ログも巻き戻す。
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public AuditLogAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(audited)")
    public Object record(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        Object result = joinPoint.proceed();
        LoginUser loginUser = LoginUsers.requireCurrent();
        Integer targetId = resolveTargetId(joinPoint, result, audited);
        if (targetId == null) {
            throw new IllegalStateException("監査対象のIDを特定できませんでした。");
        }
        auditLogService.record(loginUser.getUserId(), audited.action(), audited.targetTable(), targetId);
        return result;
    }

    private Integer resolveTargetId(ProceedingJoinPoint joinPoint, Object result, Audited audited) {
        if (!audited.idParam().isBlank()) {
            return extractNamedArgument(joinPoint, audited.idParam());
        }
        Integer fromResult = extractId(result);
        if (fromResult != null) {
            return fromResult;
        }
        Integer fromExpenseId = extractNamedArgument(joinPoint, "expenseId");
        if (fromExpenseId != null) {
            return fromExpenseId;
        }
        for (Object arg : joinPoint.getArgs()) {
            Integer id = extractId(arg);
            if (id != null) {
                return id;
            }
        }
        return null;
    }

    private Integer extractNamedArgument(ProceedingJoinPoint joinPoint, String name) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] names = parameterNameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                if (name.equals(names[i])) {
                    return extractId(args[i]);
                }
            }
        }
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (name.equals(parameters[i].getName())) {
                return extractId(args[i]);
            }
        }
        return null;
    }

    private Integer extractId(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Long longValue) {
            return longValue.intValue();
        }
        if (value instanceof Expense expense) {
            return expense.getExpenseId();
        }
        return null;
    }
}
