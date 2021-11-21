package com.nicholasdingler.OutputStreamWrapper;

import com.nicholasdingler.InputStreamWrapper.InputStreamWrapper;

public abstract class OutputStreamWrapper {
    public abstract void write(byte[] buffer, int off, int len) throws Exception;
    public abstract void write(int value) throws Exception;
    public void close() throws Exception{

    }
    public abstract InputStreamWrapper getInputStreamWrapper() throws Exception;
}
