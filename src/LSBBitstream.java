import java.io.File;
import java.io.FileInputStream;

public class LSBBitstream {
    byte[] inputBuffer;
    int nBytesRead;
    long bitCache;
    int nBitsCache;
    int length;

    LSBBitstream(byte[] inputBuffer){
        this.inputBuffer = inputBuffer;
        bitCache = 0;
        nBitsCache = 0;
        nBytesRead = 0;
        length = inputBuffer.length;
    }

    LSBBitstream(byte[] inputBuffer, int offset){
        this.inputBuffer = inputBuffer;
        bitCache = 0;
        nBitsCache = 0;
        nBytesRead = offset;
        //this.length = length;
        length = inputBuffer.length;
    }

    public boolean EOF(){
        if(nBytesRead >= length){
            return true;
        }
        return false;
    }

    public long getBits(int nBits, boolean outputByteLSBasMSB){
        long output = 0;
        try {
            //Make sure call asks for <= 56 bits, as the cached data is stored as a primitive long
            //Because a new byte may need to be read, limit to 64-8 = 56
            if (nBits > 56) {
                throw new Exception("Function not Supported for nBits > 56.");
            }
            //Read New bytes until number of cached bits is larger than the called number
            while (nBitsCache < nBits && !EOF()) {
                byte tempByte = inputBuffer[nBytesRead++];
                //bitCache |= (tempByte << nBitsCache);
                bitCache |= ( ((long)tempByte & 0xFF) << nBitsCache );

                nBitsCache += 8;
            }
            //Return bits, shift next bits in cache down to lsb, decrement nBitsCache
            long mask = ((long) 1 << nBits) - 1;
            long tempOutput = bitCache & mask;
            bitCache = bitCache >> nBits;
            nBitsCache -= nBits;
            mask = ((long) 1 << nBitsCache) - 1;
            bitCache &= mask;
            if (outputByteLSBasMSB) {
                for (int i = 0; i < nBits; i++) {
                    output = output << 1;
                    output |= tempOutput & 1;
                    tempOutput = tempOutput >> 1;
                }
            }
            else{
                output = tempOutput;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return output;
    }

    public long getNextBit(){
        long output = 0;
        try {
            long nBits = 1;
            //Read New bytes until number of cached bits is larger than the called number
            while (nBitsCache < nBits && !EOF()){
                byte tempByte = inputBuffer[nBytesRead++];
                bitCache |= ( ((long)tempByte & 0xFF) << nBitsCache);
                nBitsCache += 8;
            }
            //Return bits, shift next bits in cache down to lsb, decrement nBitsCache
            long mask = ((long)1 << nBits) - 1;
            long tempOutput = bitCache & mask;
            bitCache = bitCache >> nBits;
            nBitsCache -= nBits;
            mask = ((long)1 << nBitsCache) - 1;
            bitCache &= mask;
            output = tempOutput;
        } catch (Exception e){
            e.printStackTrace();
        }
        return output;
    }

    public void skipToNextByte(){
        nBitsCache = 0;
        bitCache = 0;
    }
}
