package com.energyxxer.enxlex.lexical_analysis.token;

import com.energyxxer.enxlex.lexical_analysis.profiles.LexerProfile;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;

import java.util.ArrayList;
import java.util.Iterator;

public class TokenStream implements Iterable<Token> {
	
	public ArrayList<Token> tokens = new ArrayList<>();

	private boolean includeInsignificantTokens = false;

	private LexerProfile profile = null;

	public TokenStream() {includeInsignificantTokens = false;}
	public TokenStream(boolean includeInsignificantTokens) {
		this.includeInsignificantTokens = includeInsignificantTokens;
	}
	
	public final void write(Token token) {
		write(token, false);
	}
	
	public final void write(Token token, boolean skip) {
		if(skip || (profile == null || !profile.filter(token))) {
			onWrite(token);
			if(profile == null || (includeInsignificantTokens || token.isSignificant()))
				tokens.add(token);
		}
	}

	public void setProfile(LexerProfile profile) {
		this.profile = profile;
	}

	public void onWrite(Token token) {}

	@Override
	public Iterator<Token> iterator() {
		return tokens.iterator();
	}

	@Override
	public String toString() {
		return "TokenStream{" +
				"tokens=" + tokens +
				", includeInsignificantTokens=" + includeInsignificantTokens +
				'}';
	}
}
