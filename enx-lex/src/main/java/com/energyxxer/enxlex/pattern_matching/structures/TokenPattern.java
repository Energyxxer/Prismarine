package com.energyxxer.enxlex.pattern_matching.structures;

import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.PatternEvaluator;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.util.ObjectPool;
import com.energyxxer.util.StringBounds;
import com.energyxxer.util.StringLocation;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class TokenPattern<T> {
	public static final ThreadLocal<ObjectPool<ArrayList<TokenPattern<?>>>> PATTERN_LIST_POOL = ThreadLocal.withInitial(() -> new ObjectPool<>(ArrayList::new, ArrayList::clear));

	protected String name = "";
	protected ArrayList<String> tags = new ArrayList<>();
	public TokenPattern parent;
	public final TokenPatternMatch source;
	protected boolean validated = false;
	private ThreadLocal<Object> heldData = null;
	private ThreadLocal<Boolean> holdsData = null;
	private HashMap<String, TokenPattern<?>> findCache = null;

	public TokenPattern(TokenPatternMatch source) {
		this.source = source;
	}

	public abstract T getContents();
	public abstract TokenPattern<T> setName(String name);

	public abstract void collect(TokenType type, List<Token> list);
	public abstract void deepCollect(TokenType type, List<Token> list);

	public abstract TokenPattern<?> getByName(String name);

	public abstract void collectByName(String name, List<TokenPattern<?>> list);
	public abstract void deepCollectByName(String name, List<TokenPattern<?>> list);

	public abstract TokenPattern<?> find(String path);

	protected TokenPattern<?> putFindResult(String path, TokenPattern<?> result) {
		if(findCache == null) findCache = new HashMap<>();
		findCache.put(path, result);
		return result;
	}
	protected boolean isPathInCache(String path) {
		return findCache != null && findCache.containsKey(path);
	}
	protected TokenPattern<?> getCachedFindResult(String path) {
		return findCache.get(path);
	}

	@NotNull public TokenPattern<?> tryFind(String path) {
		TokenPattern<?> rv = find(path);
		if(rv == null) {
			rv = this;
		}
		return rv;
	}

	public String flatten(boolean separate) {
		return flatten(separate ? " " : "");
	}
	public abstract String flatten(String delimiter);

	public abstract TokenSource getSource();

	public String getLocation() {
		StringLocation loc = getStringLocation();
		return getSource().getFileName() + ":" + loc.line + ":" + loc.column + "#" + loc.index;
	}

	public abstract StringLocation getStringLocation();
	public abstract StringBounds getStringBounds();

	public int getCharLength() {
		ArrayList<Token> tokens = flattenTokens(new ArrayList<>());
		if(tokens.size() == 0) return 0;
		int start = tokens.get(0).loc.index;
		Token lastToken = tokens.get(tokens.size()-1);
		int end = lastToken.loc.index + lastToken.value.length();
		return end - start;
	}

	public abstract ArrayList<Token> flattenTokens(ArrayList<Token> list);

	public abstract String getType();

    public String getName() {
        return name;
    }

	public TokenPattern addTag(String newTag) {
		tags.add(newTag);
		return this;
	}

	public TokenPattern addTags(List<String> newTags) {
		tags.addAll(newTags);
		return this;
	}

	public List<String> getTags() {
    	return tags;
	}

	public boolean hasTag(String tag) {
    	return tags.contains(tag);
	}

	public abstract void validate();

	public boolean isValidated() {
		return validated;
	}
	public abstract void traverse(Consumer<TokenPattern<?>> consumer);

	public Object findThenEvaluate(String path, Object defaultValue, Object... data) {
		TokenPattern<?> found = find(path);
		if(found == null) return defaultValue;
		return found.evaluate(data);
	}

	public Object findThenEvaluateLazyDefault(String path, Supplier<Object> defaultValue, Object... data) {
		TokenPattern<?> found = find(path);
		if(found == null) return defaultValue.get();
		return found.evaluate(data);
	}

    public Object evaluate(Object... data) {
    	SimplificationDomain simplified = new SimplificationDomain(this, data).simplifyFully();

    	TokenPatternMatch simplifiedSource = simplified.pattern.source;

    	PatternEvaluator evaluator = simplifiedSource.getEvaluator();
    	if(evaluator == null) {
    		throw new PatternEvaluator.NoEvaluatorException("Missing evaluator for pattern " + simplifiedSource);
		}
		return evaluator.evaluate(simplified.pattern, simplified.data);
	}

	public static Object evaluate(TokenPattern<?> pattern, Object... data) {
		SimplificationDomain domain = new SimplificationDomain(pattern, data).simplifyFully();
    	return domain.pattern.evaluate(domain.data);
	}

	public void simplify(SimplificationDomain domain) {
    	//domain.pattern == this
		Consumer<SimplificationDomain> simplificationFunction = source.getSimplificationFunction();
		if(simplificationFunction != null) {
			simplificationFunction.accept(domain);
		}
	}

	public static class SimplificationDomain {
    	public TokenPattern<?> pattern;
    	public Object[] data;

		public SimplificationDomain(TokenPattern<?> pattern, Object[] data) {
			this.pattern = pattern;
			this.data = data;
		}

		public SimplificationDomain simplifyOnce() {
			pattern.simplify(this);
			return this;
		}

		public SimplificationDomain simplifyFully() {
			TokenPattern<?> previous = null;
			while(pattern != previous) {
				previous = pattern;
				simplifyOnce();
			}
			return this;
		}
	}

	public Object getHeldData() {
		return isDataHeld() ? heldData.get() : null;
	}

	public boolean isDataHeld() {
		return heldData != null && holdsData != null && holdsData.get();
	}

	public Object setHeldData(Object heldData) {
		if(!isDataHeld()) {
			if(this.heldData == null) this.heldData = new ThreadLocal<>();
			if(this.holdsData == null) this.holdsData = ThreadLocal.withInitial(()->false);
		}
		this.heldData.set(heldData);
		this.holdsData.set(true);
		return heldData;
	}
}
