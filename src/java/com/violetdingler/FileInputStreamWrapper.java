package com.violetdingler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileInputStreamWrapper {
    private FileInputStream fin;
    private File fileObject;
    long fileSize;
    long bytesRead;

    FileInputStreamWrapper(String filename) throws Exception {
        fin = new FileInputStream(filename);
        fileObject = new File(filename);
        fileSize = fileObject.length();
    }

    public boolean EOF(){
        if(fileSize > bytesRead){
            return false;
        }
        return true;
    }

    public int read(byte[] buffer, int off, int len) throws Exception {
        //Puts data into supplied buffer, return value is number of bytes read in this operation
        if((long)len > fileSize - bytesRead){
            len = (int)(fileSize - bytesRead);
        }
        fin.read(buffer, off, len);
        bytesRead += len;
        return len;
    }

    public void close() throws Exception {
        fin.close();
    }
}
