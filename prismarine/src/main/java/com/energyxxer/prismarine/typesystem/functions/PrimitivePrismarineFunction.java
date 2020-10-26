package com.energyxxer.prismarine.typesystem.functions;

import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;

public interface PrimitivePrismarineFunction {

    Object call(ActualParameterList params, ISymbolContext ctx, Object thisObject);

    default Object safeCall(ActualParameterList params, ISymbolContext ctx, Object thisObject) {
        try {
            return call(params, ctx, thisObject);
        } catch(PrismarineException | PrismarineException.Grouped x) {
            throw x;
        } catch (Exception x) {
            throw new PrismarineException(PrismarineException.Type.INTERNAL_EXCEPTION, x.toString(), params.getPattern(), ctx);
        }
    }
}
