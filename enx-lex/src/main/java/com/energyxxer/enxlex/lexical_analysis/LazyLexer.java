package com.energyxxer.enxlex.lexical_analysis;

import com.energyxxer.enxlex.lexical_analysis.profiles.LexerContext;
import com.energyxxer.enxlex.lexical_analysis.profiles.LexerProfile;
import com.energyxxer.enxlex.lexical_analysis.profiles.ScannerContextResponse;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.lexical_analysis.token.TokenStream;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.NoticeType;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.util.StringLocationCache;
import com.energyxxer.util.logger.Debug;

import java.util.ArrayList;

public class LazyLexer extends Lexer {

    private final TokenPatternMatch pattern;

    public LazyLexer(TokenStream stream, TokenPatternMatch pattern) {
        this.stream = stream;
        this.pattern = pattern;
    }

    private String fileContents = null;
    private final StringLocationCache lineCache = new StringLocationCache();
    private LexerProfile profile = null;

    private TokenSource source;

    private TokenMatchResponse matchResponse = null;

    private boolean running = false;

    @Override
    public void start(TokenSource source, String str, LexerProfile profile) {
        start(source, str, profile, pattern);
    }
    public void start(TokenSource source, String str, LexerProfile profile, TokenPatternMatch pattern) {
        if(running) {
            Debug.log("Starting an already running lexer!");
        }
        running = true;
        setCurrentIndex(0);
        resetInsignificantTokens();
        try {
            this.source = source;
            this.fileContents = str;
            this.profile = profile;

            if (getSummaryModule() != null) getSummaryModule().onStart(this);

            lineCache.setText(fileContents);
            lineCache.prepopulate();

            {
                Token header = new Token("", TokenType.FILE_HEADER, source, 0, 0, 0);
                profile.putHeaderInfo(header);
                stream.write(header);
            }

            matchResponse = pattern.match(0, this);

            if (matchResponse.matched) {
                matchResponse.pattern.validate();
                for (Token token : matchResponse.pattern.flattenTokens(new ArrayList<>())) {
                    token.dumpNotices(notices);
                    stream.write(token);
                }
            } else {
                this.notices.add(new Notice(NoticeType.ERROR, matchResponse.getErrorMessage(), matchResponse.faultyToken));
            }

            {
                Token eof = new Token("", TokenType.END_OF_FILE, source, 0, 0, 0);
                lineCache.getLocationForOffset(fileContents.length(), eof);
                stream.write(eof);
            }

            if (getSummaryModule() != null) getSummaryModule().onEnd(this);
        } finally {
            running = false;
            this.profile = null;
            this.lineCache.clear();
        }
    }

    public String getFullText() {
        return fileContents;
    }

    public String getLookingAt() {
        return fileContents.substring(currentIndex);
    }

    public String getLookingAtTrimmed() {
        return fileContents.substring(getLookingIndexTrimmed());
    }

    @Override
    public int getLookingIndexTrimmed() {
        int index = currentIndex;
        while(index < fileContents.length() && Character.isWhitespace(fileContents.charAt(index))) index++;
        return index;
    }

    private ArrayList<Token> tempInsignificantTokens;
    private Token[] lastInsignificantTokens;
    private int lastInsignificantTokensIndex = -1;
    private int lastInsignificantTokensEndIndex = -1;

    public void resetInsignificantTokens() {
        if(tempInsignificantTokens != null) tempInsignificantTokens.clear();
        lastInsignificantTokens = null;
        lastInsignificantTokensIndex = -1;
        lastInsignificantTokensEndIndex = -1;
    }

    private Token[] retrieveInsignificantTokens() {
        if(currentIndex == lastInsignificantTokensIndex) {
            if(lastInsignificantTokens != null) currentIndex = lastInsignificantTokensEndIndex;
            return lastInsignificantTokens;
        }

        if(tempInsignificantTokens != null) tempInsignificantTokens.clear();
        lastInsignificantTokens = null;
        lastInsignificantTokensIndex = currentIndex;
        lastInsignificantTokensEndIndex = currentIndex;
        boolean any = true;
        while(any) {
            int lookingIndexTrimmed = getLookingIndexTrimmed();
            any = false;
            for (LexerContext context : profile.insignificantContexts) {
                ScannerContextResponse response = context.analyze(
                        fileContents,
                        context.ignoreLeadingWhitespace() ?
                                lookingIndexTrimmed :
                                currentIndex,
                        profile);
                if (response.success && !response.value.isEmpty()) {
                    Token token = new Token(response.value, response.tokenType, source, 0, 0, 0, response.subSections);
                    lineCache.getLocationForOffset(
                            context.ignoreLeadingWhitespace() ?
                                    getLookingIndexTrimmed() :
                                    getCurrentIndex(),
                            token
                    );
                    if (response.errorMessage != null) {
                        token.attachNotice(new Notice(NoticeType.ERROR, response.errorMessage, token));
                    }
                    if(tempInsignificantTokens == null) tempInsignificantTokens = new ArrayList<>();
                    tempInsignificantTokens.add(token);
                    currentIndex = lookingIndexTrimmed + response.endLocation.index;
                    any = true;
                    response.unlock();
                    break;
                }
                response.unlock();
            }
        }
        if(tempInsignificantTokens != null && !tempInsignificantTokens.isEmpty()) {
            lastInsignificantTokens = tempInsignificantTokens.toArray(new Token[0]);
            lastInsignificantTokensEndIndex = currentIndex;
            return lastInsignificantTokens;
        }
        return null;
    }

    private StringBuffer sb = new StringBuffer();

