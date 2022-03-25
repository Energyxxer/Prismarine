package com.energyxxer.prismarine.in;

import com.energyxxer.commodore.util.io.CompoundInput;
import com.energyxxer.enxlex.lexical_analysis.LazyLexer;
import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.lexical_analysis.token.TokenStream;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.report.Report;
import com.energyxxer.prismarine.PrismarineLanguageUnitConfiguration;
import com.energyxxer.prismarine.summaries.PrismarineProjectSummary;
import com.energyxxer.prismarine.summaries.PrismarineSummaryModule;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import com.energyxxer.prismarine.worker.tasks.SetupProductionsTask;
import com.energyxxer.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.energyxxer.prismarine.Prismarine.DEFAULT_CHARSET;

public class ProjectReader {
    private final CompoundInput input;
    private final Function<Path, TokenSource> sourceFunction;

    private final HashMap<Path, Result> cache = new HashMap<>();
    private final HashMap<PrismarineLanguageUnitConfiguration, Lexer> lexers = new HashMap<>();

    public ProjectReader() {
        this.input = null;
        this.sourceFunction = null;
    }

    public ProjectReader(CompoundInput input, Function<Path, TokenSource> sourceFunction) {
        this.input = input;
        this.sourceFunction = sourceFunction;
    }

    public Query startQuery(Path relativePath, PrismarineProjectWorker worker) {
        return new Query(this, relativePath, worker);
    }

    public void refreshPatterns() {
        for(Result result : cache.values()) {
            result.pattern = null;
            result.summary = null;
            result.matchResponse = null;
            result.changedSinceCached = false;
        }
        lexers.clear();
    }

    public void startCache() {
        for(Result result : cache.values()) {
            result.changedSinceCached = false;
        }
    }

