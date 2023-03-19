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

public class TokenSwitch extends TokenPattern<TokenPattern<?>> {
	private final TokenPattern<?> group;

	public TokenSwitch(String name, TokenPattern<?> group, TokenPatternMatch source) {
		super(source);
		this.name = name;
		this.group = group;
	}

	@Override
	public TokenPattern<?> getContents() {
		return group;
	}
	
	@Override
	public TokenSwitch setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String toString() {
		return "(Sw)" + name + ": {" + group.toString() + "}";
	}

	@Override
	public void collect(TokenType type, List<Token> list) {
		group.collect(type, list);
	}

	@Override
	public void deepCollect(TokenType type, List<Token> list) {
		group.deepCollect(type, list);
	}

	@Override
	public TokenPattern<?> getByName(String name) {
		return group.getByName(name);
	}

	@Override
	public void collectByName(String name, List<TokenPattern<?>> list) {
		group.collectByName(name, list);
	}

	@Override
	public void deepCollectByName(String name, List<TokenPattern<?>> list) {
		if(group.name.equals(name)) list.add(group);
		group.deepCollectByName(name, list);
	}

	@Override
	public TokenPattern<?> find(String path) {
		if(isPathInCache(path)) return getCachedFindResult(path);
		int dotIndex = path.indexOf('.');
		String name;
		String nextPath;
		if(dotIndex < 0) {
			name = path;
			nextPath = null;
		} else {
			name = path.substring(0, dotIndex);
			nextPath = path.substring(dotIndex+1);
		}

		if(nextPath == null) return putFindResult(path, (this.name.equals(path)) ? this : group.find(path));

		return putFindResult(path, (group.name.equals(name)) ? group.find(nextPath) : group.find(path));
	}

	@Override
	public String flatten(String delimiter) {
		return group.flatten(delimiter);
	}

	@Override
	public TokenSource getSource() {
		return group.getSource();
	}

	@Override
	public StringLocation getStringLocation() {
		return group.getStringLocation();
	}

	@Override
	public StringBounds getStringBounds() { return group.getStringBounds(); }

    @Override
    public ArrayList<Token> flattenTokens(ArrayList<Token> list) {
        return group.flattenTokens(list);
    }

	@Override
	public String getType() {
		return "SWITCH";
	}

	@Override
	public TokenSwitch addTags(List<String> newTags) {
		super.addTags(newTags);
		return this;
	}

	@Override
	public void validate() {
		this.validated = true;
		group.validate();
	}

    @Override
    public void traverse(Consumer<TokenPattern<?>> consumer, Stack<TokenPattern<?>> stack) {
		if(stack != null) stack.push(this);
		consumer.accept(this);
		group.traverse(consumer, stack);
		if(stack != null) stack.pop();
	}

    @Override
	public void simplify(SimplificationDomain domain) {
		if(source == null || (source.getEvaluator() == null && source.getSimplificationFunction() == null)) {
			domain.pattern = group;
		} else {
			super.simplify(domain);
		}
	}

    @Override
    public int endIndex() {
        return group.endIndex();
    }
}
