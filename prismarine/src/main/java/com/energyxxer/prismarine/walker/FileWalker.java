package com.energyxxer.prismarine.walker;

import com.energyxxer.commodore.util.io.CompoundInput;
import com.energyxxer.commodore.util.io.DirectoryCompoundInput;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.report.Report;
import com.energyxxer.prismarine.in.ProjectReader;
import com.energyxxer.prismarine.util.PathMatcher;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class FileWalker<T> {
    private final CompoundInput input;
    private final ProjectReader reader;

    @Nullable
    private final PrismarineProjectWorker worker;
    private final T subject;

    private final ArrayList<FileWalkerStop<T>> stops = new ArrayList<>();

    private final Report report;

    public FileWalker(CompoundInput input, Function<Path, TokenSource> sourceFunction, @Nullable PrismarineProjectWorker worker, T subject) {
        this.input = input;
        this.worker = worker;
        this.subject = subject;

        this.reader = new ProjectReader(input, sourceFunction);
        this.report = new Report();
    }

    public void walk() throws IOException {
        try {
            input.open();
            walk(null);
        } finally {
            input.close();
        }
    }

    private void walk(Path path) {
        String pathStr = path == null ? "" : path.toString().replace(File.separatorChar, '/');

        Iterable<String> innerEntries = input.listSubEntries(pathStr);

        if(innerEntries != null) {
            for(String fileName : innerEntries) {

                Path relativePath = path == null ? Paths.get(fileName) : path.resolve(fileName);
                String matcherInput = relativePath.toString().replace(File.separatorChar, '/');

                PathMatcher.Result[] results = new PathMatcher.Result[stops.size()];

                boolean anyMatchHitEnd = false;
                int i = 0;
                for(FileWalkerStop<T> stop : stops) {
                    PathMatcher.Result result = stop.pathMatcher.getMatchResult(matcherInput);
                    if(result.hitEnd) anyMatchHitEnd = true;
                    results[i] = result;
                    i++;
                }

                // do not walk through this directory if none of the
                // matches hit the end; more input will not make any stop match
                if(anyMatchHitEnd && input.isDirectory(matcherInput)) {
                    walk(relativePath);
                }

                for(i = 0; i < results.length; i++) {
                    PathMatcher.Result result = results[i];
                    FileWalkerStop<T> stop = stops.get(i);

                    if(result.matched) {
                        try {
                            File file;
                            if(input instanceof DirectoryCompoundInput) {
                                file = input.getRootFile().toPath().resolve(relativePath).toFile();
                            } else {
                                file = input.getRootFile();
                            }

                            if(!file.isFile()) continue;

                            boolean consumed = stop.accept(file, relativePath, result, worker, this);
                            if(consumed) break;
                        } catch(IOException x) {
                            x.printStackTrace();
                            //TODO logException x
                        }
                    }
                }
            }
        }
    }

    public ProjectReader getReader() {
        return reader;
    }

    public T getSubject() {
        return subject;
    }

    public void addStops(FileWalkerStop<T>... stops) {
        Collections.addAll(this.stops, stops);
    }

    public void addStops(Collection<FileWalkerStop<T>> stops) {
        this.stops.addAll(stops);
    }

    public Report getReport() {
        return report;
    }
}
