package com.energyxxer.enxlex.suggestions;

import com.energyxxer.enxlex.lexical_analysis.Lexer;

import java.text.Collator;
import java.util.*;

public class SuggestionModule {

    public enum SuggestionStatus {
        ENABLED,
        DISABLED
    }
    private Lexer lexer;

    private final int originalSuggestionIndex;
    private boolean changedSuggestionIndex = false;
    private int suggestionIndex;
    private int caretIndex;
    private final Stack<SuggestionStatus> statusStack = new Stack<>();

    private final ArrayList<Suggestion> suggestions = new ArrayList<>();
    private final HashMap<Character, PairCompletionRanges> pairCompletionRanges = new HashMap<>();

    private String[] lookingAtMemberPath = null;

    public SuggestionModule(int suggestionIndex, int caretIndex) {
        originalSuggestionIndex = suggestionIndex;
        this.suggestionIndex = suggestionIndex;
        this.caretIndex = caretIndex;
    }

    public void addSuggestion(Suggestion prediction) {
        if(!suggestions.contains(prediction)) {
            if(prediction instanceof LiteralSuggestion && prediction.hasTag(SuggestionTags.LITERAL_SORT)) {
                int insertionIndex = suggestions.size();
                Suggestion previousSuggestion;
                while(
                        insertionIndex-1 >= 0 &&
                        ((previousSuggestion = suggestions.get(insertionIndex-1)) instanceof LiteralSuggestion) &&
                                Collator.getInstance(Locale.ENGLISH).compare(((LiteralSuggestion) previousSuggestion).getLiteral(), ((LiteralSuggestion) prediction).getLiteral()) > 0
                ) {
                    insertionIndex--;
                }

                suggestions.add(insertionIndex, prediction);
            } else {
                suggestions.add(prediction);
            }
        }
    }

    public void addPairCompletionRange(int start, int end, char opening, char closing) {
        if(pairCompletionRanges.containsKey(opening)) {
            pairCompletionRanges.get(opening).put(start, end);
        } else {
            PairCompletionRanges ranges = new PairCompletionRanges(opening, closing);
            pairCompletionRanges.put(opening, ranges);
            ranges.put(start, end);
        }
    }

    public Character getPairCompletionAtIndex(char opening, int index) {
        if(pairCompletionRanges.containsKey(opening)) {
            PairCompletionRanges ranges = pairCompletionRanges.get(opening);
            if(ranges.isInRange(index)) {
                return ranges.getClosingSymbol();
            } else {
                return null;
            }
        }
        return null;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public int getOriginalSuggestionIndex() {
        return originalSuggestionIndex;
    }

    public int getSuggestionIndex() {
        return suggestionIndex;
    }

    public void setSuggestionIndex(int index) {
        this.suggestionIndex = index;
        this.changedSuggestionIndex = true;
    }

    public int getCaretIndex() {
        return caretIndex;
    }

    public void setCaretIndex(int caretIndex) {
        this.caretIndex = caretIndex;
    }

    public Lexer getLexer() {
        return lexer;
    }

    public void setLexer(Lexer lexer) {
        this.lexer = lexer;
    }

    public boolean isAtSuggestionIndex(int index) {
        return suggestionIndex >= index && suggestionIndex <= lexer.getLookingIndexTrimmed();
    }

    public boolean shouldSuggest() {
        return !statusStack.isEmpty() && statusStack.peek() == SuggestionStatus.ENABLED;
    }

    public void pushStatus(SuggestionStatus status) {
        statusStack.push(status);
    }

    public SuggestionStatus popStatus() {
        return statusStack.pop();
    }

    public void setLookingAtMemberPath(String[] lookingAtMemberPath) {
        this.lookingAtMemberPath = lookingAtMemberPath;
    }

    public String[] getLookingAtMemberPath() {
        return lookingAtMemberPath;
    }

    public boolean changedSuggestionIndex() {
        return changedSuggestionIndex;
    }
}
