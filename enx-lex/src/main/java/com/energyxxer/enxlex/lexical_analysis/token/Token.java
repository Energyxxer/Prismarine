package com.energyxxer.enxlex.lexical_analysis.token;

import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.util.StringBounds;
import com.energyxxer.util.StringLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Class containing a value, or token, its type, source file and location within
 * it.
 */
public class Token {
	public String value;
	public TokenType type;
	public TokenSource source;
	public StringLocation loc;
	public HashMap<String, Object> attributes;
	public HashMap<TokenSection, String> subSections;

	public ArrayList<String> tags = new ArrayList<>();
	public ArrayList<Notice> attachedNotices = new ArrayList<>();

    public Token(String value, TokenSource source, StringLocation loc) {
		this.value = value;
		this.type = TokenType.UNKNOWN;
		this.source = source;
		this.loc = loc;
		this.attributes = new HashMap<>();
		this.subSections = new HashMap<>();
	}

	public Token(String value, TokenSource source, StringLocation loc, HashMap<TokenSection, String> subSections) {
		this.value = value;
		this.type = TokenType.UNKNOWN;
		this.source = source;
		this.loc = loc;
		this.attributes = new HashMap<>();
		this.subSections = (subSections != null) ? subSections : new HashMap<>();
	}

	public Token(String value, TokenType tokenType, TokenSource source, StringLocation loc) {
		this.value = value;
		this.type = (tokenType != null) ? tokenType : TokenType.UNKNOWN;
		this.source = source;
		this.loc = loc;
		this.attributes = new HashMap<>();
		this.subSections = new HashMap<>();
	}

	public Token(String value, TokenType tokenType, TokenSource source, StringLocation loc, HashMap<TokenSection, String> subSections) {
		this.value = value;
		this.type = (tokenType != null) ? tokenType : TokenType.UNKNOWN;
		this.source = source;
		this.loc = loc;
		this.attributes = new HashMap<>();
		this.subSections = (subSections != null) ? subSections : new HashMap<>();
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
}
