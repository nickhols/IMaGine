package com.nicholasdingler.InputStreamWrapper;

public class BufferInputStreamWrapper extends InputStreamWrapper{
    private byte[] buffer;

    public BufferInputStreamWrapper(byte[] buffer){
        this.buffer = buffer;
        maxIndex = buffer.length - 1;
    }

    public BufferInputStreamWrapper(byte[] buffer, int index){
        this.buffer = buffer;
        this.index = index;
        maxIndex = buffer.length - 1;
    }

    public int peek(int index, byte[] buffer, int off, int len) throws Exception {
        len = Math.min(len, maxIndex + 1 - index);
        System.arraycopy(this.buffer, index, buffer, off, len);
        return len;
    }

    public byte[] readAll() throws Exception {
        return buffer;
    }
}
