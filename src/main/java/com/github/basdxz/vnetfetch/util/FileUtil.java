package com.github.basdxz.vnetfetch.util;

import lombok.*;
import lombok.experimental.*;
import org.apache.commons.io.input.BufferedFileChannelInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@UtilityClass
public final class FileUtil {
    public static void createDirectory(@NonNull File directory) throws IOException {
        if (directory.exists()) {
            if (!directory.isDirectory())
                throw new IOException(directory.getAbsolutePath() + " exists but is not a directory.");
            return;
        }
        if (!directory.mkdirs())
            throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
    }

    public static InputStream fileInputStream(@NonNull File inputFile) throws IOException {
        return new BufferedFileChannelInputStream(inputFile);
    }

    public static File appendToFileName(@NonNull File file, @NonNull String suffix) {
        return new File(file.getAbsolutePath() + suffix);
    }
}
