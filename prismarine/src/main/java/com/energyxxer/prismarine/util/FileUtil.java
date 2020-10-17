package com.energyxxer.prismarine.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

public class FileUtil {
    private FileUtil() {}

    public static boolean recursivelyDelete(File folder) {
        if(!folder.exists()) return true;
        if(!Files.isWritable(folder.toPath())) return false;
        if(!folder.isDirectory()) {
            return folder.delete();
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    recursivelyDelete(f);
                } else {
                    f.delete();
                }
            }
        }
        return folder.delete();
    }

    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;
    private static final int BUFFER_SIZE = 0x2000;

    public static byte[] readAllBytes(InputStream source) throws IOException {
        return read(source, BUFFER_SIZE);
    }

    public static byte[] read(InputStream source, int initialSize) throws IOException {
        int capacity = initialSize;
        byte[] buf = new byte[capacity];
        int nread = 0;
        int n;
        for (;;) {
            // read to EOF which may read more or less than initialSize (eg: file
            // is truncated while we are reading)
            while ((n = source.read(buf, nread, capacity - nread)) > 0)
                nread += n;

            // if last call to source.read() returned -1, we are done
            // otherwise, try to read one more byte; if that failed we're done too
            if (n < 0 || (n = source.read()) < 0)
                break;

            // one more byte was read; need to allocate a larger buffer
            if (capacity <= MAX_BUFFER_SIZE - capacity) {
                capacity = Math.max(capacity << 1, BUFFER_SIZE);
            } else {
                if (capacity == MAX_BUFFER_SIZE)
                    throw new OutOfMemoryError("Required array size too large");
                capacity = MAX_BUFFER_SIZE;
            }
            buf = Arrays.copyOf(buf, capacity);
            buf[nread++] = (byte)n;
        }
        return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
    }
}
