package com.energyxxer.prismarine.operators;

import java.util.ArrayList;
import java.util.Arrays;

public class OperatorPool {
    private ArrayList<Operator> operators = new ArrayList<>();

    public OperatorPool(Operator... operators) {
        this.operators.addAll(Arrays.asList(operators));
    }

    public Operator getBinaryOrTernaryOperatorForSymbol(String symbol) {
        for(Operator op : operators) {
            if((op instanceof BinaryOperator || op instanceof TernaryOperator) && op.getSymbol().equals(symbol)) {
                return op;
            }
        }
        return null;
    }

    public UnaryOperator getUnaryLeftOperatorForSymbol(String symbol) {
        for(Operator op : operators) {
            if(op instanceof UnaryOperator && op.getOrder() == OperationOrder.RTL && op.getSymbol().equals(symbol)) {
                return (UnaryOperator) op;
            }
        }
        return null;
    }

    public UnaryOperator getUnaryRightOperatorForSymbol(String symbol) {
        for(Operator op : operators) {
            if(op instanceof UnaryOperator && op.getOrder() == OperationOrder.LTR && op.getSymbol().equals(symbol)) {
                return (UnaryOperator) op;
            }
        }
        return null;
    }

    public BinaryOperator addBinaryOperator(String symbol, int precedence, OperationOrder order) {
        BinaryOperator operator = new BinaryOperator(symbol, precedence, order);
        operators.add(operator);
        return operator;
    }

    public TernaryOperator addTernaryOperator(String symbolLeft, String symbolRight, int precedence, OperationOrder order) {
        TernaryOperator[] pair = TernaryOperator.newPair(symbolLeft, symbolRight, precedence, order);
        operators.add(pair[0]);
        operators.add(pair[1]);
        return pair[0];
    }

    public UnaryOperator addLeftUnaryOperator(String symbol) {
        UnaryOperator operator = UnaryOperator.newLeftUnaryOperator(symbol);
        operators.add(operator);
        return operator;
    }

    public UnaryOperator addRightUnaryOperator(String symbol) {
        UnaryOperator operator = UnaryOperator.newRightUnaryOperator(symbol);
        operators.add(operator);
        return operator;
    }

    public UnaryOperator addEitherUnaryOperator(String symbol) {
        UnaryOperator[] operator = UnaryOperator.newEitherUnaryOperator(symbol);
        operators.add(operator[0]);
        operators.add(operator[1]);
        return operator[0];
    }
}
