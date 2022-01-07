package com.nicholasdingler.image;

import com.nicholasdingler.OutputStreamWrapper.*;
import com.nicholasdingler.InputStreamWrapper.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public abstract class Image {
    int width;
    int height;
    int xOffset;
    int yOffset;
    int bitdepth;
    int[][][] pixels;
    ByteBuffer bitmapStream;
    OutputStreamWrapper fout;
    InputStreamWrapper fin;
    BitmapFormat bitmapFormat;

    public enum BitmapFormat{
        gs,
        bgr,
        rgb,
        gsa,
        bgra,
        rgba
    }

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

    public int[][][] getPixels() { return pixels; }

    public OutputStreamWrapper getFout() { return fout; }

    public InputStreamWrapper getFin() { return fin; }

    void setWidth(int width){
        this.width = width;
    }

    void setHeight(int height) {
        this.height = height;
    }

    void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    void setBitdepth(int bitdepth) {
        this.bitdepth = bitdepth;
    }

    void setFin(InputStreamWrapper fin) { this.fin = fin; }

    void setFout(OutputStreamWrapper fout) { this.fout = fout; }

    boolean bufferEquals(byte[] buffer1, byte[] buffer2, int length){
        return bufferEqualsOffset(buffer1, buffer2, 0, 0, length);
    }

    boolean bufferEqualsOffset(byte[] buffer1, byte[] buffer2, int buffer1Offset, int buffer2Offset, int length){
        for(int i = 0; i < length; i++){
            if(buffer1[i + buffer1Offset] != buffer2[i + buffer2Offset]){
                return false;
            }
        }
        return true;
    }

    byte[] integerToBytesLittleEndian(int input, int nBytes){
        byte[] output = new byte[nBytes];
        for(int i = 0; i < nBytes; i++){
            output[i] = (byte) ((byte)(input >> i * 8) & 0xFF);
        }
        return output;
    }

    public void read(InputStreamWrapper fin) throws Exception{
        this.fin = fin;
        read();
    }

    public void read(String filename) throws Exception {
        fin = new FileInputStreamWrapper(filename);
        read();
    }

    public void read() throws Exception{
        throw new Exception("Reading this file format is not yet supported.");
    }

    public void write(OutputStreamWrapper fout) throws Exception{
        this.fout = fout;
        write();
    }

    public void write(String filename) throws Exception {
        fout = new FileOutputStreamWrapper(filename);
        write();
    }

    public void write() throws Exception{
        throw new Exception("Writing this file format is not yet supported.");
    }

}
