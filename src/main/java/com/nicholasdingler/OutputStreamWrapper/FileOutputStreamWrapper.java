package com.nicholasdingler.OutputStreamWrapper;

import java.io.File;
import java.io.FileOutputStream;

public class FileOutputStreamWrapper extends OutputStreamWrapper {
    private FileOutputStream fout;
    private File fileObject;
    private long fileSize;

    FileOutputStreamWrapper(String filename) throws Exception {
        fout = new FileOutputStream(filename);
        fileObject = new File(filename);
        fileSize = fileObject.length();
    }

    public void write(byte[] buffer, int off, int len) throws Exception {
        fout.write(buffer, off, len);
    }

    public void close() throws Exception {
        fout.close();
    }
}
