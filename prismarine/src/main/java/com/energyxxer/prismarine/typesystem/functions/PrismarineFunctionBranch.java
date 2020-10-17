package com.energyxxer.prismarine.typesystem.functions;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.TypeConstraints;

import java.util.ArrayList;
import java.util.Collection;

public abstract class PrismarineFunctionBranch {
    protected final PrismarineTypeSystem typeSystem;
    protected ArrayList<FormalParameter> formalParameters = new ArrayList<>();
    protected TypeConstraints returnConstraints;
    protected boolean shouldCoerceReturn = true;

    public PrismarineFunctionBranch(PrismarineTypeSystem typeSystem, Collection<FormalParameter> formalParameters) {
        this.typeSystem = typeSystem;
        this.formalParameters.addAll(formalParameters);
    }

    public ArrayList<FormalParameter> getFormalParameters() {
        return formalParameters;
    }

    public abstract TokenPattern<?> getFunctionPattern();

    public abstract Object call(Object[] params, TokenPattern<?>[] patterns, TokenPattern<?> pattern, ISymbolContext declaringCtx, ISymbolContext callingCtx, Object thisObject);

    public TypeConstraints getReturnConstraints() {
        return returnConstraints;
    }

    public void setReturnConstraints(TypeConstraints returnConstraints) {
        this.returnConstraints = returnConstraints;
    }

    public boolean isShouldCoerceReturn() {
        return shouldCoerceReturn;
    }

    public void setShouldCoerceReturn(boolean shouldCoerceReturn) {
        this.shouldCoerceReturn = shouldCoerceReturn;
    }
}
