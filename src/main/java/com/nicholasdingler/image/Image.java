package com.nicholasdingler.image;

import java.io.FileOutputStream;

public abstract class Image {
    public int width;
    public int height;
    public int xOffset;
    public int yOffset;
    public int bitdepth;
    public int[][][] pixels;
    FileOutputStream fout;
    String filename;

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public int getxOffset() {
        return xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public int getBitdepth() {
        return bitdepth;
    }

    public String getFilename() { return filename; }

    public void setWidth(int width){
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public void setBitdepth(int bitdepth) {
        this.bitdepth = bitdepth;
    }

    public void setFilename(String filename) { this.filename = filename; }

    public boolean bufferEquals(byte[] buffer1, byte[] buffer2, int length){
        return bufferEqualsOffset(buffer1, buffer2, 0, 0, length);
    }

    public boolean bufferEqualsOffset(byte[] buffer1, byte[] buffer2, int buffer1Offset, int buffer2Offset, int length){
        for(int i = 0; i < length; i++){
            if(buffer1[i + buffer1Offset] != buffer2[i + buffer2Offset]){
                return false;
            }
        }
        return true;
    }

    public int bytesToIntegerLittleEndian(byte[] buffer, int bytes){
        if (bytes > 4) {
            System.out.println("Only Integers up to 4 bytes are supported by this function");
            return 0;
        }
        int output = 0;
        for (int i = 0; i < bytes; i++){
            output += ((int)buffer[i]) << (i * 8);
        }
        return output;
    }

    public int bytesToIntegerBigEndian(byte[] buffer, int nBytes, int offset){
        if (nBytes > 4) {
            System.out.println("Only Integers up to 4 bytes are supported by this function");
            return 0;
        }
        int output = 0;
        for (int i = 0; i < nBytes; i++){
            output += (int)((buffer[i + offset]) & 0xFF) << ((nBytes - 1 - i) * 8);
        }
        return output;
    }

/*    public byte[] integerToBytesBigEndian(int input, int nBytes){
        byte[] output = new byte[nBytes];
        for(int i = 0; i < nBytes; i++){

        }
    }*/
    public byte[] integerToBytesLittleEndian(int input, int nBytes){
        byte[] output = new byte[nBytes];
        for(int i = 0; i < nBytes; i++){
            output[i] = (byte) ((byte)(input >> i * 8) & 0xFF);
        }
        return output;
    }

    public void read() throws Exception{
        throw new Exception("Reading this file format is not yet supported.");
    }

    public void read(String filename) throws Exception {
        setFilename(filename);
        read();
    }

    public void write() throws Exception{
        throw new Exception("Writing this file format is not yet supported.");
    }

    public void write(String filename) throws Exception {
        setFilename(filename);
        write();
    }


}
