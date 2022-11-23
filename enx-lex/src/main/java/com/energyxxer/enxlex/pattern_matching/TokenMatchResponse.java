package com.energyxxer.enxlex.pattern_matching;

import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;

import java.util.ArrayList;

public class TokenMatchResponse {
	private static final ThreadLocal<ArrayList<TokenMatchResponse>> reusableResponses = ThreadLocal.withInitial(ArrayList::new);

	public boolean matched;
	public Token faultyToken;
	public int length;
	public int endIndex;
	public TokenPatternMatch expected = null;
	public TokenPattern<?> pattern = null;

	public static final int NO_MATCH = 0;
	public static final int PARTIAL_MATCH = 1;
	public static final int COMPLETE_MATCH = 2;

	public TokenMatchResponse() {
	}

	public int getMatchType() {
		if(matched) return COMPLETE_MATCH;
		if(length > 0) return PARTIAL_MATCH;
		return NO_MATCH;
	}

	@Override
	public String toString() {
		return "TokenMatchResponse{" +
				"matched=" + matched +
				", faultyToken=" + faultyToken +
				", length=" + length +
				", expected=" + expected +
				", pattern=" + pattern +
				", matchType=" + getMatchType() +
				'}';
	}

	private static TokenMatchResponse getOrCreateResponse() {
		ArrayList<TokenMatchResponse> availableResponses = reusableResponses.get();
		if(availableResponses.isEmpty()) {
			return new TokenMatchResponse();
		} else {
			return availableResponses.remove(availableResponses.size()-1);
		}
	}

	public static TokenMatchResponse success(int length, int endIndex, TokenPattern<?> pattern) {
		TokenMatchResponse response = getOrCreateResponse();
		response.matched = true;
		response.faultyToken = null;
		response.length = length;
		response.endIndex = endIndex;
		response.expected = null;
		response.pattern = pattern;
		return response;
	}

	public static TokenMatchResponse failure(Token faultyToken, int length, int endIndex, TokenPatternMatch expected, TokenPattern<?> pattern) {
		TokenMatchResponse response = getOrCreateResponse();
		response.matched = false;
		response.faultyToken = faultyToken;
		response.length = length;
		response.endIndex = endIndex;
		response.expected = expected;
		response.pattern = pattern;
		return response;
	}

	public void discard() {
		reusableResponses.get().add(this);
	}

	public boolean matchedThenDiscard() {
		discard();
		return this.matched;
	}

	public String getErrorMessage() {
		if (!matched) {
			if(faultyToken == null || faultyToken.type == TokenType.END_OF_FILE) {
				return "Expected " + expected.toTrimmedString();
			}
			return "Unexpected token '" + faultyToken.value + "'. " + expected.toTrimmedString() + " expected";
		}
		return null;
	}
}
