package com.energyxxer.prismarine.operators;

public class UnaryOperator extends Operator {

    private UnaryOperator(String symbol, int precedence, OperationOrder order) { //LTR order: RIGHT unary op; RTL order: LEFT unary op
        super(symbol, precedence, order);
    }

    public static UnaryOperator newLeftUnaryOperator(String symbol) {
        return new UnaryOperator(symbol, 0, OperationOrder.RTL);
    }

    public static UnaryOperator newRightUnaryOperator(String symbol) {
        return new UnaryOperator(symbol, 0, OperationOrder.LTR);
    }

    public static UnaryOperator[] newEitherUnaryOperator(String symbol) {
        return new UnaryOperator[] {newLeftUnaryOperator(symbol), newRightUnaryOperator(symbol)};
    }
}
