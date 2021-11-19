package com.nicholasdingler.InputBitstream;

import com.nicholasdingler.InputStreamWrapper.InputStreamWrapper;

public abstract class InputBitstream {
    InputStreamWrapper stream;
    long bitCache;
    int nBitsCache;

    public InputBitstream(){
        stream = null;
        bitCache = 0;
        nBitsCache = 0;
    }

    public InputBitstream(InputStreamWrapper stream){
        this.stream = stream;
        bitCache = 0;
        nBitsCache = 0;
    }

    public void setStream(InputStreamWrapper stream) {
        this.stream = stream;
    }

    public boolean EOF(){
        return (stream.EOF() && nBitsCache == 0);
    }

    public abstract long getBits(int nBits, boolean invertOutput) throws Exception;

    public long getNextBit() throws Exception {
        return getBits(1, false);
    }

    public void skipToNextByte(){
        bitCache = 0;
        nBitsCache = 0;
    }
}
