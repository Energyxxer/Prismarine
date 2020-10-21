package com.energyxxer.nbtmapper.packs;

import com.energyxxer.commodore.util.io.CompoundInput;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Function;

public class NBTTypeMapPack {
    private ArrayList<RawNBTTypeMap> rawFiles = new ArrayList<>();

    private NBTTypeMapPack() {}

    public static NBTTypeMapPack fromCompound(CompoundInput input, Function<Path, TokenSource> sourceFunction) throws IOException {
        NBTTypeMapPack pack = new NBTTypeMapPack();
        try {
            input.open();
            InputStream fileListIS = input.get("");
            if (fileListIS != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(fileListIS))) {
                    String innerFileName;
                    ArrayList<RawNBTTypeMap> typeMapsInside = new ArrayList<>();
                    while ((innerFileName = br.readLine()) != null) {
                        InputStream is = input.get(innerFileName);
                        if (is != null) {
                            typeMapsInside.add(new RawNBTTypeMap(sourceFunction.apply(Paths.get(innerFileName)), readAllText(is)));
                        }
                    }

                    pack.rawFiles.addAll(typeMapsInside);
                }
            }
        } finally {
            input.close();
        }
        return pack;
    }

    private static String readAllText(InputStream is) throws IOException {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            if(sb.length() > 0) sb.setLength(sb.length()-1);
            return sb.toString();
        }
    }

    public Iterable<RawNBTTypeMap> getAllFileContents() {
        return rawFiles;
    }
}
