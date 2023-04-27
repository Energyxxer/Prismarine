package com.energyxxer.enxlex.pattern_matching;

public interface MicroEvaluator<CTX> {
    Object evaluate(CTX ctx, Object[] data);
}