    @Override
    public synchronized Token retrieveTokenOfType(TokenType type) {
        int startIndex = currentIndex;
        try {
            Token[] beforeTokens = retrieveInsignificantTokens();
            int lookingIndexTrimmed = getLookingIndexTrimmed();
            for (LexerContext context : profile.contexts) {
                if (context.handlesType(type)) {
                    ScannerContextResponse response = context.analyzeExpectingType(fileContents, context.ignoreLeadingWhitespace() ?
                            lookingIndexTrimmed :
                            currentIndex, type, profile);
                    if (response.success && response.tokenType == type) {
                        Token token = new Token(response.value, response.tokenType, source, 0, 0, 0, response.subSections);
                        lineCache.getLocationForOffset(context.ignoreLeadingWhitespace() ?
                                getLookingIndexTrimmed() :
                                getCurrentIndex(),
                                token
                        );
                        if (response.errorMessage != null) {
                            token.attachNotice(new Notice(NoticeType.ERROR, response.errorMessage, token));
                        }
                        token.setBeforeTokens(beforeTokens);
                        response.unlock();
                        return token;
                    }
                    response.unlock();
                }
            }
            if (type == TokenType.END_OF_FILE) {
                if (getLookingIndexTrimmed() == fileContents.length()) {
                    Token token = new Token("", TokenType.END_OF_FILE, source, 0, 0, 0);
                    lineCache.getLocationForOffset(fileContents.length(), token);
                    token.setBeforeTokens(beforeTokens);
                    return token;
                }
            }
            if (type == TokenType.NEWLINE) {
                int index = currentIndex;
                while (index < fileContents.length() && fileContents.charAt(index) != '\n' && Character.isWhitespace(fileContents.charAt(index)))
                    index++;
                if (index < fileContents.length() && fileContents.charAt(index) == '\n') {
                    Token token = new Token("\n", TokenType.NEWLINE, source, 0, 0, 0);
                    lineCache.getLocationForOffset(index, token);
                    token.setBeforeTokens(beforeTokens);
                    return token;
                }
            }
            if (type == TokenType.UNKNOWN || type == null) {
                sb.setLength(0);
                for (int i = getLookingIndexTrimmed(); i < fileContents.length(); i++) {

                    char lastChar = '\u0000';

                    if (i > 0) lastChar = fileContents.charAt(i - 1);

                    if (sb.length() > 0 && lastChar != '\u0000' && !profile.canMerge(lastChar, fileContents.charAt(i))) {
                        Token token = new Token(sb.toString(), TokenType.UNKNOWN, source, 0, 0, 0);
                        sb.setLength(0);
                        lineCache.getLocationForOffset(getLookingIndexTrimmed(), token);
                        token.setBeforeTokens(beforeTokens);
                        return token;
                    }
                    sb.append(fileContents.charAt(i));
                }
                if (sb.length() > 0) {
                    Token token = new Token(sb.toString(), TokenType.UNKNOWN, source, 0, 0, 0);
                    sb.setLength(0);
                    lineCache.getLocationForOffset(getLookingIndexTrimmed(), token);
                    token.setBeforeTokens(beforeTokens);
                    return token;
                }
            }
            return null;
        } finally {
            currentIndex = startIndex;
        }
    }

    @Override
    public synchronized Token retrieveAnyToken() {
        int lookingIndexTrimmed = getLookingIndexTrimmed();
        for (LexerContext context : profile.contexts) {
            ScannerContextResponse response = context.analyze(
                    fileContents,
                    context.ignoreLeadingWhitespace() ?
                            lookingIndexTrimmed :
                            currentIndex,
                    profile);
            if (response.success && !response.value.isEmpty()) {
                Token token = new Token(response.value, response.tokenType, source, 0, 0, 0, response.subSections);
                lineCache.getLocationForOffset(
                        context.ignoreLeadingWhitespace() ?
                                getLookingIndexTrimmed() :
                                getCurrentIndex(),
                        token
                );
                if (response.errorMessage != null) {
                    token.attachNotice(new Notice(NoticeType.ERROR, response.errorMessage, token));
                }
                response.unlock();
                return token;
            }
            response.unlock();
        }
        if(getLookingIndexTrimmed() == fileContents.length()) {
            Token eof = new Token("", TokenType.END_OF_FILE, source, 0, 0, 0);
            lineCache.getLocationForOffset(fileContents.length(), eof);
            return eof;
        }
        {
            sb.setLength(0);
            for (int i = getLookingIndexTrimmed(); i < fileContents.length(); i++) {

                char lastChar = '\u0000';

                if (i > 0) lastChar = fileContents.charAt(i - 1);

                if (sb.length() > 0 && lastChar != '\u0000' && !profile.canMerge(lastChar, fileContents.charAt(i))) {
                    Token token = new Token(sb.toString(), TokenType.UNKNOWN, source, 0, 0, 0);
                    sb.setLength(0);
                    lineCache.getLocationForOffset(getLookingIndexTrimmed(), token);
                    return token;
                }
                sb.append(fileContents.charAt(i));
            }
            if(sb.length() > 0) {
                Token token = new Token(sb.toString(), TokenType.UNKNOWN, source, 0, 0, 0);
                sb.setLength(0);
                lineCache.getLocationForOffset(getLookingIndexTrimmed(), token);
                return token;
            }
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
        return fileContents.length();
    }

    public TokenMatchResponse getMatchResponse() {
        return matchResponse;
    }

    @Override
    public void setSuggestionModule(SuggestionModule suggestionModule) {
        super.setSuggestionModule(suggestionModule);
        if(suggestionModule != null) suggestionModule.setLexer(this);
    }

    @Override
    public void clear() {
        super.clear();
        source = null;
        fileContents = null;
        lineCache.clear();
        profile = null;
        matchResponse = null;
    }
}
