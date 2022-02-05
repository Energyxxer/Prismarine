package com.energyxxer.prismarine.expressions;

import com.energyxxer.prismarine.operators.BinaryOperator;
import com.energyxxer.prismarine.operators.OperationOrder;
import com.energyxxer.prismarine.operators.Operator;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;

public class TokenBinaryExpression extends TokenExpression {
    private TokenPattern left;
    private BinaryOperator operator;
    private TokenPattern operatorPattern;
    private TokenPattern right;

    public TokenBinaryExpression(TokenPatternMatch source) {
        super(source);
    }

    @Override
    public void pushValue(TokenPattern value) {
        if(left == null) {
            left = value;
        } else if(right == null) {
            right = value;
        } else {
            if(right instanceof TokenExpression) {
                ((TokenExpression) right).pushValue(value);
            } else {
                throw new IllegalStateException("Added a value to a filled expression (binary right)");
            }
        }
    }

    @Override
    public TokenExpression pushOperator(Operator newOperator, TokenPattern<?> operatorPattern) {
        if(this.operator == null) {
            if(!(newOperator instanceof BinaryOperator)) {
                throw new IllegalArgumentException("Received a non-binary operator as the first operator of a binary expression");
            }
            this.operator = (BinaryOperator) newOperator;
            this.operatorPattern = operatorPattern;
            return this;
        } else {
            int precedenceDiff = newOperator.getPrecedence() - this.operator.getPrecedence();
            if(precedenceDiff == 0) {
                precedenceDiff = newOperator.getOrder() == OperationOrder.LTR ? 1 : -1;
            }

            if(precedenceDiff > 0) { //new tree root
                return TokenExpression.createExpressionForOperator(this, newOperator, operatorPattern, this.source).setName(this.name).addTags(this.tags);
            } else { //new right subtree
                if(right instanceof TokenExpression) {
                    right = ((TokenExpression) right).pushOperator(newOperator, operatorPattern);
                    return this;
                } else {
                    this.right = TokenExpression.createExpressionForOperator(this.right, newOperator, operatorPattern, this.source).setName(this.name).addTags(this.tags);
                    return this;
                }
            }
        }
    }

    @Override
    public TokenPattern[] getContents() {
        return new TokenPattern[] {left, operatorPattern, right};
    }

    @Override
    public int endIndex() {
        return (right != null ? right : (operatorPattern != null ? operatorPattern : left)).endIndex();
    }

    @Override
    public boolean isComplete() {
        return left != null && (!(left instanceof TokenExpression) || ((TokenExpression) left).isComplete()) && operator != null && right != null && (!(right instanceof TokenExpression) || ((TokenExpression) right).isComplete());
    }

    @Override
    public BinaryOperator getOperator() {
        return operator;
    }

    @Override
    public TokenPattern<?> getOperatorPattern() {
        return operatorPattern;
    }

    @Override
    public TokenPattern[] getOperands() {
        return new TokenPattern[] {left, right};
    }
}
