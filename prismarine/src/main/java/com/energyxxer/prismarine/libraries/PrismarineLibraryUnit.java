package com.energyxxer.prismarine.libraries;

import com.energyxxer.prismarine.PrismarineLanguageUnitConfiguration;
import com.energyxxer.prismarine.in.ProjectReader;

import java.nio.file.Path;

public class PrismarineLibraryUnit {

    public enum Availability {
        COMPILER_ONLY(true, false), SUMMARY_ONLY(false, true), BOTH(true, true);
        public final boolean compiler;
        public final boolean summary;

        Availability(boolean compiler, boolean summary) {
            this.compiler = compiler;
            this.summary = summary;
        }
    }

    private Path relativePath;
    private PrismarineLanguageUnitConfiguration unitConfig;
    private String content;
    private Availability availability;

    private ProjectReader.Result parseResult;


    public PrismarineLibraryUnit(Path relativePath, PrismarineLanguageUnitConfiguration unitConfig, String content, Availability availability) {
        this.relativePath = relativePath;
        this.unitConfig = unitConfig;
        this.content = content;
        this.availability = availability;
    }

    public Path getRelativePath() {
        return relativePath;
    }

    public PrismarineLanguageUnitConfiguration getUnitConfig() {
        return unitConfig;
    }

    public String getContent() {
        return content;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setParseResult(ProjectReader.Result parseResult) {
        this.parseResult = parseResult;
    }

    public ProjectReader.Result getParseResult() {
        return parseResult;
    }
}
