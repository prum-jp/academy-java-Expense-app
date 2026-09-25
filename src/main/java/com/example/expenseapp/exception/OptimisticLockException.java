package com.example.expenseapp.exception;

public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException() {
        super("他のユーザーにより更新されています。画面を再読み込みして再度お試しください。");
    }
}
