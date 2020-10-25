package com.energyxxer.prismarine.typesystem.functions.typed;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.symbols.SymbolVisibility;
import com.energyxxer.prismarine.typesystem.functions.FormalParameter;
import com.energyxxer.prismarine.typesystem.functions.PrismarineFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TypedFunction {
    protected final String name;
    protected final TokenPattern<?> definingPattern;
    protected final PrismarineFunction function;
    protected List<FormalParameter> formalParameters;
    protected @NotNull SymbolVisibility visibility = SymbolVisibility.PUBLIC;

    public TypedFunction(String name) {
        this.name = name;
        this.definingPattern = null;
        this.function = null;
        this.formalParameters = Collections.emptyList();
    }

    public TypedFunction(String name, TokenPattern<?> definingPattern, PrismarineFunction function) {
        this.name = name;
        this.definingPattern = definingPattern;
        this.function = function;

        this.formalParameters = function.getBranch().getFormalParameters();
    }

    public List<FormalParameter> getFormalParameters() {
        return formalParameters;
    }

    public TypedFunction setFormalParameters(List<FormalParameter> formalParameters) {
        this.formalParameters = formalParameters;
        return this;
    }

    public PrismarineFunction getFunction() {
        return function;
    }

    @NotNull
    public SymbolVisibility getVisibility() {
        return visibility;
    }

    public TypedFunction setVisibility(SymbolVisibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public TokenPattern<?> getDefiningPattern() {
        return definingPattern;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        String formalParams = formalParameters.toString();
        return name + "(" + formalParams.substring(1, formalParams.length()-1) + ")";
    }
}
