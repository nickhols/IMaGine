package com.nicholasdingler.OutputStreamWrapper;

import com.nicholasdingler.InputStreamWrapper.FileInputStreamWrapper;
import com.nicholasdingler.InputStreamWrapper.InputStreamWrapper;

import java.io.File;
import java.io.FileOutputStream;

public class FileOutputStreamWrapper extends OutputStreamWrapper {
    private FileOutputStream fout;
    private File fileObject;
    private long fileSize;

    public FileOutputStreamWrapper(String filename) throws Exception {
        fout = new FileOutputStream(filename);
        fileObject = new File(filename);
        fileSize = fileObject.length();
    }

    public void openFile(String filename) throws Exception{
        fout = new FileOutputStream(filename);
        fileObject = new File(filename);
        fileSize = fileObject.length();
    }

    public void write(byte[] buffer, int off, int len) throws Exception {
        fout.write(buffer, off, len);
    }

    public void write(int value) throws Exception{
        fout.write(value);
    }

    public void close() throws Exception {
        fout.close();
    }

    public InputStreamWrapper getInputStreamWrapper() throws Exception {
        return new FileInputStreamWrapper(fileObject.getAbsolutePath());
    }
}
