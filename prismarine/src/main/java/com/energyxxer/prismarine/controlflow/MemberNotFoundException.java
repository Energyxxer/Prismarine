package com.energyxxer.prismarine.controlflow;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException() {
    }

    public MemberNotFoundException(String message) {
        super(message);
    }
}
