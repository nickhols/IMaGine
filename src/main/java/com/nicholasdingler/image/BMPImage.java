package com.nicholasdingler.image;

import java.io.FileOutputStream;

public class BMPImage extends Image {
    byte[] scanlines;
    int stride;

    public BMPImage(){
        int size = 200;
        pixels = new int[size][size][4];
        height = size;
        width = size;
        for(int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                pixels[i][j][0] = (byte)(int)(((float)i / (float)height) * 255);
            }
        }
    }

    public BMPImage(Image sourceImage){
        width = sourceImage.width;
        height = sourceImage.height;
        xOffset = sourceImage.xOffset;
        yOffset = sourceImage.yOffset;
        bitdepth = sourceImage.bitdepth;
        pixels = sourceImage.pixels;
    }

    public void write(String outputFilePath) throws Exception {
        if(!outputFilePath.endsWith(".bmp")){
            System.out.println("Unrecognized file extension.");
            return;
        }
//            fout = new FileOutputStream(outputFilePath);
//            byte[] writeBuffer = integerToBytesLittleEndian(0x4D42, 2);
//            fout.write(writeBuffer);
//            //fout.write(integerToBytesLittleEndian(0x3A, 4));
//            int dataSize = height * (width * 3 + (4 - ((width * 3) % 4) % 4));
//            int fileSize = 14 + 40 + dataSize;
//            //
//            dataSize = height * width * 4;
//            fileSize = 14 + 108 + dataSize;
//            //
//            fout.write(integerToBytesLittleEndian(fileSize, 4));
//            fout.write(integerToBytesLittleEndian(0, 4));
//            fout.write(integerToBytesLittleEndian(14+40, 4));
//            fout.write(integerToBytesLittleEndian(40,4));
//            fout.write(integerToBytesLittleEndian(width,4));
//            fout.write(integerToBytesLittleEndian(height,4));
//            fout.write(integerToBytesLittleEndian(1,2));
//            //fout.write(integerToBytesLittleEndian(24,2));
//            //
//            fout.write(integerToBytesLittleEndian(32,2));
//            fout.write(integerToBytesLittleEndian(3,4));
//            //
//            //fout.write(integerToBytesLittleEndian(0,4));
//            fout.write(integerToBytesLittleEndian(0,4));
//            fout.write(integerToBytesLittleEndian(0x2e23,4));
//            fout.write(integerToBytesLittleEndian(0x2e23,4));
//            fout.write(integerToBytesLittleEndian(0,4));
//            fout.write(integerToBytesLittleEndian(0,4));
//            stride = width * 3 + (4 - (width * 3) % 4) % 4;
//            //
//            stride = width * 4 + (4 - (width * 4) % 4) % 4;
//            //
//            scanlines = new byte[height * stride];
        fout = new FileOutputStream(outputFilePath);
        byte[] writeBuffer = integerToBytesLittleEndian(0x4D42, 2);
        fout.write(writeBuffer);
        //fout.write(integerToBytesLittleEndian(0x3A, 4));
        int dataSize = height * (width * 3 + (4 - ((width * 3) % 4) % 4));
        int fileSize = 14 + 40 + dataSize;
        //
        dataSize = height * width * 4;
        fileSize = 14 + 108 + dataSize;
        //
        fout.write(integerToBytesLittleEndian(fileSize, 4));
        fout.write(integerToBytesLittleEndian(0, 4));
        fout.write(integerToBytesLittleEndian(14+108, 4));
        fout.write(integerToBytesLittleEndian(108,4));
        fout.write(integerToBytesLittleEndian(width,4));
        fout.write(integerToBytesLittleEndian(height,4));
        fout.write(integerToBytesLittleEndian(1,2));
        //fout.write(integerToBytesLittleEndian(24,2));
        //
        fout.write(integerToBytesLittleEndian(32,2));
        fout.write(integerToBytesLittleEndian(3,4));
        //
        //fout.write(integerToBytesLittleEndian(0,4));
        fout.write(integerToBytesLittleEndian(dataSize,4));
        fout.write(integerToBytesLittleEndian(0x2e23,4));
        fout.write(integerToBytesLittleEndian(0x2e23,4));
        fout.write(integerToBytesLittleEndian(0,4));
        fout.write(integerToBytesLittleEndian(0,4));
        fout.write(integerToBytesLittleEndian(0xFF0000, 4));
        fout.write(integerToBytesLittleEndian(0xFF00, 4));
        fout.write(integerToBytesLittleEndian(0xFF, 4));
        fout.write(integerToBytesLittleEndian(0xFF000000, 4));
        fout.write(integerToBytesLittleEndian(0x57696E20, 4));
        for(int i = 0; i < 36; i++){
            fout.write(integerToBytesLittleEndian(0,1));
        }
        fout.write(integerToBytesLittleEndian(0,4));
        fout.write(integerToBytesLittleEndian(0,4));
        fout.write(integerToBytesLittleEndian(0,4));

        stride = width * 3 + (4 - (width * 3) % 4) % 4;
        //
        stride = width * 4 + (4 - (width * 4) % 4) % 4;
        //
        scanlines = new byte[height * stride];
        int outputBufferIndex = 0;
        for(int i = 0; i < height; i++){
            int bytesWritten = 0;
            for(int j = 0; j < width; j++){
                scanlines[outputBufferIndex++] = (byte)(pixels[(height - i) - 1][j][2]);
                scanlines[outputBufferIndex++] = (byte)(pixels[(height - i) - 1][j][1]);
                scanlines[outputBufferIndex++] = (byte)(pixels[(height - i) - 1][j][0]);
                //
                scanlines[outputBufferIndex++] = (byte)(pixels[(height - i) - 1][j][3]);
                bytesWritten += 1;
                //
                bytesWritten += 3;
            }
            outputBufferIndex += (4 - (bytesWritten % 4)) % 4;
        }
        fout.write(scanlines);

        System.out.println("Done!");
    }
}
