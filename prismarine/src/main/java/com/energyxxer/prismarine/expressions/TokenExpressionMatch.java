package com.energyxxer.prismarine.expressions;

import com.energyxxer.prismarine.operators.Operator;
import com.energyxxer.prismarine.operators.OperatorPool;
import com.energyxxer.prismarine.operators.UnaryOperator;
import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;

import java.util.ArrayList;

import static com.energyxxer.enxlex.pattern_matching.TokenMatchResponse.*;

public class TokenExpressionMatch extends TokenPatternMatch {

    private TokenPatternMatch valueMatch;
    private TokenPatternMatch operatorMatch;

    private OperatorPool operatorPool;

    public TokenExpressionMatch(TokenPatternMatch valueMatch, OperatorPool operatorPool, TokenPatternMatch operatorMatch) {
        this.valueMatch = valueMatch;
        this.operatorMatch = operatorMatch;
        this.operatorPool = operatorPool;
    }

    @Override
    public TokenMatchResponse match(int index, Lexer lexer) {
        lexer.setCurrentIndex(index);
        boolean expectOperator = false;

        boolean hasMatched = true;
        Token faultyToken = null;
        int length = 0;
        TokenPatternMatch expected = null;
        TokenPattern expr = null;

        ArrayList<UnaryOperator> unaryOperators = new ArrayList<>();
        ArrayList<TokenPattern> unaryOperatorPatterns = new ArrayList<>();

        itemLoop: for (int i = index; i < lexer.getFileLength();) {

            lexer.setCurrentIndex(i);

            if (expectOperator) {
                TokenMatchResponse itemMatch = this.operatorMatch.match(i, lexer);
                expectOperator = false;
                switch(itemMatch.getMatchType()) {
                    case NO_MATCH: {
                        break itemLoop;
                    }
                    case PARTIAL_MATCH: {
                        hasMatched = false;
                        faultyToken = itemMatch.faultyToken;
                        expected = itemMatch.expected;
                        length += itemMatch.length;
                        break itemLoop;
                    }
                    case COMPLETE_MATCH: {
                        i += itemMatch.length;
                        length += itemMatch.length;
                        String flattenedOperator = itemMatch.pattern.flatten(false);
                        Operator op = operatorPool.getBinaryOrTernaryOperatorForSymbol(flattenedOperator);

                        if(op == null) {
                            hasMatched = false;
                            faultyToken = itemMatch.pattern.flattenTokens().get(0);
                            expected = this.operatorMatch;
                            break itemLoop;
                        }
                        try {
                            if(expr == null) {
                                throw new IllegalStateException("expr cannot be null after the first value");
                            } else if(expr instanceof TokenExpression) {
                                expr = ((TokenExpression) expr).pushOperator(op, itemMatch.pattern);
                            } else {
                                expr = TokenExpression.createExpressionForOperator(expr, op, itemMatch.pattern, this).setName(this.name).addTags(this.tags);
                            }
                        } catch(ExpressionBalanceException x) {
                            hasMatched = false;
                            faultyToken = itemMatch.pattern.flattenTokens().get(0);
                            expected = this.operatorMatch;
                            break itemLoop;
                        }
                    }
                }
            } else {
                //match unary left operators

                while(true) {
                    TokenMatchResponse operatorMatch = this.operatorMatch.match(i, lexer);
                    UnaryOperator op;
                    if(operatorMatch.matched) {
                        String symbol = operatorMatch.pattern.flatten(false);
                        op = operatorPool.getUnaryLeftOperatorForSymbol(symbol);
                    } else {
                        op = null;
                    }
                    if(op == null) {
                        //no more unary operators
                        break;
                    } else {
                        length += operatorMatch.length;
                        i += operatorMatch.length;
                        unaryOperators.add(op);
                        unaryOperatorPatterns.add(operatorMatch.pattern);
                    }
                }

                TokenPattern value = null;

                //match value
                TokenMatchResponse itemMatch = this.valueMatch.match(i, lexer);
                switch(itemMatch.getMatchType()) {
                    case NO_MATCH:
                    case PARTIAL_MATCH: {
                        hasMatched = false;
                        faultyToken = itemMatch.faultyToken;
                        expected = itemMatch.expected;
                        length += itemMatch.length;
                        break itemLoop;
                    }
                    case COMPLETE_MATCH: {
                        i += itemMatch.length;
                        length += itemMatch.length;
                        value = itemMatch.pattern;

                        expectOperator = true;
                    }
                }

                //Wrap value in unary left operators
                for(int operatorIndex = unaryOperators.size()-1; operatorIndex >= 0; operatorIndex--) {
                    UnaryOperator op = unaryOperators.get(operatorIndex);
                    TokenPattern<?> operatorPattern = unaryOperatorPatterns.get(operatorIndex);
                    value = TokenExpression.createExpressionForOperator(value, op, operatorPattern, this).setName(this.name).addTags(this.tags);
                }
                unaryOperators.clear();
                unaryOperatorPatterns.clear();

                //match unary right operators

                TokenMatchResponse previousUnaryOperatorMatch = null;
                while(true) {
                    TokenMatchResponse operatorMatch = this.operatorMatch.match(i, lexer);
                    UnaryOperator op;
                    String symbol = null;
                    if(operatorMatch.matched) {
                        symbol = operatorMatch.pattern.flatten(false);
                        op = operatorPool.getUnaryRightOperatorForSymbol(symbol);
                    } else {
                        op = null;
                    }
                    if(op == null) {
                        //no more unary operators
                        if(
                                (!operatorMatch.matched || //If the faulty token is not a binary/ternary operator...
                                operatorPool.getBinaryOrTernaryOperatorForSymbol(symbol) == null) &&
                                (unaryOperators.size() > 0 && previousUnaryOperatorMatch != null && //...and the last unary operator encountered doubles as a binary operator...
                                operatorPool.getBinaryOrTernaryOperatorForSymbol(unaryOperators.get(unaryOperators.size()-1).getSymbol()) != null)
                        ) {
                            //...then remove the last unary operator - it should be interpreted as a binary one instead (next loop)
                            i -= previousUnaryOperatorMatch.length;
                            length -= previousUnaryOperatorMatch.length;
                            unaryOperators.remove(unaryOperators.size()-1);
                            unaryOperatorPatterns.remove(unaryOperatorPatterns.size()-1);
                        }
                        break;
                    } else {
                        length += operatorMatch.length;
                        i += operatorMatch.length;
                        unaryOperators.add(op);
                        unaryOperatorPatterns.add(operatorMatch.pattern);
                        previousUnaryOperatorMatch = operatorMatch;
                    }
                }

                //Wrap value in unary right operators
                for(int operatorIndex = 0; operatorIndex < unaryOperators.size(); operatorIndex++) {
                    UnaryOperator op = unaryOperators.get(operatorIndex);
                    TokenPattern<?> operatorPattern = unaryOperatorPatterns.get(operatorIndex);
                    value = TokenExpression.createExpressionForOperator(value, op, operatorPattern, this).setName(this.name).addTags(this.tags);
                }
                unaryOperators.clear();
                unaryOperatorPatterns.clear();

                //Push value onto expression tree

                if(expr == null) {
                    expr = value;
                } else if(expr instanceof TokenExpression) {
                    ((TokenExpression) expr).pushValue(value);
                } else {
                    throw new IllegalStateException("Unexpected two values in a row, missed operator in between");
                }
            }
        }

        if(expr == null) { //end of file
            hasMatched = false;
            expected = this;
        }

        if(hasMatched && (expr instanceof TokenExpression && !((TokenExpression) expr).isComplete())) {
            hasMatched = false;
            faultyToken = lexer.retrieveAnyToken();
            expected = operatorMatch;
        }

        if(!hasMatched) {
            invokeFailProcessors(expr, lexer);
        } else {
            invokeProcessors(expr, lexer);
        }
        return new TokenMatchResponse(hasMatched, faultyToken, length, expected, expr);
    }

    @Override
    public String toString() {
        return "Expression";
    }

    @Override
    public String toTrimmedString() {
        return toString();
    }

    @Override
    public String deepToString(int i) {
        return toString();
    }
}
