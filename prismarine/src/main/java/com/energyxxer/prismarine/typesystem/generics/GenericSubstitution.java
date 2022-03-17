package com.energyxxer.prismarine.typesystem.generics;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.TypeConstraints;

import java.util.ArrayList;

public class GenericSubstitution implements AutoCloseable {

    private GenericSupplier genericSupplier;
    private TokenPattern<?> pattern;
    private ISymbolContext ctx;

    private ArrayList<TypeConstraints> constraints;

    public GenericSubstitution(GenericSupplier genericSupplier, TokenPattern<?> p, ISymbolContext ctx) {
        if(genericSupplier != null) {
            constraints = new ArrayList<>();
            this.pattern = p;
            this.ctx = ctx;
        }
        this.genericSupplier = genericSupplier;
    }

    public GenericSubstitution substitute(TypeConstraints constraint) {
        if(constraints != null && constraint != null && constraint.isGeneric()) {
            constraint.startGenericSubstitution(genericSupplier, pattern, ctx);
            constraints.add(constraint);
        }
        return this;
    }

    @Override
    public void close() {
        if(constraints != null) for(TypeConstraints constraint : constraints) {
            constraint.endGenericSubstitution();
        }

        genericSupplier = null;
        pattern = null;
        ctx = null;

        constraints = null;
    }
}
