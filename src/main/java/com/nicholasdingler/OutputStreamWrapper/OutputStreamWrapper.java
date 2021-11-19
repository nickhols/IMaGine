package com.nicholasdingler.OutputStreamWrapper;

public abstract class OutputStreamWrapper {
    public abstract void write(byte[] buffer, int off, int len) throws Exception;
    public OutputStreamWrapper(){
    }
}
