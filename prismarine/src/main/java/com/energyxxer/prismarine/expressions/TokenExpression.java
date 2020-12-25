package com.energyxxer.prismarine.expressions;

import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.operators.BinaryOperator;
import com.energyxxer.prismarine.operators.Operator;
import com.energyxxer.prismarine.operators.TernaryOperator;
import com.energyxxer.prismarine.operators.UnaryOperator;
import com.energyxxer.util.StringBounds;
import com.energyxxer.util.StringLocation;

import java.util.ArrayList;
import java.util.List;

public abstract class TokenExpression extends TokenPattern<TokenPattern<?>[]> {
    public TokenExpression(TokenPatternMatch source) {
        super(source);
    }

    public abstract void pushValue(TokenPattern value);

    public abstract TokenExpression pushOperator(Operator newOperator, TokenPattern<?> operatorPattern);

    public static TokenExpression createExpressionForOperator(TokenPattern<?> left, Operator operator, TokenPattern<?> operatorPattern, TokenPatternMatch source) {
        if(operator instanceof BinaryOperator) {
            TokenBinaryExpression expr = new TokenBinaryExpression(source);
            expr.pushValue(left);
            expr.pushOperator(operator, operatorPattern);
            return expr;
        } else if(operator instanceof TernaryOperator) {
            if(((TernaryOperator) operator).isPrimaryOperator()) {
                TokenTernaryExpression expr = new TokenTernaryExpression(source);
                expr.pushValue(left);
                expr.pushOperator(operator, operatorPattern);
                return expr;
            } else {
                throw new ExpressionBalanceException("Unexpected right ternary operator " + operator.getSymbol());
            }
        } else if(operator instanceof UnaryOperator) {
            TokenUnaryExpression expr = new TokenUnaryExpression(source);
            expr.pushValue(left);
            expr.pushOperator(operator, operatorPattern);
            return expr;
        } else {
            throw new IllegalStateException("Unexpected operator of class " + operator.getClass());
        }
    }

    @Override
    public TokenExpression addTag(String newTag) {
        super.addTag(newTag);
        return this;
    }

    @Override
    public TokenExpression addTags(List<String> newTags) {
        super.addTags(newTags);
        return this;
    }

    @Override
    public TokenExpression setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public List<Token> search(TokenType type) {
        ArrayList<Token> list = new ArrayList<>();
        for(TokenPattern<?> p : getContents()) {
            if(p.getContents() instanceof Token) {
                if(((Token) p.getContents()).type == type) list.add((Token) p.getContents());
            }
        }
        return list;
    }

    @Override
    public List<Token> deepSearch(TokenType type) {
        ArrayList<Token> list = new ArrayList<>();
        for(TokenPattern<?> p : getContents()) {
            list.addAll(p.deepSearch(type));
        }
        return list;
    }

    @Override
    public List<TokenPattern<?>> searchByName(String s) {
        ArrayList<TokenPattern<?>> list = new ArrayList<>();
        for(TokenPattern<?> p : getContents()) {
            if(p.getName().equals(name)) list.add(p);
        }
        return list;
    }

    @Override
    public List<TokenPattern<?>> deepSearchByName(String s) {
        ArrayList<TokenPattern<?>> list = new ArrayList<>();
        for(TokenPattern<?> p : getContents()) {
            if(p.getName().equals(name)) list.add(p);
            list.addAll(p.deepSearchByName(name));
        }
        return list;
    }

    @Override
    public TokenPattern<?> find(String path) {
        String[] subPath = path.split("\\.",2);

        List<TokenPattern<?>> next = searchByName(subPath[0]);
        if(next.size() <= 0) return null;
        if(subPath.length == 1) return next.get(0);
        return next.get(0).find(subPath[1]);
    }

    @Override
    public StringLocation getStringLocation() {
        StringLocation l = null;
        for (TokenPattern<?> pattern : getContents()) {
            StringLocation loc = pattern.getStringLocation();
            if(l == null) {
                l = loc;
                continue;
            }
            if(loc.index < l.index) {
                l = loc;
            }
        }
        return l;
    }

    @Override
    public StringBounds getStringBounds() {
        StringLocation start = null;
        StringLocation end = null;

        //Find start
        for (TokenPattern<?> pattern : getContents()) {
            StringLocation loc = pattern.getStringLocation();
            if(start == null) {
                start = loc;
                continue;
            }
            if(loc.index < start.index) {
                start = loc;
            }
        }

        //Find end
        for (TokenPattern<?> pattern : getContents()) {
            StringLocation loc = pattern.getStringBounds().end;
            if(end == null) {
                end = loc;
                continue;
            }
            if(loc.index > end.index) {
                end = loc;
            }
        }
        return new StringBounds(start, end);
    }

    @Override
    public ArrayList<Token> flattenTokens(ArrayList<Token> list) {
        for(TokenPattern<?> pattern : getContents()) {
            pattern.flattenTokens(list);
        }
        return list;
    }

    @Override
    public TokenSource getSource() {
        for(TokenPattern pattern : getContents()) {
            TokenSource source = pattern.getSource();
            if(source != null) {
                return source;
            }
        }
        return null;
    }

    @Override
    public String flatten(boolean separate) {
        StringBuilder sb = new StringBuilder();
        TokenPattern<?>[] patterns = getContents();
        for(int i = 0; i < patterns.length; i++) {
            String str = patterns[i].flatten(separate);
            sb.append(str);
            if(!str.isEmpty() && i < patterns.length-1 && separate) sb.append(" ");
        }
        return sb.toString();
    }

    @Override
    public void validate() {
        this.validated = true;
        if(this.name != null && this.name.length() > 0) this.tags.add(name);
        TokenPattern<?>[] patterns = getContents();
        for(TokenPattern<?> p : patterns) {
            for(String tag : this.tags) {
                if(!tag.startsWith("__")) {
                    p.addTag(tag);
                }
            }
            p.validate();
        }
    }

    @Override
    public String getType() {
        return "EXPRESSION";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for(TokenPattern<?> pattern : getContents()) {
            sb.append(' ');
            if(pattern instanceof TokenExpression) {
                sb.append(pattern);
            } else {
                sb.append(pattern.flatten(false));
            }
        }
        sb.append(" )");
        return sb.toString();
    }

    public abstract boolean isComplete();

    public abstract Operator getOperator();
    public abstract TokenPattern<?> getOperatorPattern();
    public abstract TokenPattern[] getOperands();
}
