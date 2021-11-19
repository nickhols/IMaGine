package com.nicholasdingler.InputBitstream;

import com.nicholasdingler.InputStreamWrapper.InputStreamWrapper;

public class InputMSBBitstream extends InputBitstream{

    public InputMSBBitstream(InputStreamWrapper stream){
        this.stream = stream;
    }

    public long getBits(int nBits, boolean invertOutput) throws Exception {
        long output = 0;
        //Make sure call asks for <= 56 bits, as the cached data is stored as a primitive long
        //Because a new byte may need to be read, limit to 64-8 = 56
        if (nBits > 56) {
            throw new Exception("Function not Supported for nBits > 56.");
        }
        //Read New bytes until number of cached bits is larger than the called number
        while (nBitsCache < nBits && !EOF()) {
            byte tempByte = stream.read();
            //bitCache |= (tempByte << nBitsCache);
            bitCache = bitCache << 8;
            bitCache |= ((long)tempByte & 0xFF);
            nBitsCache += 8;
        }
        //Return bits, shift next bits in cache down to lsb, decrement nBitsCache
        long tempOutput = (bitCache >> (nBitsCache - nBits)) & ( (1 << nBits) - 1 );
        nBitsCache -= nBits;
        long mask = ((long) 1 << nBitsCache) - 1;
        bitCache &= mask;
        if (invertOutput) {
            for (int i = 0; i < nBits; i++) {
                output = output << 1;
                output |= tempOutput & 1;
                tempOutput = tempOutput >> 1;
            }
        }
        else{
            output = tempOutput;
        }
        return output;
    }
}
