package com.energyxxer.prismarine.operators;

public class Operator {
    private String symbol;
    private int precedence;
    private OperationOrder order;

    public Operator(String symbol, int precedence, OperationOrder order) {
        this.symbol = symbol;
        this.precedence = precedence;
        this.order = order;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getPrecedence() {
        return precedence;
    }

    public OperationOrder getOrder() {
        return order;
    }
}
