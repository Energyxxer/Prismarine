package com.energyxxer.prismarine.out;

import com.energyxxer.commodore.module.Exportable;
import com.energyxxer.commodore.module.ExportablePack;
import com.energyxxer.commodore.module.ModulePackGenerator;
import com.energyxxer.prismarine.PrismarineCompiler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.energyxxer.commodore.module.ModulePackGenerator.OutputType.FOLDER;
import static com.energyxxer.commodore.module.ModulePackGenerator.OutputType.ZIP;

public class PrismarineExportablePack implements ExportablePack {
    @NotNull
    public final ArrayList<Exportable> exportables;

    @Nullable
    protected PrismarineCompiler compiler;

    @NotNull
    protected final String rootPath;
    @NotNull
    protected final File rootFile;
    @NotNull
    protected final ModulePackGenerator.OutputType outputType;

    protected ZipOutputStream zipStream;

    protected float progressDelta = 1;

    public PrismarineExportablePack(@Nullable PrismarineCompiler compiler, @NotNull File outFile) {
        this(compiler, outFile, outFile.getName().endsWith(".zip") ? ZIP : FOLDER);
    }

    public PrismarineExportablePack(@Nullable PrismarineCompiler compiler, @NotNull File outFile, @NotNull ModulePackGenerator.OutputType outputType) {
        this.compiler = compiler;
        this.outputType = outputType;

        if(outputType == FOLDER && !outFile.exists()) {
            outFile.mkdirs();
        } else if(outputType == ZIP && !outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }

        this.rootPath = outFile.getAbsolutePath();
        this.rootFile = outFile;
        this.exportables = new ArrayList<>();
    }

    public ArrayList<IOException> generate(int numThreads) {
        if(outputType == ZIP) {
            try {
                zipStream = new ZipOutputStream(new FileOutputStream(rootFile));
            } catch (FileNotFoundException e) {
                ArrayList<IOException> exceptions = new ArrayList<>();
                exceptions.add(e);
                return exceptions;
            }
        }

        ArrayList<IOException> exceptions = null;
        try {
            exceptions = Exportable.exportAll(exportables, rootPath, zipStream, true, numThreads);
        } finally {
            if(zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                    if(exceptions == null) exceptions = new ArrayList<>();
                    exceptions.add(e);
                }
            }
        }
        return exceptions;
    }

    private void createFile(@Nullable String path, @Nullable byte[] contents) throws IOException {
        if(path == null || contents == null) return;
        if(compiler != null) {
            compiler.updateProgress(compiler.getProgress() + progressDelta);
            compiler.setProgress(getProgressMessage() + ": " + path);
        }
        if(outputType == ZIP) {
            ZipEntry e = new ZipEntry(path);
            zipStream.putNextEntry(e);

            byte[] data = contents;
            zipStream.write(data, 0, data.length);
            zipStream.closeEntry();
        } else {
            File file = new File(rootPath + File.separator + path.replace('/', File.separatorChar));
            file.getParentFile().mkdirs();
            file.createNewFile();

            try(FileOutputStream writer = new FileOutputStream(file)) {
                writer.write(contents);
                writer.flush();
            }
        }
    }

    @NotNull
    public ModulePackGenerator.OutputType getOutputType() {
        return outputType;
    }

    @Override
    public List<Exportable> getAllExportables() {
        return exportables;
    }

    public String getProgressMessage() {
        return "Generating pack";
    }

    public PrismarineCompiler getCompiler() {
        return compiler;
    }

    public void setCompiler(PrismarineCompiler compiler) {
        this.compiler = compiler;
    }
}
