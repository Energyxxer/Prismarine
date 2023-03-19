package com.energyxxer.prismarine.typesystem.functions.typed;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.SymbolVisibility;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.TypeConstraints;
import com.energyxxer.prismarine.typesystem.functions.ActualParameterList;
import com.energyxxer.prismarine.typesystem.functions.FormalParameter;
import com.energyxxer.prismarine.typesystem.functions.PrismarineFunction;
import com.energyxxer.prismarine.typesystem.generics.GenericUtils;
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


    public static ActualParameterResponse getActualParameterByFormalIndex(int formalIndex, List<FormalParameter> formalParams, ActualParameterList actualParams, ISymbolContext ctx, Object thisObject) {
        FormalParameter formalParameter = formalParams.get(formalIndex);
        TypeConstraints formalConstraints = formalParameter.getTypeConstraints();

        try {
            if(formalConstraints.isGeneric()) {
                formalConstraints.startGenericSubstitution(GenericUtils.nonGeneric(formalConstraints.getGenericHandler(), thisObject, actualParams, ctx));
            }
            int actualIndex = actualParams.getIndexOfName(formalParameter.getName());
            if(actualIndex == -1) {
                //in position (or missing)
                actualIndex = formalIndex;

                if(actualParams.getNameForIndex(formalIndex) != null) {
                    if(formalParameter.getTypeConstraints().isNullable()) {
                        return ActualParameterResponse.get(null, -1);
                    }
                    throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "There is no argument given that corresponds to the required formal parameter '" + formalParameter.getName() + "'", actualParams.getPattern(formalIndex), ctx);
                } else if(actualIndex >= actualParams.size()) {
                    if(formalParameter.getTypeConstraints().isNullable()) {
                        return ActualParameterResponse.get(null, -1);
                    }
                }
            } else if(actualIndex != formalIndex) {
                //out of position
                if(actualParams.getNameForIndex(formalIndex) == null && formalIndex < actualParams.size()) {
                    throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Named argument '" + formalParameter.getName() + "' is used out-of-position but its formal position (index " + formalIndex + ") is not named", actualParams.getPattern(formalIndex), ctx);
                }
            }
            Object actualValue;
            if(actualIndex < actualParams.size()) {
                actualValue = actualParams.getValue(actualIndex);
            } else {
                actualValue = null;
            }

            TokenPattern<?> actualPattern = actualIndex < actualParams.size() ? actualParams.getPattern(actualIndex) : actualParams.getPattern();
            formalParameter.getTypeConstraints().validate(actualValue, actualPattern, ctx);
            actualValue = formalParameter.getTypeConstraints().adjustValue(actualValue, actualPattern, ctx);
            if(formalParameter.getValueConstraints() != null) {
                formalParameter.getValueConstraints().validate(actualValue, formalParameter.getName(), actualPattern, ctx);
            }
            return ActualParameterResponse.get(actualValue, actualIndex);
        } finally {
            if(formalConstraints.isGeneric()) {
                formalConstraints.endGenericSubstitution();
            }
        }
    }

    public static class ActualParameterResponse {
        private static final ThreadLocal<ActualParameterResponse> INSTANCE = ThreadLocal.withInitial(ActualParameterResponse::new);

        public Object value;
        public int index;

        public static ActualParameterResponse get(Object value, int index) {
            ActualParameterResponse response = INSTANCE.get();
            response.value = value;
            response.index = index;
            return response;
        }
    }
}
