package com.energyxxer.prismarine.controlflow;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;

public class ReturnException extends RuntimeException {
    private final TokenPattern<?> pattern;
    private final Object value;
    private final ISymbolContext ctx;

    public ReturnException(TokenPattern<?> pattern, Object value, ISymbolContext ctx) {
        this.pattern = pattern;
        this.value = value;
        this.ctx = ctx;
    }

    public TokenPattern<?> getPattern() {
        return pattern;
    }

    public Object getValue() {
        return value;
    }

    public ISymbolContext getContext() {
        return ctx;
    }
}
