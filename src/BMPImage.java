import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class BMPImage extends Image {
    byte[] scanlines;
    int stride;

    BMPImage(){
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

    BMPImage(Image sourceImage){
        width = sourceImage.width;
        height = sourceImage.height;
        xOffset = sourceImage.xOffset;
        yOffset = sourceImage.yOffset;
        bitdepth = sourceImage.bitdepth;
        pixels = sourceImage.pixels;
    }

    void write(String outputFilePath){
        try {
            if(!outputFilePath.endsWith(".bmp")){
                System.out.println("Unrecognized file extension.");
                return;
            }
            fout = new FileOutputStream(outputFilePath);
            byte[] writeBuffer = integerToBytesLittleEndian(0x4D42, 2);
            fout.write(writeBuffer);
            //fout.write(integerToBytesLittleEndian(0x3A, 4));
            int dataSize = height * (width * 3 + (4 - ((width * 3) % 4) % 4));
            int fileSize = 14 + 40 + dataSize;
            fout.write(integerToBytesLittleEndian(fileSize, 4));
            fout.write(integerToBytesLittleEndian(0, 4));
            fout.write(integerToBytesLittleEndian(14+40, 4));
            fout.write(integerToBytesLittleEndian(40,4));
            fout.write(integerToBytesLittleEndian(width,4));
            fout.write(integerToBytesLittleEndian(height,4));
            fout.write(integerToBytesLittleEndian(1,2));
            fout.write(integerToBytesLittleEndian(24,2));
            fout.write(integerToBytesLittleEndian(0,4));
            fout.write(integerToBytesLittleEndian(0,4));
            fout.write(integerToBytesLittleEndian(0x2e23,4));
            fout.write(integerToBytesLittleEndian(0x2e23,4));
            fout.write(integerToBytesLittleEndian(0,4));
            fout.write(integerToBytesLittleEndian(0,4));
            stride = width * 3 + (4 - (width * 3) % 4) % 4;
            scanlines = new byte[height * stride];
            int outputBufferIndex = 0;
            for(int i = 0; i < height; i++){
                int bytesWritten = 0;
                for(int j = 0; j < width; j++){
                    scanlines[outputBufferIndex++] = (byte)(pixels[(height - i) - 1][j][2]);
                    scanlines[outputBufferIndex++] = (byte)(pixels[(height - i) - 1][j][1]);
                    scanlines[outputBufferIndex++] = (byte)(pixels[(height - i) - 1][j][0]);
                    bytesWritten += 3;
                }
                outputBufferIndex += (4 - (bytesWritten % 4)) % 4;
            }
            fout.write(scanlines);


        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Done!");
    }
}