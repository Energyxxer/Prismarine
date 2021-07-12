package com.energyxxer.prismarine.typesystem;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;

public interface ValueConstraints {
    void validate(Object value, String symbolName, TokenPattern<?> pattern, ISymbolContext ctx);
}
