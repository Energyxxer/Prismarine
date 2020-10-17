package com.energyxxer.prismarine.typesystem.functions;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;

public interface PrimitivePrismarineFunction {

    Object call(Object[] params, TokenPattern<?>[] patterns, TokenPattern<?> pattern, ISymbolContext ctx, Object thisObject);

    default Object safeCall(Object[] params, TokenPattern<?>[] patterns, TokenPattern<?> pattern, ISymbolContext ctx, Object thisObject) {
        try {
            return call(params, patterns, pattern, ctx, thisObject);
        } catch(PrismarineException | PrismarineException.Grouped x) {
            throw x;
        } catch (Exception x) {
            throw new PrismarineException(PrismarineException.Type.INTERNAL_EXCEPTION, x.toString(), pattern, ctx);
        }
    }
}
