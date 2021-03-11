package com.energyxxer.prismarine.reporting;

import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.NoticeType;
import com.energyxxer.enxlex.report.StackTrace;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

public class PrismarineException extends RuntimeException {

    private final Type type;
    private final Notice notice;
    private final TokenPattern<?> cause;
    private boolean breaking = false;

    public PrismarineException(Type type, String message, @NotNull TokenPattern<?> cause, ISymbolContext ctx) {
        this(type, message, cause, ctx.getCompiler().getCallStack().getView(cause));
    }

    private PrismarineException(Type type, String message, @NotNull TokenPattern<?> cause, StackTrace stackTrace) {
        super(message);
        this.type = type;

        if(type == Type.IMPOSSIBLE) {
            StackTraceElement[] javaStackTrace = Thread.currentThread().getStackTrace();
            for(int i = 1; i < javaStackTrace.length; i++) {
                if(!javaStackTrace[i].getClassName().equals(getClass().getName())) {

                    message += " (" + javaStackTrace[i].getFileName() + ":" + javaStackTrace[i].getLineNumber() + ") Please report as soon as possible";
                    break;
                }
            }
        }

        this.notice = new Notice(NoticeType.ERROR, message, message, cause).setStackTrace(stackTrace);

        for(StackTrace.StackTraceElement frame : stackTrace.getElements()) {
            TokenSource source = frame.getPattern().getSource();
            notice.pointToSource(source);
        }
        this.cause = cause;

    }

    public Type getType() {
        return type;
    }

    public Notice getNotice() {
        return notice;
    }

    public void expandToUncaught() {
        notice.setExtendedMessage("Uncaught " + type.getHumanReadableName() + ": " + notice.getExtendedMessage());
    }

    public boolean isBreaking() {
        return breaking;
    }

    public PrismarineException setBreaking(boolean breaking) {
        this.breaking = breaking;
        return this;
    }

    public TokenPattern<?> getCausePattern() {
        return cause;
    }

    @Override
    public String toString() {
        return type.getHumanReadableName() + ": " + notice.getExtendedMessage();
    }

    public static class Grouped extends RuntimeException implements Iterable<PrismarineException> {
        private final ArrayList<PrismarineException> exceptions;

        public Grouped(ArrayList<PrismarineException> exceptions) {
            this.exceptions = exceptions;
        }

        public ArrayList<PrismarineException> getExceptions() {
            return exceptions;
        }

        @NotNull
        @Override
        public Iterator<PrismarineException> iterator() {
            return exceptions.iterator();
        }
    }


    public static class Type {
        public static final Type IMPOSSIBLE = new Type("Impossible Error");
        public static final Type INTERNAL_EXCEPTION = new Type("Internal Exception");

        private final String humanReadableName;

        public Type(String humanReadableName) {
            this.humanReadableName = humanReadableName;
        }

        public String getHumanReadableName() {
            return humanReadableName;
        }
    }
}
