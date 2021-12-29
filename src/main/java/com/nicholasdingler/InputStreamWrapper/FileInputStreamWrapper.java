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
        fileChannel = fin.getChannel();
        maxIndex = (int)fileObject.length() - 1;
        index = 0;
    }

    @Override
    public void setIndex(int newIndex) throws Exception{
        fileChannel.position(newIndex);
        index = newIndex;
    }

    public int peek(int index, byte[] buffer, int off, int len) throws Exception {
        //Puts data into supplied buffer, return value is number of bytes read in this operation
        if(len > (maxIndex + 1) - index){
            len = ((maxIndex + 1) - index);
        }
        int markIndex = this.index;
        if((int)fileChannel.position() != index){
            setIndex(index);
        }
        fin.read(buffer, off, len);
        setIndex(markIndex);
        return len;
    }

    public byte[] readAll() throws Exception {
        byte[] entireFile = new byte[(int)fileSize];
        read(entireFile,0, (int) fileSize);
        index = maxIndex + 1;
        return entireFile;
    }

    public void close() throws Exception {
        fin.close();
    }
}
