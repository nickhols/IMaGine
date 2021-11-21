package com.nicholasdingler.InputStreamWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileInputStreamWrapper extends InputStreamWrapper {
    private FileInputStream fin;
    private File fileObject;
    private long fileSize;
    private FileChannel fileChannel;

    public FileInputStreamWrapper(String filename) throws Exception {
        fin = new FileInputStream(filename);
        fileObject = new File(filename);
        fileSize = fileObject.length();
        fileChannel = fin.getChannel();
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

    public byte[] readAll() throws Exception {
        byte[] entireFile = new byte[(int)fileSize];
        read(entireFile,0, (int) fileSize);
        index = fileSize;
        return entireFile;
    }

    public void close() throws Exception {
        fin.close();
    }

    public byte read() throws Exception{
        byte output = (byte) fin.read();
        index++;
        return output;
    }

    public byte readFromIndex(int index) throws Exception {
        fileChannel.position(index);
        byte output = read();
        fileChannel.position(this.index);
        return output;
    }

    public void readFromIndex(int index, byte[] buffer, int off, int len) throws Exception{
        fileChannel.position(index);
        read(buffer, off, len);
        fileChannel.position(this.index);
    }

    public boolean addressable(int index) {
        return index <= fileSize - 1;
    }
}
