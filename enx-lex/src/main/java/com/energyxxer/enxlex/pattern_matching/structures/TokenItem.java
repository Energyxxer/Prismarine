package com.energyxxer.enxlex.pattern_matching.structures;

import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.util.StringBounds;
import com.energyxxer.util.StringLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class TokenItem extends TokenPattern<Token> {
	private final Token token;
	
	public TokenItem(Token token, TokenPatternMatch source) {
		super(source);
		this.token = token;
	}

	@Override
	public Token getContents() {
		return token;
	}
	
	@Override
	public TokenItem setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String toString() {
		return "(I)" + ((name != null && name.length() > 0) ? name + ": " : "") + "{ " + token + " }";
	}

	@Override
	public void collect(TokenType type, List<Token> list) {
		if(token.type == type) list.add(token);
	}

	@Override
	public void deepCollect(TokenType type, List<Token> list) {
		collect(type, list);
	}

	@Override
	public TokenPattern<?> getByName(String name) {
		return null;
	}

	@Override
	public void collectByName(String name, List<TokenPattern<?>> list) {
	}

	@Override
	public void deepCollectByName(String name, List<TokenPattern<?>> list) {
	}

	@Override
	public TokenPattern<?> find(String path) {
		return (this.name != null && name.equals(path)) ? this : null;
	}

	@Override
	public String flatten(String delimiter) {
		return token.value;
	}

	@Override
	public TokenSource getSource() {
		return token.source;
	}

	@Override
	public StringLocation getStringLocation() {
		return new StringLocation(token.index, token.line, token.column);
	}

	@Override
	public StringBounds getStringBounds() {
		return new StringBounds(
				new StringLocation(token.index, token.line, token.column),
				new StringLocation(token.index + token.value.length(), token.line, token.column + token.value.length())
		);
	}

    @Override
    public ArrayList<Token> flattenTokens(ArrayList<Token> list) {
		token.flattenTokens(list);
        return list;
    }

	@Override
	public String getType() {
		return "ITEM";
	}

	@Override
	public TokenItem addTags(List<String> newTags) {
		super.addTags(newTags);
		return this;
	}

	@Override
	public void validate() {
		this.validated = true;
	}

    @Override
    public void traverse(Consumer<TokenPattern<?>> consumer, Stack<TokenPattern<?>> stack) {
		if(stack != null) stack.push(this);
		consumer.accept(this);
		if(stack != null) stack.pop();
	}

    @Override
    public int endIndex() {
        return token.endIndex();
    }
}
