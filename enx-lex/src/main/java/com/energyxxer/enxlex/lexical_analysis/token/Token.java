package com.energyxxer.enxlex.lexical_analysis.token;

import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.Report;
import com.energyxxer.util.ObjectPool;
import com.energyxxer.util.StringBounds;
import com.energyxxer.util.StringLocation;

import java.util.*;

/**
 * Class containing a value, or token, its type, source file and location within
 * it.
 */
public class Token extends StringLocation {
	public static final ThreadLocal<ObjectPool<ArrayList<Token>>> TOKEN_LIST_POOL = ThreadLocal.withInitial(() -> new ObjectPool<>(ArrayList::new, ArrayList::clear));

	private Token[] beforeTokens;

	public String value;
	public TokenType type;
	public TokenSource source;

	private HashMap<String, Object> attributes;
	private HashMap<TokenSection, String> subSections;
	private ArrayList<Notice> attachedNotices;

	public ArrayList<String> tags = null;

    public Token(String value, TokenSource source, int index, int line, int column) {
		super(index, line, column);
		this.value = value;
		this.type = TokenType.UNKNOWN;
		this.source = source;
	}

	public Token(String value, TokenSource source, int index, int line, int column, HashMap<TokenSection, String> subSections) {
		super(index, line, column);
		this.value = value;
		this.type = TokenType.UNKNOWN;
		this.source = source;
		this.subSections = subSections;
	}

	public Token(String value, TokenType tokenType, TokenSource source, int index, int line, int column) {
		super(index, line, column);
		this.value = value;
		this.type = (tokenType != null) ? tokenType : TokenType.UNKNOWN;
		this.source = source;
	}

	public Token(String value, TokenType tokenType, TokenSource source, int index, int line, int column, HashMap<TokenSection, String> subSections) {
		super(index, line, column);
		this.value = value;
		this.type = (tokenType != null) ? tokenType : TokenType.UNKNOWN;
		this.source = source;
		this.subSections = subSections;
	}

	public boolean isSignificant() {
		return type.isSignificant();
	}

	public String getLocation() {
		return source.getPrettyName() + ":" + line + ":" + column + "#" + index;
	}

	public TokenSource getSource() {
		return source;
	}

	@Override
	public String toString() {
    	boolean verbose = true;
    	if(verbose) {
    		return type.getHumanReadableName() + " '" + value + "' (" + (isSignificant() ? "" : "in") + "significant)";
		} else
		return value;
	}
	
	public static Token merge(TokenType type, Token... tokens) {
		StringBuilder s = new StringBuilder();
		for(Token t : tokens) {
			s.append(t.value);
		}
		return new Token(s.toString(), type, tokens[0].source, tokens[0].index, tokens[0].line, tokens[0].column);
	}

	public HashMap<TokenSection, String> getSubSections() {
		return subSections;
	}

	public void setSubSections(HashMap<TokenSection, String> subSections) {
		this.subSections = subSections;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Token token = (Token) o;
		return index == token.index && line == token.line && column == token.column && Arrays.equals(beforeTokens, token.beforeTokens) && Objects.equals(value, token.value) && Objects.equals(type, token.type) && Objects.equals(source, token.source) && Objects.equals(attributes, token.attributes) && Objects.equals(subSections, token.subSections) && Objects.equals(attachedNotices, token.attachedNotices) && Objects.equals(tags, token.tags);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(value, type, source, index, line, column, attributes, subSections, attachedNotices, tags);
		result = 31 * result + Arrays.hashCode(beforeTokens);
		return result;
	}

	public StringBounds getStringBounds() {
		return new StringBounds(new StringLocation(index, line, column), new StringLocation(index + value.length(), line, column + value.length()));
	}

	public void dumpNotices(Report report) {
		if(attachedNotices != null) report.addNotices(attachedNotices);
	}

	public void dumpNotices(List<Notice> report) {
		if(attachedNotices != null) report.addAll(attachedNotices);
	}

	public void attachNotice(Notice notice) {
    	if(attachedNotices == null) attachedNotices = new ArrayList<>();
    	attachedNotices.add(notice);
	}

	public void putAttribute(String key, Object value) {
		if(attributes == null) attributes = new HashMap<>();
		attributes.put(key, value);
	}

	public Object getAttribute(String key) {
    	return attributes == null ? null : attributes.get(key);
	}

	public HashMap<String, Object> getAttributes() {
		return attributes;
	}

	public List<String> getTags() {
    	return tags;
	}

	public boolean hasTag(String tag) {
    	return tags != null && tags.contains(tag);
	}

	public Token addTag(String newTag) {
		if(newTag != null) {
			if(tags == null) tags = new ArrayList<>(2);
			tags.add(newTag);
		}
		return this;
	}

	public Token addTags(List<String> newTags) {
		if(newTags != null) {
			if(tags == null) tags = new ArrayList<>(2);
			tags.addAll(newTags);
		}
		return this;
	}

	public Token[] getBeforeTokens() {
		return beforeTokens;
	}

	public void setBeforeTokens(Token[] beforeTokens) {
		this.beforeTokens = beforeTokens;
	}

	public int length() {
    	return value.length();
	}

	public int endIndex() {
    	return index + value.length();
	}

	public int startIndexWithBefore() {
    	if(beforeTokens == null) return index;
    	else return beforeTokens[0].index;
	}

	public int totalLength() {
    	return endIndex() - startIndexWithBefore();
	}

	public ArrayList<Token> flattenTokens(ArrayList<Token> list) {
    	if(beforeTokens != null) {
    		for(Token token : beforeTokens) {
    			token.flattenTokens(list);
			}
		}
		list.add(this);
    	return list;
	}
}