    public void populateWithCachedReader(ProjectReader cachedReader) {
        for(Map.Entry<Path, Result> entry : cachedReader.cache.entrySet()) {
            if(this.cache.containsKey(entry.getKey())) {
                if(this.cache.get(entry.getKey()).readTime == 0) {
                    this.cache.put(entry.getKey(), entry.getValue());
                }
            } else {
                this.cache.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public Function<Path, TokenSource> getSourceFunction() {
        return sourceFunction;
    }

    public static class Query {
        protected ProjectReader reader;
        protected final Path relativePath;

        protected PrismarineLanguageUnitConfiguration unitConfig;
        protected PrismarineProjectWorker worker;

        protected boolean needsBytes = false;
        protected boolean needsString = false;
        protected boolean needsJSON = false;
        protected boolean needsPattern = false;
        protected boolean needsSummary = false;

        protected boolean skipIfNotChanged = false;

        protected PrismarineProjectSummary parentSummary = null;
        protected boolean skipSummaryIfMatchFailed = false;

        protected Query(ProjectReader reader, Path relativePath, PrismarineProjectWorker worker) {
            this.reader = reader;
            this.relativePath = relativePath;
            this.worker = worker;
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

        public Query needsSummary(PrismarineLanguageUnitConfiguration unitConfig, PrismarineProjectSummary parentSummary, boolean skipIfMatchFailed) {
            this.needsPattern(unitConfig);
            this.needsSummary = true;
            this.parentSummary = parentSummary;
            this.skipSummaryIfMatchFailed = skipIfMatchFailed;
            return this;
        }

        public PrismarineProjectSummary getParentSummary() {
            return parentSummary;
        }

        public Query skipIfNotChanged() {
            this.skipIfNotChanged = true;
            return this;
        }

        public Result perform() throws IOException {
            return reader.performQuery(this);
        }
    }

    private static long getChangeTime(File file) {
        try {
            return Math.max(file.lastModified(), Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime().toMillis());
        } catch (IOException e) {
            e.printStackTrace();
            return file.lastModified();
        }
    }

    private static long getFileSize(File file) {
        try {
            return Files.readAttributes(file.toPath(), BasicFileAttributes.class).size();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private Result performQuery(Query query) throws IOException {
        Result existing = cache.get(query.relativePath);

        String relativePathStr = query.relativePath.toString().replace(File.separatorChar, '/');

        byte[] bytes = null;

        Path root = input.getRootFile().toPath();
        File leafFile = root.resolve(relativePathStr).toFile();
        if(existing != null
                && (leafFile.exists() ? getChangeTime(leafFile) : getChangeTime(root.toFile())) <= existing.readTime
                && (leafFile.exists() ? getFileSize(leafFile) : getFileSize(root.toFile())) == existing.size
        ) {
            if(query.skipIfNotChanged) return null;
            else existing.skippableIfNotChanged = false;
            if(existing.matchesRequirements(query)) return existing;
            bytes = existing.bytes;
        }

        if(bytes == null) {
            try (InputStream is = input.get(relativePathStr)) {
                if(is == null) throw new FileNotFoundException(query.relativePath.toString());
                bytes = FileUtil.readAllBytes(is);
            }
        }

        int hashCode = Arrays.hashCode(bytes);

        if(existing != null && existing.hashCode == hashCode) {
            //Return cached result if it contains what the caller needs and is up to date
            if(query.skipIfNotChanged) return null;
            else existing.skippableIfNotChanged = false;
            if(existing.matchesRequirements(query)) {
                existing.readTime = System.currentTimeMillis();
                existing.size = getFileSize(leafFile);
                return existing;
            }
        }

        //Create a new result
        Result result = new Result();
        result.relativePath = query.relativePath;
        result.readTime = System.currentTimeMillis();
        result.size = getFileSize(leafFile);
        result.hashCode = hashCode;
        result.bytes = bytes;
        result.skippableIfNotChanged = query.skipIfNotChanged;
        result.changedSinceCached = existing == null || existing.hashCode != hashCode;
        if(query.needsString) result.string = new String(bytes, DEFAULT_CHARSET);
        if(query.needsJSON) {
            result.jsonObject = new Gson().fromJson(result.string, JsonObject.class);
        }

        TokenSource source = sourceFunction.apply(query.relativePath);

        result = populateParseResult(query, source, result);

        cache.put(query.relativePath, result);

        return result;
    }

    public Result populateParseResult(Query query, TokenSource source, Result result) {
        result.relativePath = query.relativePath;

        if(query.needsPattern) {
            Lexer lexer = getLexerForUnitConfig(query.unitConfig, query.worker);

            PrismarineSummaryModule summary = null;
            if(query.needsSummary) {
                summary = query.unitConfig.createSummaryModule(source, query.relativePath, query.parentSummary);
                summary.setFileLocation(query.relativePath);
                lexer.setSummaryModule(summary);
            }

            lexer.start(source, result.string, query.unitConfig.createLexerProfile());
            TokenMatchResponse response = ((LazyLexer) lexer).getMatchResponse();

            if(response.matched) {
                result.pattern = response.pattern;
            }
            result.matchResponse = response;

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

    private Lexer getLexerForUnitConfig(PrismarineLanguageUnitConfiguration unitConfig, PrismarineProjectWorker worker) {
        if(lexers.containsKey(unitConfig)) return lexers.get(unitConfig);

        Lexer newLexer = new LazyLexer(new TokenStream(), worker.output.get(SetupProductionsTask.INSTANCE).get(unitConfig).FILE);
        lexers.put(unitConfig, newLexer);

        return newLexer;
    }

    public void putResultHash(Path relativePath, int hashCode) {
        if(!cache.containsKey(relativePath)) {
            Result result = new Result();
            result.relativePath = relativePath;
            result.hashCode = hashCode;
            cache.put(relativePath, result);
        }
    }

    public Collection<Result> getResults() {
        return cache.values();
    }

    public static class Result {
        protected Path relativePath;
        protected int hashCode;
        protected byte[] bytes;
        protected String string;
        protected JsonObject jsonObject;
        protected TokenPattern<?> pattern;
        protected PrismarineSummaryModule summary;

        protected boolean skippableIfNotChanged = true;
        protected boolean changedSinceCached = true;
        public long readTime;
        public long size;

        public TokenMatchResponse matchResponse;

        protected boolean matchesRequirements(Query query) {
            return  !(query.needsBytes && bytes == null) &&
                    !(query.needsString && string == null) &&
                    !(query.needsJSON && jsonObject == null) &&
                    !(query.needsPattern && pattern == null) &&
                    !(query.needsSummary  && summary == null)
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

        public boolean isSkippableIfNotChanged() {
            return skippableIfNotChanged;
        }

        public boolean wasChangedSinceCached() {
            return changedSinceCached;
        }

        public TokenMatchResponse getMatchResponse() {
            return matchResponse;
        }
    }
}
