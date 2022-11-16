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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class TokenPattern<T> {
	public static final ThreadLocal<ObjectPool<ArrayList<TokenPattern<?>>>> PATTERN_LIST_POOL = ThreadLocal.withInitial(() -> new ObjectPool<>(ArrayList::new, ArrayList::clear));

	protected String name = "";
	protected ArrayList<String> tags = null;
	public TokenPattern parent;
	public final TokenPatternMatch source;
	protected boolean validated = false;
	private ThreadLocal<Object> heldData = null;
	private ThreadLocal<Boolean> holdsData = null;
	private ArrayList<String> findCacheKeys = null;
	private ArrayList<TokenPattern<?>> findCacheValues = null;
	private int findCacheLastIndex = 0;

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
		if(findCacheKeys == null) findCacheKeys = new ArrayList<>();
		if(findCacheValues == null) findCacheValues = new ArrayList<>();

		synchronized(this) {
			findCacheKeys.add(path);
			findCacheValues.add(result);
		}
		return result;
	}
	protected boolean isPathInCache(String path) {
		if(findCacheKeys == null) return false;
		for(int i = 0; i < findCacheKeys.size(); i++) {
			int j = (i + findCacheLastIndex) % findCacheKeys.size();
			if(findCacheKeys.get(j).equals(path)) {
				findCacheLastIndex = j;
				return true;
			}
		}
		return false;
	}
	protected TokenPattern<?> getCachedFindResult(String path) {
		for(int i = 0; i < findCacheKeys.size(); i++) {
			int j = (i + findCacheLastIndex) % findCacheKeys.size();
			if(findCacheKeys.get(j).equals(path)) {
				return findCacheValues.get(j);
			}
		}
		return null;
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
		if(newTag != null) {
			if(tags == null) tags = new ArrayList<>(2);
			tags.add(newTag);
		}
		return this;
	}

	public TokenPattern addTags(List<String> newTags) {
    	if(newTags != null) {
			if(tags == null) tags = new ArrayList<>(2);
    		tags.addAll(newTags);
		}
		return this;
	}

	public List<String> getTags() {
    	return tags;
	}

	public boolean hasTag(String tag) {
    	return tags != null && tags.contains(tag);
	}

	public abstract void validate();

	public boolean isValidated() {
		return validated;
	}
	public abstract void traverse(Consumer<TokenPattern<?>> consumer);

	public <CTX> Object findThenEvaluate(String path, Object defaultValue, CTX ctx, Object[] data) {
		TokenPattern<?> found = find(path);
		if(found == null) return defaultValue;
		return found.evaluate(ctx, data);
	}

	public <CTX> Object findThenEvaluateLazyDefault(String path, Supplier<Object> defaultValue, CTX ctx, Object[] data) {
		TokenPattern<?> found = find(path);
		if(found == null) return defaultValue.get();
		return found.evaluate(ctx, data);
	}

    public <CTX> Object evaluate(CTX ctx, Object[] data) {
    	SimplificationDomain simplified = SimplificationDomain.get(this, ctx, data).simplifyFully();

    	TokenPatternMatch simplifiedSource = simplified.pattern.source;

    	PatternEvaluator<CTX> evaluator = simplifiedSource.getEvaluator();
    	if(evaluator == null) {
    		throw new PatternEvaluator.NoEvaluatorException("Missing evaluator for pattern " + simplifiedSource);
		}
		simplified.unlock();
		return evaluator.evaluate(simplified.pattern, (CTX) simplified.ctx, simplified.data);
	}

	public static <CTX> Object evaluate(TokenPattern<?> pattern, CTX ctx, Object[] data) {
		SimplificationDomain domain = SimplificationDomain.get(pattern, ctx, data).simplifyFully();
		domain.unlock();
    	return domain.pattern.evaluate(domain.ctx, domain.data);
	}

	public void simplify(SimplificationDomain domain) {
    	//domain.pattern == this
		Consumer<SimplificationDomain> simplificationFunction = source.getSimplificationFunction();
		if(simplificationFunction != null) {
			simplificationFunction.accept(domain);
		}
	}

	public abstract int endIndex();

	public static class SimplificationDomain {
		private static ThreadLocal<SimplificationDomain> INSTANCE = ThreadLocal.withInitial(SimplificationDomain::new);
    	public TokenPattern<?> pattern;
		public Object ctx;
    	public Object[] data;
		private boolean locked = false;

		public SimplificationDomain() {
		}

		public static SimplificationDomain get(TokenPattern<?> pattern, Object ctx, Object[] data) {
			SimplificationDomain instance = INSTANCE.get();
			if(instance.locked) {
				throw new IllegalStateException("Must unlock SimplificationDomain before trying to create another. Please report ASAP, and include the stack trace");
			}
			instance.pattern = pattern;
			instance.ctx = ctx;
			instance.data = data;
			instance.locked = true;
			return instance;
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

		public void unlock() {
			locked = false;
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
