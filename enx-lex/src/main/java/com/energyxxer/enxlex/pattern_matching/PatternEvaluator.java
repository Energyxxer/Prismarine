package com.energyxxer.enxlex.pattern_matching;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;

public interface PatternEvaluator<CTX> {
    Object evaluate(TokenPattern<?> pattern, CTX ctx, Object[] data);

    class NoEvaluatorException extends RuntimeException {
        public NoEvaluatorException() {
        }

        public NoEvaluatorException(String message) {
            super(message);
        }
    }
}
