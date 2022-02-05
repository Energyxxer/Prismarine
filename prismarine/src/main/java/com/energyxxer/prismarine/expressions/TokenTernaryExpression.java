package com.energyxxer.prismarine.expressions;

import com.energyxxer.prismarine.operators.OperationOrder;
import com.energyxxer.prismarine.operators.Operator;
import com.energyxxer.prismarine.operators.TernaryOperator;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;

public class TokenTernaryExpression extends TokenExpression {
    private TernaryOperator operator; //primary (left)

    private TokenPattern firstOperatorPattern;
    private TokenPattern secondOperatorPattern;

    private boolean secondOperatorFound = false;

    private TokenPattern left;
    private TokenPattern middle;
    private TokenPattern right;

    public TokenTernaryExpression(TokenPatternMatch source) {
        super(source);
    }

    @Override
    public void pushValue(TokenPattern value) {
        if(left == null) {
            left = value;
        } else if(middle == null) {
            middle = value;
        } else {
            if(!secondOperatorFound) {
                if(middle instanceof TokenExpression) {
                    ((TokenExpression) middle).pushValue(value);
                } else {
                    throw new IllegalStateException("Added a value to a filled expression (ternary middle)");
                }
            } else {
                if(right == null) {
                    right = value;
                } else if(right instanceof TokenExpression) {
                    ((TokenExpression) right).pushValue(value);
                } else {
                    throw new IllegalStateException("Added a value to a filled expression (ternary right)");
                }
            }
        }
    }

    @Override
    public TokenExpression pushOperator(Operator newOperator, TokenPattern<?> operatorPattern) {
        if(this.operator == null) {
            if(!(newOperator instanceof TernaryOperator) || !((TernaryOperator)newOperator).isPrimaryOperator()) {
                throw new IllegalArgumentException("Received a non-primary ternary operator as the first operator of a ternary expression");
            }
            this.operator = (TernaryOperator) newOperator;
            this.firstOperatorPattern = operatorPattern;
            return this;
        } else {
            if(!secondOperatorFound && newOperator == operator.getTernaryRight() && (!(middle instanceof TokenExpression) || ((TokenExpression) middle).isComplete())) {
                secondOperatorFound = true;
                secondOperatorPattern = operatorPattern;
                return this;
            }

            int precedenceDiff = newOperator.getPrecedence() - this.operator.getPrecedence();
            if(precedenceDiff == 0) {
                precedenceDiff = newOperator.getOrder() == OperationOrder.LTR ? 1 : -1;
            }
            if(!secondOperatorFound) {
                precedenceDiff = -1;
            }

            if(precedenceDiff > 0) { //new tree root
                return TokenExpression.createExpressionForOperator(this, newOperator, operatorPattern, this.source).setName(this.name).addTags(this.tags);
            } else { //new right subtree
                if(!secondOperatorFound) { //middle is not complete so push the operator to the middle branch
                    if(middle instanceof TokenExpression) {
                        middle = ((TokenExpression) middle).pushOperator(newOperator, operatorPattern);
                    } else {
                        middle = TokenExpression.createExpressionForOperator(middle, newOperator, operatorPattern, this.source).setName(this.name).addTags(this.tags);
                    }
                } else { //middle is complete so push the operator to the right branch
                    if(right instanceof TokenExpression) {
                        right = ((TokenExpression) right).pushOperator(newOperator, operatorPattern);
                    } else {
                        right = TokenExpression.createExpressionForOperator(right, newOperator, operatorPattern, this.source).setName(this.name).addTags(this.tags);
                    }
                }
                return this;
            }
        }
    }

    @Override
    public TokenPattern[] getContents() {
        return new TokenPattern[] {left, firstOperatorPattern, middle, secondOperatorPattern, right};
    }

    @Override
    public int endIndex() {
        return (right != null ? right : (secondOperatorPattern != null ? secondOperatorPattern : (middle != null ? middle : (firstOperatorPattern != null ? firstOperatorPattern : left)))).endIndex();
    }

    @Override
    public boolean isComplete() {
        return secondOperatorFound && left != null && (!(left instanceof TokenExpression) || ((TokenExpression) left).isComplete()) && operator != null && right != null && (!(right instanceof TokenExpression) || ((TokenExpression) right).isComplete());
    }

    @Override
    public TernaryOperator getOperator() {
        return operator;
    }

    @Override
    public TokenPattern<?> getOperatorPattern() {
        return firstOperatorPattern;
    }

    @Override
    public TokenPattern[] getOperands() {
        return new TokenPattern[] {left, middle, right};
    }
}
