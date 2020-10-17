package com.energyxxer.prismarine.state;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.report.StackTrace;
import com.energyxxer.prismarine.Prismarine;
import com.energyxxer.prismarine.PrismarineLanguageUnit;

import java.util.ArrayList;
import java.util.Stack;

public class CallStack {

    public static class Call {

        String calledFunction;

        TokenPattern<?> calledPattern;
        PrismarineLanguageUnit calledFile;
        TokenPattern<?> calledBy;
        public Call(TokenPattern<?> calledPattern, PrismarineLanguageUnit calledFile, TokenPattern<?> calledBy) {
            this("<anonymous function>", calledPattern, calledFile, calledBy);
        }
        public Call(String calledFunction, TokenPattern<?> calledPattern, PrismarineLanguageUnit calledFile, TokenPattern<?> calledBy) {
            this.calledFunction = calledFunction;
            this.calledPattern = calledPattern;
            this.calledFile = calledFile;
            this.calledBy = calledBy;
        }

        @Override
        public String toString() {
            return "at " + calledFile.getPrettyName() + " ~ " + calledFunction + " (" + calledBy.getFile().getName() + ":" + calledBy.getStringLocation().line + ")\n";
        }
    }

    private Stack<Call> stack = new Stack<>();

    public Call push(Call item) {
        return stack.push(item);
    }

    public Call pop() {
        return stack.pop();
    }

    public Call peek() {
        return stack.peek();
    }

    public StackTrace getView(TokenPattern<?> leaf) {
        ArrayList<StackTrace.StackTraceElement> elements = new ArrayList<>();
        for(int i = stack.size()-1; i >= 0; i--) {
            Call call = stack.get(i);
            TokenPattern<?> calledBy = i < stack.size()-1 ? stack.get(i+1).calledBy : leaf;
            elements.add(new StackTrace.StackTraceElement("at " + (call.calledFile != null ? call.calledFile.getPrettyName() : "<internal function>") + " ~ " + call.calledFunction, calledBy, calledBy.getFile().equals(Prismarine.NULL_FILE) ? "standard library file" : null));
        }
        return new StackTrace(elements);
    }
}
