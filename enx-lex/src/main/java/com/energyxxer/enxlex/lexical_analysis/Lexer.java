package com.energyxxer.enxlex.lexical_analysis;

import com.energyxxer.enxlex.lexical_analysis.inspections.InspectionModule;
import com.energyxxer.enxlex.lexical_analysis.profiles.LexerProfile;
import com.energyxxer.enxlex.lexical_analysis.summary.SummaryModule;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.lexical_analysis.token.TokenStream;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.suggestions.SuggestionModule;

import java.util.ArrayList;

public abstract class Lexer {

    protected TokenStream stream;

    protected ArrayList<Notice> notices = new ArrayList<>();

    protected SuggestionModule suggestionModule = null;
    protected SummaryModule summaryModule = null;
    protected InspectionModule inspectionModule = null;

    protected int currentIndex = 0;

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }


    public TokenStream getStream() {
        return stream;
    }

    public ArrayList<Notice> getNotices() {
        return notices;
    }

    public SuggestionModule getSuggestionModule() {
        return suggestionModule;
    }

    public abstract void start(TokenSource source, String str, LexerProfile profile);

    public void setSuggestionModule(SuggestionModule suggestionModule) {
        this.suggestionModule = suggestionModule;
    }

    public SummaryModule getSummaryModule() {
        return summaryModule;
    }

    public void setSummaryModule(SummaryModule summaryModule) {
        this.summaryModule = summaryModule;
    }

    public InspectionModule getInspectionModule() {
        return inspectionModule;
    }

    public void setInspectionModule(InspectionModule inspectionModule) {
        this.inspectionModule = inspectionModule;
    }


    public abstract String getFullText();

    public abstract int getLookingIndexTrimmed();

    public abstract Token retrieveTokenOfType(TokenType type);
    public abstract Token retrieveAnyToken();

    public abstract boolean isRangeWhitespace(int startIndex, int endIndex);

    public abstract int getFileLength();

    public void clear() {
        stream.clear();
    };
}
