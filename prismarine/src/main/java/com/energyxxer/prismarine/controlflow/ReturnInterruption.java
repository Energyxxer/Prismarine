package com.energyxxer.prismarine.controlflow;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;

public class ReturnInterruption extends Interruption {
    private final Object value;
    private final ISymbolContext ctx;

    public ReturnInterruption(TokenPattern<?> pattern, Object value, ISymbolContext ctx) {
        super(pattern);
        this.value = value;
        this.ctx = ctx;
    }

    public Object getValue() {
        return value;
    }

    public ISymbolContext getContext() {
        return ctx;
    }

    @Override
    public String getUncaughtMessage() {
        return "Return statement outside inner function";
    }
}
