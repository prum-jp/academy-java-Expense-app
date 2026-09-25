package com.example.expenseapp.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    String action();

    String targetTable();

    /**
     * JoinPoint の引数名。未指定時は戻り値または expenseId / Expense から推定する。
     */
    String idParam() default "";
}
