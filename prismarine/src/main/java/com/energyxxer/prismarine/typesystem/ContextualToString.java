package com.energyxxer.prismarine.typesystem;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;

public interface ContextualToString {
    String contextualToString(TokenPattern<?> pattern, ISymbolContext ctx);
}
