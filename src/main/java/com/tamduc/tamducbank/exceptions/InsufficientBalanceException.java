package com.tamduc.tamducbank.exceptions;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String error) {
        super(error);
    }
}
