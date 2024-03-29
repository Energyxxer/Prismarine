package com.energyxxer.enxlex.lexical_analysis;

import com.energyxxer.enxlex.lexical_analysis.profiles.LexerContext;
import com.energyxxer.enxlex.lexical_analysis.profiles.LexerProfile;
import com.energyxxer.enxlex.lexical_analysis.profiles.ScannerContextResponse;
import com.energyxxer.enxlex.lexical_analysis.token.*;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.NoticeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For tokenizing any file by rules given by Lexer Profiles
 */
public class EagerLexer extends Lexer {
	
	/*public EagerLexer(File directory, TokenStream stream) {
		this.stream = stream;
		parse(directory);
	}*/

	public EagerLexer(TokenStream stream) {
		this.stream = stream;
	}

	private TokenSource source;

	private final StringBuffer token = new StringBuffer();
	private int line = 0;
	private int column = 0;
	private int index = 0;

	private TokenType tokenType = null;

	private int tokenLine = 0;
	private int tokenColumn = 0;
	private int tokenIndex = 0;

	private HashMap<TokenSection, String> subSections = null;

	private String fileContents;

	@Override
	public synchronized void start(TokenSource source, String str, LexerProfile profile) {
		this.source = source;
		this.fileContents = str;
		stream.setProfile(profile);
		profile.setStream(stream);
		line = column = index = tokenLine = tokenColumn = tokenIndex = 0;
		setCurrentIndex(0);
		token.setLength(0);

		{
			Token header = new Token("", TokenType.FILE_HEADER, source, 0, 0, 0);
			profile.putHeaderInfo(header);
			flush(header);
		}

		mainLoop: for(int i = 0; i <= str.length(); i++) {
			this.index = i;
			String c = "";

			boolean isClosingIteration = true;

			if(i < str.length()) {
				c = Character.toString(str.charAt(i));
				isClosingIteration = false;
			}

			for(LexerContext ctx : profile.contexts) {
				if(ctx.getCondition() == LexerContext.ContextCondition.LEADING_WHITESPACE && token.length() > 0) continue;
				if(ctx.getCondition() == LexerContext.ContextCondition.LINE_START && column != 0) continue;
				ScannerContextResponse response = ctx.analyze(str, i, profile);
				if(response.errorMessage != null) {
					Notice notice = new Notice(NoticeType.ERROR, response.errorMessage);
					notice.setSourceLocation(source, response.errorIndex, response.errorLength);
					notices.add(notice);
				}
				if(response.success) {
					flush();
					updateTokenPos();
					line += response.endLocation.line;
					if(response.endLocation.line == 0) column += response.endLocation.column;
					else column = response.endLocation.column;
					i += response.value.length()-1;
					token.append(response.value);
					tokenType = response.tokenType;
					subSections = response.subSections;
					response.unlock();
					flush();
					continue mainLoop;
				}
			}

			if (c.equals("\n")) {
				if(profile.useNewlineTokens()) {
					flush();
					updateTokenPos();
					token.append('\n');
					tokenType = TokenType.NEWLINE;
				}
				line++;
				column = 0;
			} else {
				column++;
			}

			if(isClosingIteration) {
				flush();
				break;
			}

			if(Character.isWhitespace(c.charAt(0))) {
				//Is whitespace.
				flush();
				continue;
			} else if(token.length() == 0) {
				//Is start of a new token.
				updateTokenPos();
				tokenType = null;
			}

			char lastChar = '\u0000';

			if(i > 0) lastChar = str.charAt(i-1);

			if(lastChar != '\u0000' && !profile.canMerge(lastChar,c.charAt(0))) {
				flush();
				updateTokenPos();
			}
			token.append(c);
		}
		flush();

		updateTokenPos();
		token.setLength(0);
		tokenType = TokenType.END_OF_FILE;
		flush();
	}

	private void updateTokenPos() {
		tokenLine = line;
		tokenColumn = column;
		tokenIndex = index;
	}

	private void flush() {
		if(token.length() > 0 || (tokenType == TokenType.FILE_HEADER || tokenType == TokenType.END_OF_FILE))
			flush(new Token(token.toString(), tokenType, source, tokenIndex, tokenLine, tokenColumn, subSections));

		token.setLength(0);
		tokenType = null;
		subSections = null;
	}
	
	private void flush(Token token) {
		stream.write(token);
	}

	public ArrayList<Notice> getNotices() {
		return notices;
	}

	public TokenStream getStream() {
		return stream;
	}

	@Override
	public String getFullText() {
		return fileContents;
	}

	private static int findIndexForTokenList(int index, List<Token> list)
	{
		if (list.isEmpty()) return 0;

		int minIndex = 0; // inclusive
		int maxIndex = list.size(); // exclusive

		if (index < list.get(minIndex).index)
		{
			return minIndex;
		}
		if (index > list.get(maxIndex-1).index)
		{
			return maxIndex;
		}

		while (minIndex < maxIndex)
		{
			int pivotIndex = (minIndex + maxIndex) / 2;

			int pivotId = list.get(pivotIndex).index;
			if (pivotId == index)
			{
				return pivotIndex;
			}
			else if (index > pivotId)
			{
				minIndex = pivotIndex + 1;
			}
			else
			{
				maxIndex = pivotIndex;
			}
		}

		return minIndex;
	}

	@Override
	public int getLookingIndexTrimmed() {
		int tokenIndex = findIndexForTokenList(currentIndex, stream.tokens);
		while(tokenIndex < stream.tokens.size() && !stream.tokens.get(tokenIndex).isSignificant()) {
			tokenIndex++;
		}
		if(tokenIndex < stream.tokens.size()) {
			return stream.tokens.get(tokenIndex).index;
		}
		return currentIndex;
	}

	@Override
	public Token retrieveTokenOfType(TokenType type) {
		Token token = retrieveAnyToken();
		if(token != null && (token.type == type || (type == null && token.type == TokenType.UNKNOWN))) {
			return token;
		}
		return null;
	}

	@Override
	public Token retrieveAnyToken() {
		int tokenIndex = findIndexForTokenList(currentIndex, stream.tokens);
		while(tokenIndex < stream.tokens.size() && !stream.tokens.get(tokenIndex).isSignificant()) {
			tokenIndex++;
		}
		if(tokenIndex < stream.tokens.size()) {
			return stream.tokens.get(tokenIndex);
		}
		return null;
	}

	@Override
	public boolean isRangeWhitespace(int startIndex, int endIndex) {
		for(int i = startIndex; i < endIndex; i++) {
			if(!Character.isWhitespace(fileContents.charAt(i))) return false;
		}
		return true;
	}

	@Override
	public int getFileLength() {
		return stream.tokens.isEmpty() ? 0 : stream.tokens.get(stream.tokens.size()-1).getStringBounds().end.index;
	}

	@Override
	public void clear() {
		super.clear();
		source = null;
		fileContents = null;
	}
}
