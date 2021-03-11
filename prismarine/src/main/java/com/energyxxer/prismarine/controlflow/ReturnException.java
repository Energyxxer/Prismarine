package com.energyxxer.prismarine.controlflow;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;

public class ReturnException extends RuntimeException {
    private final TokenPattern<?> pattern;
    private final Object value;

    public ReturnException(TokenPattern<?> pattern, Object value) {
        this.pattern = pattern;
        this.value = value;
    }

    public TokenPattern<?> getPattern() {
        return pattern;
    }

    public Object getValue() {
        return value;
    }
}
