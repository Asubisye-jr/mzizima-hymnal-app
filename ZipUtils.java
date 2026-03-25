package com.example.mzizimahymnal;

import java.io.*;
import java.util.List;
import java.util.zip.*;

public class ZipUtils {

    public static void zipSongs(File zipFile, File jsonFile, List<File> audioFiles) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));

        addFileToZip(jsonFile, "songs.json", zos);

        for (File audio : audioFiles) {
            addFileToZip(audio, "audio/" + audio.getName(), zos);
        }

        zos.close();
    }

    private static void addFileToZip(File file, String entryName, ZipOutputStream zos) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        zos.putNextEntry(new ZipEntry(entryName));

        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }
}