package com.nicholasdingler.OutputBitstream;

import com.nicholasdingler.OutputStreamWrapper.OutputStreamWrapper;

public class OutputLSBBitstream extends OutputBitstream{
    public OutputLSBBitstream(OutputStreamWrapper stream){
        this.stream = stream;
    }

    public void write(long bits, int nBits, boolean invertEndian) throws Exception{
        long outputByte = 0;
        //Make sure call asks for <= 56 bits, as the cached data is stored as a primitive long
        //Because a new byte may need to be read, limit to 64-8 = 56
        if (nBits > 56) {
            throw new Exception("Function not Supported for nBits > 56.");
        }
        if(invertEndian){
            long tempBits = bits;
            bits = 0;
            for (int i = 0; i < nBits; i++) {
                bits = bits << 1;
                bits |= tempBits & 1;
                tempBits = tempBits >> 1;
            }
        }
        bits = bits & ((0x1 << nBits) - 1); //mask to correct number of bits, just in case
        bits = bits << nBitsCache;
        bitCache |= bits;
        nBitsCache += nBits;
        while(nBitsCache >= 8){
            stream.write((int)bitCache & 0xFF);
            bitCache = bitCache >> 8;
            nBitsCache -= 8;
            long mask = (1 << nBitsCache) - 1;
            bitCache = bitCache & mask;
        }
    }
}
