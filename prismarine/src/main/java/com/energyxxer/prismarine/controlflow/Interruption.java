package com.energyxxer.prismarine.controlflow;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.NoticeType;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;

public abstract class Interruption {
    private final TokenPattern<?> pattern;

    public Interruption(TokenPattern<?> pattern) {
        this.pattern = pattern;
    }

    public TokenPattern<?> getPattern() {
        return pattern;
    }

    public abstract String getUncaughtMessage();

    public Notice getNotice() {
        return new Notice(NoticeType.ERROR, getUncaughtMessage(), pattern);
    }

    public PrismarineException getException(ISymbolContext ctx) {
        return new PrismarineException(PrismarineException.Type.INTERNAL_EXCEPTION, getUncaughtMessage(), pattern, ctx);
    }
}
