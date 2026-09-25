package com.example.expenseapp.exception;

public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("現在のパスワードが正しくありません。");
    }
}
