package com.energyxxer.prismarine.typesystem.functions;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.state.CallStack;
import com.energyxxer.prismarine.symbols.Symbol;
import com.energyxxer.prismarine.symbols.SymbolVisibility;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;

import java.util.stream.Collectors;

public class PrismarineFunction implements PrimitivePrismarineFunction {
    private PrismarineFunctionBranch branch;
    private ISymbolContext declaringContext;
    private String functionName = "<anonymous function>";

    public PrismarineFunction(String functionName, PrismarineFunctionBranch branch, ISymbolContext declaringContext) {
        this.branch = branch;
        this.declaringContext = declaringContext;
        if(functionName != null) this.functionName = functionName;
    }

    @Override
    public Object call(Object[] params, TokenPattern<?>[] patterns, TokenPattern<?> pattern, ISymbolContext ctx, Object thisObject) {
        TokenPattern<?> functionPattern = branch.getFunctionPattern();

        if(declaringContext != null) ctx.getCompiler().getCallStack().push(new CallStack.Call(functionName, functionPattern, declaringContext.getStaticParentUnit(), pattern));

        try {
            return branch.call(params, patterns, pattern, declaringContext, ctx, thisObject);
        } catch(StackOverflowError x) {
            throw new PrismarineException(PrismarineException.Type.INTERNAL_EXCEPTION, "Stack Overflow Error", pattern, ctx);
        } finally {
            if(declaringContext != null) ctx.getCompiler().getCallStack().pop();
        }
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public String toString() {
        return "<function(" + (branch.getFormalParameters().stream().map(Object::toString).collect(Collectors.joining(", "))) + ")>";
    }

    public PrismarineFunctionBranch getBranch() {
        return branch;
    }

    public ISymbolContext getDeclaringContext() {
        return declaringContext;
    }

    public static class FixedThisFunction implements PrimitivePrismarineFunction {
        private final PrimitivePrismarineFunction function;
        private final Object thisObject;

        public FixedThisFunction(PrimitivePrismarineFunction function, Object thisObject) {
            this.function = function;
            this.thisObject = thisObject;
        }

        @Override
        public Object call(Object[] params, TokenPattern<?>[] patterns, TokenPattern<?> pattern, ISymbolContext ctx, Object thisObject) {
            return function.call(params, patterns, pattern, ctx, this.thisObject);
        }
    }

    public static class FixedThisFunctionSymbol extends Symbol {
        private PrismarineFunction pickedOverload;
        private Object thisObject;

        public FixedThisFunctionSymbol(String name, PrismarineFunction pickedOverload, Object thisObject) {
            super(name, SymbolVisibility.GLOBAL);
            this.pickedOverload = pickedOverload;
            this.thisObject = thisObject;
        }

        public PrismarineFunction getPickedOverload() {
            return pickedOverload;
        }

        public Object getThisObject() {
            return thisObject;
        }

        public Object safeCall(Object[] params, TokenPattern<?>[] patterns, TokenPattern<?> pattern, ISymbolContext ctx) {
            return pickedOverload.safeCall(params, patterns, pattern, ctx, this.thisObject);
        }
    }
}
