package com.energyxxer.prismarine.expressions;


import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.operators.OperationOrder;
import com.energyxxer.prismarine.operators.Operator;
import com.energyxxer.prismarine.operators.UnaryOperator;


public class TokenUnaryExpression extends TokenExpression {
    private TokenPattern operand;
    private UnaryOperator operator;
    private TokenPattern<?> operatorPattern;

    public TokenUnaryExpression(TokenPatternMatch source) {
        super(source);
    }

    @Override
    public void pushValue(TokenPattern value) {
        this.operand = value;
    }

    @Override
    public TokenExpression pushOperator(Operator newOperator, TokenPattern<?> operatorPattern) {
        if(this.operator == null) {
            if (!(newOperator instanceof UnaryOperator)) {
                throw new IllegalArgumentException("Received a non-unary operator as the first operator of a binary expression");
            }
            this.operator = (UnaryOperator) newOperator;
            this.operatorPattern = operatorPattern;
            return this;
        } else {
            //unary operators are always of the lower precedence
            return TokenExpression.createExpressionForOperator(this, newOperator, operatorPattern, this.source).setName(this.name).addTags(this.tags);
        }
    }

    @Override
    public boolean isComplete() {
        return operand != null && operator != null && (!(operand instanceof TokenExpression) || ((TokenExpression) operand).isComplete());
    }

    @Override
    public TokenPattern<?>[] getContents() {
        if(operator.getOrder() == OperationOrder.LTR) {
            return new TokenPattern<?>[] {operand, operatorPattern};
        } else {
            return new TokenPattern<?>[] {operatorPattern, operand};
        }
    }

    @Override
    public int endIndex() {
        return (operator.getOrder() == OperationOrder.LTR ? operatorPattern : operand).endIndex();
    }

    @Override
    public UnaryOperator getOperator() {
        return operator;
    }

    @Override
    public TokenPattern<?> getOperatorPattern() {
        return operatorPattern;
    }

    @Override
    public TokenPattern[] getOperands() {
        return new TokenPattern[] {operand};
    }
}
