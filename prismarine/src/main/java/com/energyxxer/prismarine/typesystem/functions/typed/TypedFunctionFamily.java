package com.energyxxer.prismarine.typesystem.functions.typed;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.TypeConstraints;
import com.energyxxer.prismarine.typesystem.functions.ActualParameterList;
import com.energyxxer.prismarine.typesystem.functions.FormalParameter;
import com.energyxxer.prismarine.typesystem.functions.PrimitivePrismarineFunction;
import com.energyxxer.prismarine.typesystem.functions.PrismarineFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TypedFunctionFamily<T extends TypedFunction> implements PrimitivePrismarineFunction {
    protected final String name;
    protected final ArrayList<T> implementations = new ArrayList<>();
    protected boolean useExternalThis = false;

    public TypedFunctionFamily(String name) {
        this.name = name;
    }

    public PrismarineFunction.FixedThisFunctionSymbol pickOverloadSymbol(ActualParameterList params, TokenPattern<?> pattern, ISymbolContext ctx, Object thisObject) {
        return new PrismarineFunction.FixedThisFunctionSymbol(name, pickOverload(params, pattern, ctx), thisObject);
    }

    public PrismarineFunction pickOverload(ActualParameterList params, TokenPattern<?> pattern, ISymbolContext ctx) {
        ArrayList<T> bestScoreBranchMatches = new ArrayList<>();
        double bestScore = -1;

        //PrismarineFunctionBranch bestPick = null;
        //boolean bestPickFullyMatched = false;

        for(T method : implementations) {
            List<FormalParameter> branchParams = method.getFormalParameters();
            boolean branchMatched = true;
            double score = 0;
            int paramsCompared = branchParams.size();
            for(int i = 0; i < branchParams.size() && branchMatched; i++) {
                FormalParameter formalParam = branchParams.get(i);
                Object actualParam = null;
                if(i < params.getValues().size()) actualParam = params.getValues().get(i);
                int paramScore = 1;
                if(formalParam.getConstraints() != null) {
                    paramScore = formalParam.getConstraints().rateMatch(actualParam, ctx);
                }
                if(paramScore == 0) {
                    branchMatched = false;
                    score = 0;
                }
                score += paramScore;
            }
            if(branchMatched && branchParams.size() < params.size()) {
                score += (params.size() - branchParams.size());
                paramsCompared += (params.size() - branchParams.size());
                branchMatched = false;
                //More parameters than asked for, deny this overload.
            }

            if(paramsCompared != 0) score /= paramsCompared;
            else {
                score = params.size() == 0 ? 6 : 2;
            }
            if(branchMatched && score >= bestScore) {
                if(score != bestScore) bestScoreBranchMatches.clear();
                bestScore = score;
                bestScoreBranchMatches.add(method);
            }
        }
        if(bestScoreBranchMatches.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            boolean any = false;
            for(Object obj : params.getValues()) {
                sb.append(ctx.getTypeSystem().getTypeIdentifierForObject(obj));
                sb.append(", ");
                any = true;
            }
            if(any) {
                sb.setLength(sb.length()-2);
            }
            StringBuilder overloads = new StringBuilder();
            for(T method : this.implementations) {
                overloads.append("\n    ").append(name).append("(");
                overloads.append(method.getFormalParameters().toString().substring(1));
                overloads.setLength(overloads.length()-1);
                overloads.append(")");
                TypeConstraints returnConstraints = method.getFunction().getBranch().getReturnConstraints();
                if(returnConstraints != null) {
                    overloads.append(" : ");
                    overloads.append(returnConstraints);
                }
            }
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Overload not found for parameter types: (" + sb.toString() + ")\nValid overloads are:" + overloads.toString(), pattern, ctx);
        }
        T bestMatch = bestScoreBranchMatches.get(0);
        if(bestScoreBranchMatches.size() > 1) {
            int sameLengthMatches = 0;
            for(T branch : bestScoreBranchMatches) {
                if(branch.getFormalParameters().size() == params.size()) {
                    bestMatch = branch;
                    sameLengthMatches++;
                }
            }
            if(sameLengthMatches > 1) {
                throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Ambiguous call between: " + bestScoreBranchMatches.stream().filter(b->b.getFormalParameters().size() == params.size()).map(b -> b.getFormalParameters().toString()).collect(Collectors.joining(", ")), pattern, ctx);
            } else if(sameLengthMatches < 1) {
                throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Ambiguous call between: " + bestScoreBranchMatches.stream().map(b -> b.getFormalParameters().toString()).collect(Collectors.joining(", ")), pattern, ctx);
            }
        }

        this.validatePickedOverload(bestMatch, params, pattern, ctx);

        return bestMatch.getFunction();
    }

    protected void validatePickedOverload(T bestMatch, ActualParameterList params, TokenPattern<?> pattern, ISymbolContext ctx) {
    }

    public String getName() {
        return name;
    }

    public T findOverloadForParameters(List<FormalParameter> types) {
        for(T method : implementations) {
            if(FormalParameter.parameterListEquals(method.getFormalParameters(), types)) {
                return method;
            }
        }
        return null;
    }

    public ArrayList<T> getImplementations() {
        return implementations;
    }

    @Override
    public Object call(Object[] params, TokenPattern<?>[] patterns, TokenPattern<?> pattern, ISymbolContext ctx, Object thisObject) {
        PrismarineFunction.FixedThisFunctionSymbol pickedConstructor = this.pickOverloadSymbol(new ActualParameterList(Arrays.asList(params), Arrays.asList(patterns), pattern), pattern, ctx, useExternalThis ? thisObject : null);
        return pickedConstructor.safeCall(params, patterns, pattern, ctx);
    }

    public void setUseExternalThis(boolean useExternalThis) {
        this.useExternalThis = useExternalThis;
    }

    public void putOverload(T function) {
        implementations.add(function);
    }
}
