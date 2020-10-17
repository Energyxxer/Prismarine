package com.energyxxer.prismarine.in;

import com.energyxxer.commodore.util.io.CompoundInput;
import com.energyxxer.commodore.util.io.DirectoryCompoundInput;
import com.energyxxer.enxlex.lexical_analysis.LazyLexer;
import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.lexical_analysis.token.TokenStream;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.report.Report;
import com.energyxxer.prismarine.PrismarineLanguageUnitConfiguration;
import com.energyxxer.prismarine.summaries.PrismarineSummaryModule;
import com.energyxxer.prismarine.util.FileUtil;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import com.energyxxer.prismarine.worker.tasks.SetupProductionsTask;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;

import static com.energyxxer.prismarine.Prismarine.DEFAULT_CHARSET;

public class ProjectReader {
    private CompoundInput input;
    private PrismarineProjectWorker worker;

    private HashMap<Path, Result> cache = new HashMap<>();
    private HashMap<PrismarineLanguageUnitConfiguration, Lexer> lexers = new HashMap<>();

    public ProjectReader(CompoundInput input, PrismarineProjectWorker worker) {
        this.input = input;
        this.worker = worker;
    }

    public Query startQuery(Path relativePath) {
        return new Query(this, relativePath);
    }

    public void refreshPatterns() {
        for(Result result : cache.values()) {
            result.pattern = null;
            result.summary = null;
        }
        lexers.clear();
    }

    public PrismarineProjectWorker getWorker() {
        return worker;
    }

    public void setWorker(PrismarineProjectWorker worker) {
        this.worker = worker;
    }

    public static class Query {
        protected ProjectReader reader;
        protected final Path relativePath;

        protected PrismarineLanguageUnitConfiguration unitConfig;

        protected boolean needsBytes = false;
        protected boolean needsString = false;
        protected boolean needsJSON = false;
        protected boolean needsPattern = false;
        protected boolean needsSummary = false;

        protected boolean skipIfNotChanged = false;

        protected boolean skipSummaryIfMatchFailed = false;

        protected Query(ProjectReader reader, Path relativePath) {
            this.reader = reader;
            this.relativePath = relativePath;
        }

        public Query needsBytes() {
            this.needsBytes = true;
            return this;
        }

        public Query needsString() {
            this.needsBytes();
            this.needsString = true;
            return this;
        }

        public Query needsJSON() {
            this.needsString();
            this.needsJSON = true;
            return this;
        }

        public Query needsPattern(PrismarineLanguageUnitConfiguration unitConfig) {
            this.needsString();
            this.needsPattern = true;
            this.unitConfig = unitConfig;
            return this;
        }

        public Query needsSummary(PrismarineLanguageUnitConfiguration unitConfig, boolean skipIfMatchFailed) {
            this.needsPattern(unitConfig);
            this.needsSummary = true;
            this.skipSummaryIfMatchFailed = skipIfMatchFailed;
            return this;
        }

        public Query skipIfNotChanged() {
            this.skipIfNotChanged = true;
            return this;
        }

        public Result perform() throws IOException {
            return reader.performQuery(this);
        }
    }

    private Result performQuery(Query query) throws IOException {
        //ownFiles.add(query.relativePath);

        Result existing = cache.get(query.relativePath);

        String relativePathStr = query.relativePath.toString().replace(File.separatorChar, '/');

        byte[] bytes;
        try (InputStream is = input.get(relativePathStr)) {
            if(is == null) throw new FileNotFoundException(query.relativePath.toString());
            bytes = FileUtil.readAllBytes(is);
        }

        int hashCode = Arrays.hashCode(bytes);

        if(existing != null && existing.hashCode == hashCode && existing.matchesRequirements(query)) {
            //Return cached result if it contains what the caller needs and is up to date
            if(query.skipIfNotChanged) return null;
            return existing;
        }

        //Create a new result
        Result result = new Result();
        result.relativePath = query.relativePath;
        result.hashCode = hashCode;
        result.bytes = bytes;
        if(query.needsString) result.string = new String(bytes, DEFAULT_CHARSET);
        if(query.needsJSON) {
            result.jsonObject = new Gson().fromJson(result.string, JsonObject.class);
        }

        File file;
        if(input instanceof DirectoryCompoundInput) {
            file = input.getRootFile().toPath().resolve(query.relativePath).toFile();
        } else {
            file = input.getRootFile();
        }

        result = populateParseResult(query, file, result);

        cache.put(query.relativePath, result);

        return result;
    }

    public Result populateParseResult(Query query, File file, Result result) {
        if(query.needsPattern) {
            Lexer lexer = getLexerForUnitConfig(query.unitConfig);

            PrismarineSummaryModule summary = null;
            if(query.needsSummary) {
                summary = query.unitConfig.createSummaryModule(file, query.relativePath);
                lexer.setSummaryModule(result.summary);
            }

            lexer.start(file, result.string, query.unitConfig.createLexerProfile());
            TokenMatchResponse response = ((LazyLexer) lexer).getMatchResponse();

            if(response.matched) {
                result.pattern = response.pattern;
            }

            if(query.needsSummary && (response.matched || !query.skipSummaryIfMatchFailed)) {
                result.summary = summary;
            }
        }

        return result;
    }

    public Result createResultFromString(String string) {
        Result result = new Result();
        result.string = string;
        return result;
    }

    public void dumpNotices(Report report) {
        for(Lexer lexer : lexers.values()) {
            report.addNotices(lexer.getNotices());
        }
    }

    private Lexer getLexerForUnitConfig(PrismarineLanguageUnitConfiguration unitConfig) {
        if(lexers.containsKey(unitConfig)) return lexers.get(unitConfig);

        Lexer newLexer = new LazyLexer(new TokenStream(), worker.output.get(SetupProductionsTask.INSTANCE).get(unitConfig).FILE);
        lexers.put(unitConfig, newLexer);

        return newLexer;
    }

    public static class Result {
        protected Path relativePath;
        protected int hashCode;
        protected byte[] bytes;
        protected String string;
        protected JsonObject jsonObject;
        protected TokenPattern<?> pattern;
        protected PrismarineSummaryModule summary;

        protected boolean matchesRequirements(Query query) {
            return (!query.needsBytes || bytes != null) ||
                    (!query.needsString || string != null) ||
                    (!query.needsJSON || jsonObject != null) ||
                    (!query.needsPattern || pattern != null) ||
                    (!query.needsSummary  || summary != null)
            ;
        }

        public int getHashCode() {
            return hashCode;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public String getString() {
            return string;
        }

        public JsonObject getJsonObject() {
            return jsonObject;
        }

        public TokenPattern<?> getPattern() {
            return pattern;
        }

        public PrismarineSummaryModule getSummary() {
            return summary;
        }

        public Path getRelativePath() {
            return relativePath;
        }
    }
}
