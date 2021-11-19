package com.nicholasdingler.InputStreamWrapper;

import java.io.File;
import java.io.FileInputStream;

public class FileInputStreamWrapper extends InputStreamWrapper {
    private FileInputStream fin;
    private File fileObject;
    private long fileSize;

    public FileInputStreamWrapper(String filename) throws Exception {
        fin = new FileInputStream(filename);
        fileObject = new File(filename);
        fileSize = fileObject.length();
        index = 0;
    }

    public boolean EOF(){
        return index >= fileSize;
    }

    public int read(byte[] buffer, int off, int len) throws Exception {
        //Puts data into supplied buffer, return value is number of bytes read in this operation
        if((long)len > fileSize - index){
            len = (int)(fileSize - index);
        }
        fin.read(buffer, off, len);
        index += len;
        return len;
    }

    public void close() throws Exception {
        fin.close();
    }

    public byte read() throws Exception{
        byte output = (byte) fin.read();
        index++;
        return output;
    }
}
