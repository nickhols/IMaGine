package com.nicholasdingler.InputStreamWrapper;

public abstract class InputStreamWrapper {
    protected int index;
    protected int maxIndex;
    public long getIndex(){
        return index;
    }
    public void setIndex(int index) throws Exception{
        this.index = index;
    }
    public abstract int peek(int index, byte[] buffer, int off, int len) throws Exception;
    public abstract byte[] readAll() throws Exception;
    public byte peek(int index) throws Exception{
        byte[] output = new byte[1];
        peek(index, output, 0, 1);
        return output[0];
    }
    public int peek(byte[] buffer, int off, int len) throws Exception{
        return peek(index, buffer, off, len);
    }
    public byte peek() throws Exception{
        return peek(index);
    }
    public int read(byte[] buffer, int off, int len) throws Exception {
        int bytesRead = peek(buffer, off, len);
        setIndex(index + bytesRead);
        return bytesRead;
    }
    public byte read() throws Exception{
        byte output = peek();
        if(addressable(index)){
            setIndex(index + 1);
        }
        return output;
    }
    public boolean addressable(int index){
        return (index <= maxIndex) && (index >= 0);
    }
    public boolean EOF(){
        return index > maxIndex;
    }
    public int readInt(int nBytes, boolean littleEndian) throws Exception{
        int output = peekIntFrom(index, nBytes, littleEndian);
        setIndex(index + nBytes);
        return output;
    }
    public int peekIntFrom(int index, int nBytes, boolean littleEndian) throws Exception{
        if (nBytes > 4) {
            throw new Exception("Function only defined for Integers, up to 4 bytes.");
        }
        byte[] tempBuffer = new byte[nBytes];
        peek(index, tempBuffer, 0, nBytes);
        if(!littleEndian) {
            int output = 0;
            for (int i = 0; i < nBytes; i++) {
                output += (int) ((tempBuffer[i]) & 0xFF) << ((nBytes - 1 - i) * 8);
            }
            return output;
        }
        //Little Endian
        int output = 0;
        for (int i = 0; i < nBytes; i++){
            output += ((int)tempBuffer[i]) << (i * 8);
        }
        return output;
    }
    public int peekInt(int nBytes, boolean littleEndian) throws Exception {
        return peekIntFrom(index, nBytes, littleEndian);
    }
    public int available(){
        return maxIndex + 1 - index;
    }
}
