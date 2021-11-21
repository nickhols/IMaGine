package com.nicholasdingler.OutputStreamWrapper;

import com.nicholasdingler.InputStreamWrapper.BufferInputStreamWrapper;
import com.nicholasdingler.InputStreamWrapper.InputStreamWrapper;

public class BufferOutputStreamWrapper extends OutputStreamWrapper{
    byte[] stream;
    int index;
    public BufferOutputStreamWrapper(){
        stream = new byte[20];
        index = 0;
    }
    public BufferOutputStreamWrapper(byte[] stream){
        this.stream = stream;
        index = 0;
    }
    public void write(byte[] buffer, int off, int len){
        while(index + len > stream.length){
            byte[] temp = new byte[stream.length*2];
            System.arraycopy(stream, 0, temp, 0, stream.length);
            stream = temp;
        }
        System.arraycopy(buffer, off, stream, index, len);
        index += len;
    }
    public void write(int value){
        if(index >= stream.length){
            byte[] temp = new byte[stream.length*2];
            System.arraycopy(stream, 0, temp, 0, stream.length);
            stream = temp;
        }
        stream[index++] = (byte)value;
    }

    public void close(){
        byte[] temp = new byte[index];
        System.arraycopy(stream, 0, temp, 0, index);
        stream = temp;
    }

    public InputStreamWrapper getInputStreamWrapper() throws Exception {
        close();
        return new BufferInputStreamWrapper(stream);
    }
}
