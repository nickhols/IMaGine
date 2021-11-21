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

    public byte[] readAll() throws Exception {
        return buffer;
    }

    public byte read() throws Exception {
        if(EOF()){
            throw new Exception("Attempted to read past buffer end.");
        }
        return buffer[(int)index++];
    }

    public byte readFromIndex(int index) {
        return buffer[index];
    }

    public void readFromIndex(int index, byte[] buffer, int off, int len) {
        System.arraycopy(this.buffer, index, buffer, off, len);
    }

    public boolean addressable(int index) {
        return index <= buffer.length - 1;
    }

    public boolean EOF(){
        return index >= buffer.length;
    }

    public byte[] tempGetBuffer(){
        return buffer;
    }
}
