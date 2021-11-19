package com.nicholasdingler.InputStreamWrapper;

public class BufferInputStreamWrapper extends InputStreamWrapper{
    private byte[] buffer;

    public BufferInputStreamWrapper(byte[] buffer){
        this.buffer = buffer;
    }

    public BufferInputStreamWrapper(byte[] buffer, int index){
        this.buffer = buffer;
        this.index = index;
    }

    public int read(byte[] buffer, int off, int len) throws Exception {
        if((long)len > buffer.length - index){
            len = (int)(buffer.length - index);
        }
        System.arraycopy(this.buffer, (int)index, buffer, off, len);
        return len;
    }

    public byte read() throws Exception {
        if(EOF()){
            throw new Exception("Attempted to read past buffer end.");
        }
        return buffer[(int)index++];
    }

    public boolean EOF(){
        return index >= buffer.length;
    }
}
