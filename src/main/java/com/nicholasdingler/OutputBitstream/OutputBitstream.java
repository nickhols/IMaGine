package com.nicholasdingler.OutputBitstream;

import com.nicholasdingler.OutputStreamWrapper.OutputStreamWrapper;

public abstract class OutputBitstream {
    OutputStreamWrapper stream;
    protected long bitCache;
    protected int nBitsCache;

    public void setStream(OutputStreamWrapper stream){
        this.stream = stream;
    }

    public OutputStreamWrapper getStream(){
        return stream;
    }

    public abstract void write(long value, int nBits, boolean invertEndian) throws Exception;

    public void writeBit(long bit) throws Exception{
        write(bit, 1, false);
    }

    public void skipToNextByte() throws Exception{
        if(nBitsCache > 0){
            stream.write((int)bitCache & 0xFF);
        }
        bitCache = 0;
        nBitsCache = 0;
    }

    public void close() throws Exception{
        skipToNextByte();
        stream.close();
    }
}
