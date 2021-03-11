package com.energyxxer.prismarine.operators;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.expressions.TokenBinaryExpression;
import com.energyxxer.prismarine.expressions.TokenExpression;
import com.energyxxer.prismarine.expressions.TokenTernaryExpression;
import com.energyxxer.prismarine.expressions.TokenUnaryExpression;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.functions.ActualParameterList;
import com.energyxxer.prismarine.typesystem.functions.PrismarineFunction;
import com.energyxxer.prismarine.typesystem.functions.typed.TypedFunction;
import com.energyxxer.prismarine.typesystem.functions.typed.TypedFunctionFamily;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OperatorManager<T extends TypedFunction> {
    private Function<String, TypedFunctionFamily<T>> familyConstructor = TypedFunctionFamily::new;

    private final PrismarineTypeSystem typeSystem;

    public final HashMap<String, TypedFunctionFamily<T>> unaryLeftOperators = new HashMap<>();
    public final HashMap<String, TypedFunctionFamily<T>> unaryRightOperators = new HashMap<>();
    public final HashMap<String, TypedFunctionFamily<T>> binaryOperators = new HashMap<>();
    public final HashMap<String, TypedFunctionFamily<T>> ternaryOperators = new HashMap<>();

    public final HashMap<String, BiFunction<TokenUnaryExpression, ISymbolContext, Object>> specialUnaryLeftOperators = new HashMap<>();
    public final HashMap<String, BiFunction<TokenUnaryExpression, ISymbolContext, Object>> specialUnaryRightOperators = new HashMap<>();
    public final HashMap<String, BiFunction<TokenBinaryExpression, ISymbolContext, Object>> specialBinaryOperators = new HashMap<>();
    public final HashMap<String, BiFunction<TokenTernaryExpression, ISymbolContext, Object>> specialTernaryOperators = new HashMap<>();

    public OperatorManager(PrismarineTypeSystem typeSystem, Function<String, TypedFunctionFamily<T>> familyConstructor) {
        this.familyConstructor = familyConstructor;
        this.typeSystem = typeSystem;
    }

    public OperatorManager(PrismarineTypeSystem typeSystem) {
        this.typeSystem = typeSystem;
    }

    public PrismarineTypeSystem getTypeSystem() {
        return typeSystem;
    }

    public void addOperator(String symbol, HashMap<String, TypedFunctionFamily<T>> map, T function) {
        TypedFunctionFamily<T> family = map.get(symbol);
        if(family == null) {
            map.put(symbol, family = familyConstructor.apply(symbol));
        }
        family.putOverload(function);
    }

    private Object evaluate(TokenUnaryExpression expr, ISymbolContext ctx) {
        UnaryOperator op = expr.getOperator();
        String sym = op.getSymbol();

        HashMap<String, TypedFunctionFamily<T>> correspondingMap = op.getOrder() == OperationOrder.LTR ? unaryRightOperators : unaryLeftOperators;
        HashMap<String, BiFunction<TokenUnaryExpression, ISymbolContext, Object>> correspondingSpecialMap = op.getOrder() == OperationOrder.LTR ? specialUnaryRightOperators : specialUnaryLeftOperators;

        Object[] operands;

        if(correspondingSpecialMap.containsKey(sym)) {
            try {
                return correspondingSpecialMap.get(sym).apply(expr, ctx);
            } catch(SpecialOperatorFailure failure) {
                //special operator overload didn't match the types
                //Reuse the already-parsed operands
                operands = failure.getOperandsAsEvaluated();
            }
        } else {
            TokenPattern<?>[] operandPatterns = expr.getOperands();
            operands = Arrays.copyOf(operandPatterns, operandPatterns.length, Object[].class);
        }

        TypedFunctionFamily<T> family = correspondingMap.get(sym);
        if(family == null) {
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Unknown unary operator " + sym, expr, ctx);
        }

        return evaluate(family, operands, expr, ctx);
    }
    private Object evaluate(TokenBinaryExpression expr, ISymbolContext ctx) {
        BinaryOperator op = expr.getOperator();
        String sym = op.getSymbol();

        Object[] operands;

        if(specialBinaryOperators.containsKey(sym)) {
            try {
                return specialBinaryOperators.get(sym).apply(expr, ctx);
            } catch(SpecialOperatorFailure failure) {
                //special operator overload didn't match the types
                //Reuse the already-parsed operands
                operands = failure.getOperandsAsEvaluated();
            }
        } else {
            TokenPattern<?>[] operandPatterns = expr.getOperands();
            operands = Arrays.copyOf(operandPatterns, operandPatterns.length, Object[].class);
        }

        TypedFunctionFamily<T> family = binaryOperators.get(sym);
        if(family == null) {
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Unknown binary operator " + sym, expr, ctx);
        }

        return evaluate(family, operands, expr, ctx);
    }
    private Object evaluate(TokenTernaryExpression expr, ISymbolContext ctx) {
        TernaryOperator op = expr.getOperator();
        String sym = op.getSymbol();

        Object[] operands;

        if(specialTernaryOperators.containsKey(sym)) {
            try {
                return specialTernaryOperators.get(sym).apply(expr, ctx);
            } catch(SpecialOperatorFailure failure) {
                //special operator overload didn't match the types
                //Reuse the already-parsed operands
                operands = failure.getOperandsAsEvaluated();
            }
        } else {
            TokenPattern<?>[] operandPatterns = expr.getOperands();
            operands = Arrays.copyOf(operandPatterns, operandPatterns.length, Object[].class);
        }

        TypedFunctionFamily<T> family = ternaryOperators.get(sym);
        if(family == null) {
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Unknown ternary operator " + sym + " " + op.getTernaryRight().getSymbol(), expr, ctx);
        }

        return evaluate(family, operands, expr, ctx);
    }
    public Object evaluate(@NotNull TokenExpression expr, ISymbolContext ctx) {
        if(expr instanceof TokenUnaryExpression) return evaluate((TokenUnaryExpression) expr, ctx);
        if(expr instanceof TokenBinaryExpression) return evaluate((TokenBinaryExpression) expr, ctx);
        if(expr instanceof TokenTernaryExpression) return evaluate((TokenTernaryExpression) expr, ctx);
        throw new IllegalArgumentException("TokenExpression that is not unary, binary nor ternary: " + expr.getClass());
    }

    private Object evaluate(TypedFunctionFamily<T> family, Object[] operands, TokenExpression expr, ISymbolContext ctx) {
        TokenPattern<?>[] operandPatterns = expr.getOperands();
        for(int i = 0; i < operands.length; i++) {
            if(operands[i] instanceof TokenExpression) {
                operands[i] = evaluate((TokenExpression) operands[i], ctx);
            } else if(operands[i] instanceof TokenPattern<?>) {
                operands[i] = ((TokenPattern<?>) operands[i]).evaluate(ctx);
            }
        }

        ActualParameterList params = new ActualParameterList(operands, operandPatterns, expr);
        PrismarineFunction function = family.pickOverload(params, ctx, null);
        return function.safeCall(params, ctx, null);
    }

    public Object evaluateUnaryLeft(String symbol, Object[] operands, TokenExpression expr, ISymbolContext ctx) {
        return evaluate(unaryLeftOperators.get(symbol), operands, expr, ctx);
    }

    public Object evaluateUnaryRight(String symbol, Object[] operands, TokenExpression expr, ISymbolContext ctx) {
        return evaluate(unaryRightOperators.get(symbol), operands, expr, ctx);
    }

    public Object evaluateBinary(String symbol, Object[] operands, TokenExpression expr, ISymbolContext ctx) {
        return evaluate(binaryOperators.get(symbol), operands, expr, ctx);
    }

    public Object evaluateTernary(String symbol, Object[] operands, TokenExpression expr, ISymbolContext ctx) {
        return evaluate(ternaryOperators.get(symbol), operands, expr, ctx);
    }

    public void registerUnaryLeftOperator(String symbol, T method) {
        addOperator(symbol, unaryLeftOperators, method);
    }

    public void registerUnaryRightOperator(String symbol, T method) {
        addOperator(symbol, unaryRightOperators, method);
    }

    public void registerBinaryOperator(String symbol, T method) {
        addOperator(symbol, binaryOperators, method);
    }

    public void registerTernaryOperator(String symbol, T method) {
        addOperator(symbol, ternaryOperators, method);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface NativeOperator {
        String symbol();

        int grade();
    }

    public static class SpecialOperatorFailure extends RuntimeException {
        private final Object[] operandsAsEvaluated;

        public SpecialOperatorFailure(Object[] operandsAsEvaluated) {
            this.operandsAsEvaluated = operandsAsEvaluated;
        }

        public Object[] getOperandsAsEvaluated() {
            return operandsAsEvaluated;
        }
    }
}
