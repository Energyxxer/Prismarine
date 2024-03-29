package com.energyxxer.prismarine.providers;

import com.energyxxer.enxlex.pattern_matching.PatternEvaluator;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.PrismarineProductions;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;

public interface PatternProviderUnit<CTX> extends PatternEvaluator<CTX> {
    TokenPatternMatch createPatternMatch(PrismarineProductions productions, PrismarineProjectWorker worker);

    default String[] getTargetProductionNames() {
        return null;
    }

    default Object evaluate(TokenPattern<?> tokenPattern, CTX ctx, Object[] objects) {
        throw new RuntimeException("Unexpected evaluation");
    }
}
