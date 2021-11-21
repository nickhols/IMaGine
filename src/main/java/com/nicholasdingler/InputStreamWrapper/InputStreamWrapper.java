package com.nicholasdingler.InputStreamWrapper;

import java.io.IOException;

public abstract class InputStreamWrapper {
    protected long index;
    public void setIndex(long index){
        this.index = index;
    };
    public abstract int read(byte[] buffer, int off, int len) throws Exception;
    public abstract byte[] readAll() throws Exception;
    public abstract byte read() throws Exception;
    public abstract byte readFromIndex(int index) throws Exception;
    public abstract void readFromIndex(int index, byte[] buffer, int off, int len) throws Exception;
    public abstract boolean addressable(int index);
    public abstract boolean EOF();
}
