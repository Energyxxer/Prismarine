package com.energyxxer.prismarine.operators;

public class TernaryOperator extends Operator {
    private final boolean isLeftOperator;
    private TernaryOperator counterpart;

    private TernaryOperator(String symbol, int precedence, OperationOrder order, boolean isLeftOperator) {
        super(symbol, precedence, order);
        this.isLeftOperator = isLeftOperator;
    }

    public static TernaryOperator[] newPair(String symbolLeft, String symbolRight, int precedence, OperationOrder order) {
        TernaryOperator left = new TernaryOperator(symbolLeft, precedence, order, true);
        TernaryOperator right = new TernaryOperator(symbolRight, precedence, order, false);
        left.counterpart = right;
        right.counterpart = left;

        return new TernaryOperator[] {left, right};
    }

    public boolean isPrimaryOperator() {
        return isLeftOperator;
    }

    public Operator getTernaryRight() {
        return isLeftOperator ? counterpart : this;
    }
}
