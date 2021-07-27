package com.energyxxer.enxlex.lexical_analysis.token;

import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.Report;
import com.energyxxer.util.ObjectPool;
import com.energyxxer.util.StringBounds;
import com.energyxxer.util.StringLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Class containing a value, or token, its type, source file and location within
 * it.
 */
public class Token {
	public static final ThreadLocal<ObjectPool<ArrayList<Token>>> TOKEN_LIST_POOL = ThreadLocal.withInitial(() -> new ObjectPool<>(ArrayList::new, ArrayList::clear));

	public String value;
	public TokenType type;
	public TokenSource source;
	public StringLocation loc;

	private HashMap<String, Object> attributes;
	private HashMap<TokenSection, String> subSections;
	private ArrayList<Notice> attachedNotices;

	public ArrayList<String> tags = null;

    public Token(String value, TokenSource source, StringLocation loc) {
		this.value = value;
		this.type = TokenType.UNKNOWN;
		this.source = source;
		this.loc = loc;
	}

	public Token(String value, TokenSource source, StringLocation loc, HashMap<TokenSection, String> subSections) {
		this.value = value;
		this.type = TokenType.UNKNOWN;
		this.source = source;
		this.loc = loc;
		this.subSections = subSections;
	}

	public Token(String value, TokenType tokenType, TokenSource source, StringLocation loc) {
		this.value = value;
		this.type = (tokenType != null) ? tokenType : TokenType.UNKNOWN;
		this.source = source;
		this.loc = loc;
	}

	public Token(String value, TokenType tokenType, TokenSource source, StringLocation loc, HashMap<TokenSection, String> subSections) {
		this.value = value;
		this.type = (tokenType != null) ? tokenType : TokenType.UNKNOWN;
		this.source = source;
		this.loc = loc;
		this.subSections = subSections;
	}

	public boolean isSignificant() {
		return type.isSignificant();
	}

	public String getLocation() {
		return source.getPrettyName() + ":" + loc.line + ":" + loc.column + "#" + loc.index;
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
		return new Token(s.toString(), type, tokens[0].source, tokens[0].loc);
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

		if (!Objects.equals(value, token.value)) return false;
		if (!Objects.equals(type, token.type)) return false;
		if (!Objects.equals(source, token.source)) return false;
		if (!Objects.equals(loc, token.loc)) return false;
		return Objects.equals(attributes, token.attributes);
	}

	@Override
	public int hashCode() {
		int result = value != null ? value.hashCode() : 0;
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (source != null ? source.hashCode() : 0);
		result = 31 * result + (loc != null ? loc.hashCode() : 0);
		result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
		return result;
	}

	public StringBounds getStringBounds() {
		return new StringBounds(loc, new StringLocation(loc.index + value.length(), loc.line, loc.column + value.length()));
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
}
