package com.nicholasdingler.InputStreamWrapper;

public abstract class InputStreamWrapper {
    protected long index;
    public abstract int read(byte[] buffer, int off, int len) throws Exception;
    public abstract byte read() throws Exception;
    public abstract boolean EOF();
    public InputStreamWrapper(){
        index = 0;
    }
}
