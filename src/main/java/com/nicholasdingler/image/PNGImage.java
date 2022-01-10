package com.nicholasdingler.image;

import com.nicholasdingler.ArrayUtil;
import com.nicholasdingler.InputBitstream.InputMSBBitstream;
import com.nicholasdingler.InputStreamWrapper.*;
import com.nicholasdingler.OutputBitstream.OutputMSBBitstream;
import com.nicholasdingler.OutputStreamWrapper.BufferOutputStreamWrapper;
import com.nicholasdingler.OutputStreamWrapper.OutputStreamWrapper;
import com.nicholasdingler.ZLibStream;
import com.nicholasdingler.unrecognizedFormatException;

import java.lang.reflect.Array;
import java.nio.Buffer;

public class PNGImage extends Image {
    private int colorType;
    private int compression;
    private int interlacing;
    private int filter;
    private byte[] IDAT = {};
    private byte[] dataStream;
    private byte[] rawScanlines;
    private byte[][] palette;

    public PNGImage(){

    }

    public PNGImage(Image sourceImage){
        width = sourceImage.width;
        height = sourceImage.height;
        xOffset = sourceImage.xOffset;
        yOffset = sourceImage.yOffset;
        bitdepth = sourceImage.bitdepth;
        pixels = sourceImage.pixels;
        BPP = bitdepth * 4;
    }

    private int BPP;

    public int getColorType() {
        return colorType;
    }

    public int getCompression() {
        return compression;
    }

    public int getFilter() {
        return filter;
    }

    public int getInterlacing() {
        return interlacing;
    }

    public void setColorType(int colorType) {
        this.colorType = colorType;
    }

    public void setCompression(int compression) {
        this.compression = compression;
    }

    public void setFilter(int filter) {
        this.filter = filter;
    }

    public void setInterlacing(int interlacing) {
        this.interlacing = interlacing;
    }

    public void read(String filename)throws Exception{
        read(new FileInputStreamWrapper(filename));
    }

    public void read(FileInputStreamWrapper fin) throws Exception {
        readMetaData(fin);
        readData(fin);
        ZLibStream zl = new ZLibStream();
        zl.inflate(new BufferInputStreamWrapper(IDAT));
        dataStream = zl.getInflatedStream().readAll();
        parseDatastream();
        fin.close();
    }

    private void readMetaData(FileInputStreamWrapper fin) throws Exception {
        //Ensure that the
        int signatureBufferSize = 8;
        byte[] signatureBuffer = new byte[signatureBufferSize];
        fin.read(signatureBuffer, 0, 8);
        byte[] signature = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};
        if(!bufferEquals(signatureBuffer, signature, 8)){
            System.out.println("Invalid PNG Format.");
            throw new unrecognizedFormatException();
        }


        //Read IHDR Chunk
        byte[] IHDRChunk = readChunk(fin);
        //Get important data from the IHDR buffer, including dimensions, BPP, Color type, Compression method, Filter type, and Interlacing format
        setWidth(ArrayUtil.bytesToIntegerBigEndian(IHDRChunk, 4, 8));
        setHeight(ArrayUtil.bytesToIntegerBigEndian(IHDRChunk, 4, 12));
        setBitdepth(ArrayUtil.bytesToIntegerBigEndian(IHDRChunk, 1, 16));
        setColorType(ArrayUtil.bytesToIntegerBigEndian(IHDRChunk, 1, 17));
        setCompression(ArrayUtil.bytesToIntegerBigEndian(IHDRChunk, 1, 18));
        setFilter(ArrayUtil.bytesToIntegerBigEndian(IHDRChunk, 1, 19));
        setInterlacing(ArrayUtil.bytesToIntegerBigEndian(IHDRChunk, 1, 20));
        switch(colorType){
            case 0://Greyscale
                BPP = bitdepth;
                bitmapFormat = BitmapFormat.gs;
                break;
            case 2:
                BPP = bitdepth * 3;
                bitmapFormat = BitmapFormat.rgb;
                break;
            case 3:
                BPP = bitdepth;
                bitmapFormat = BitmapFormat.rgb;
                break;
            case 4:
                BPP = bitdepth * 2;
                bitmapFormat = BitmapFormat.gsa;
                break;
            case 6:
                BPP = bitdepth * 4;
                bitmapFormat = BitmapFormat.rgba;
                break;
            default:
                throw new unrecognizedFormatException();
        }
    }

    private void readData(FileInputStreamWrapper fin) throws Exception {
        byte[] IDATSignature = {'I', 'D', 'A', 'T'};
        byte[] IENDSignature = {'I', 'E', 'N', 'D'};
        byte[] PLTESignature = {'P', 'L', 'T', 'E'};

        while (!fin.EOF()){ //read chunks until end of file
            byte[] chunk = readChunk(fin);
            if(bufferEqualsOffset(chunk, IDATSignature, 4, 0, 4)){//Chunk is an IDAT Chunk, Critical/Public
                //Create new IDAT Array to replace old one
                //Because arrays can't be resized, and multiple IDAT Chunks are possible and should be concatenated
                byte[] IDATNew = new byte[IDAT.length + ArrayUtil.bytesToIntegerBigEndian(chunk, 4, 0)];
                System.arraycopy(IDAT, 0, IDATNew, 0, IDAT.length);
                System.arraycopy(chunk, 8, IDATNew, IDAT.length, ArrayUtil.bytesToIntegerBigEndian(chunk, 4, 0));
                IDAT = IDATNew;
            }
            else if(bufferEqualsOffset(chunk, PLTESignature, 4, 0, 4)){//Chunk is the IEND Chunk, Critical/Public
                //PLTE is only required for indexed color type
                if(colorType == 3){
                    parsePalette(chunk);
                }
            }
            else if(bufferEqualsOffset(chunk, IENDSignature, 4, 0, 4)){//Chunk is the IEND Chunk, Critical/Public
                //When IEND is found, the file is done being read according to the PNG Specification
                //EOF should trigger after this read, but an explicit break is given in addition
                break;
            }
            else{
                System.out.println("Unrecognized Chunk:" + (char)chunk[4] + (char)chunk[5] + (char)chunk[6] + (char)chunk[7]);
                //Do nothing, add support for ancillary public chunks later
                //carry over ancillary private chunks to output, but dont process?
            }
        }

        return;
    }

    private byte[] readChunk(FileInputStreamWrapper fin) throws Exception {
        byte[] lengthBuffer = new byte[4];
        fin.read(lengthBuffer, 0, 4);
        int chunkLength = ArrayUtil.bytesToIntegerBigEndian(lengthBuffer,4, 0);
        byte[] buffer = new byte[chunkLength + 12];
        System.arraycopy(lengthBuffer, 0, buffer, 0, 4);
        fin.read(buffer, 4, chunkLength + 8);
        byte[] crcData = new byte[chunkLength + 4];
        byte[] crcActual = new byte[4];
        byte[] crcExpected = new byte[4];
        System.arraycopy(buffer, 4, crcData, 0, chunkLength + 4);
        System.arraycopy(buffer, chunkLength + 8, crcExpected, 0, 4);
        crcActual = calculateCRC(crcData);
        for(int i = 0; i < 4; i++){
            if(crcActual[i] != crcExpected[i]) {
                System.out.print("Invalid Chunk CRC: " + buffer[4] + buffer[5] + buffer[6] + buffer[7]);
                break;
            }
        }
        return buffer;
    }

    private void parseDatastream() throws Exception {
        pixels = new int[height][width][4];
        //com.violetdingler.MSBBitstream bs = new com.violetdingler.MSBBitstream(filteredScanlines);
        if(interlacing == 0){
            parseSubImage(0, 0);
        }
        else{
            int dataStreamOffset = 0;
            for(int i = 1; i < 8; i++){
                dataStreamOffset += parseSubImage(i, dataStreamOffset);
            }
        }
    }

    private int parseSubImage(int pass, int dataStreamOffset) throws Exception {
        int nScanlines = 0;
        int firstScanline = 0;
        int scanlineDifferential = 0;
        int nPixelsInScanline = 0;
        int firstPixel = 0;
        int pixelDifferential = 0;
        switch(pass){
            case 1:
                nPixelsInScanline = (width + 7)/8;
                nScanlines = (height + 7) / 8;
                firstScanline = 0;
                scanlineDifferential = 8;
                pixelDifferential = 8;
                firstPixel = 0;
                break;
            case 2:
                nPixelsInScanline = (width + 3)/8;
                nScanlines = (height + 7) / 8;
                firstScanline = 0;
                scanlineDifferential = 8;
                pixelDifferential = 8;
                firstPixel = 4;
                break;
            case 3:
                nPixelsInScanline = (width + 3)/4;
                nScanlines = (height + 3) / 8;
                firstScanline = 4;
                scanlineDifferential = 8;
                pixelDifferential = 4;
                firstPixel = 0;
                break;
            case 4:
                nPixelsInScanline = (width + 1)/4;
                nScanlines = (height + 3) / 4;
                firstScanline = 0;
                scanlineDifferential = 4;
                pixelDifferential = 4;
                firstPixel = 2;
                break;
            case 5:
                nPixelsInScanline = (width + 1)/2;
                nScanlines = (height + 1) / 4;
                firstScanline = 2;
                scanlineDifferential = 4;
                pixelDifferential = 2;
                firstPixel = 0;
                break;
            case 6:
                nPixelsInScanline = (width)/2;
                nScanlines = (height + 1) / 2;
                firstScanline = 0;
                scanlineDifferential = 2;
                pixelDifferential = 2;
                firstPixel = 1;
                break;
            case 7:
                nPixelsInScanline = (width);
                nScanlines = (height) / 2;
                firstScanline = 1;
                scanlineDifferential = 2;
                pixelDifferential = 1;
                firstPixel = 0;
                break;
            case 0:
                nPixelsInScanline = (width);
                nScanlines = height;
                firstScanline = 0;
                scanlineDifferential = 1;
                pixelDifferential = 1;
                firstPixel = 0;
                break;
        }
        int stride = 1 + (nPixelsInScanline * BPP + (8 - (nPixelsInScanline * BPP) % 8) % 8) / 8;
        byte[] subImageScanlines = new byte[nScanlines * stride];
//        for(int i = 0; i < nScanlines; i++){
//            getNextScanline(stride, dataStreamOffset, subImageScanlines, stride * i);
//            dataStreamOffset += stride;
//        }
        getNextSubImage(stride, dataStreamOffset, subImageScanlines, nScanlines);
        //
        //Unfilter the scanlines of this subimage pass
        //
        subImageScanlines = unfilterScanlines(subImageScanlines, nScanlines, stride);
        //
        //Push subimage into image
        //
        pushSubImage(subImageScanlines, nPixelsInScanline, nScanlines, firstScanline, scanlineDifferential, pixelDifferential, firstPixel);
        return stride * nScanlines;
    }

    private void getNextSubImage(int nBytesInScanline, int inputOffset, byte[] output, int nScanlines){
        System.arraycopy(dataStream, inputOffset, output, 0, nBytesInScanline * nScanlines);
    }

    private void getNextScanline(int nBytesInScanline, int inputOffset, byte[] output, int outputOffset) {
        System.arraycopy(dataStream, inputOffset, output, outputOffset, nBytesInScanline);
    }

    private void pushSubImage(byte[] subImageScanlines, int nPixelsInScanline, int nScanlines, int firstScanline, int scanlineDifferential, int pixelDifferential, int firstPixel) throws Exception {
        //MSBBitstream subImageBitStream = new MSBBitstream(subImageScanlines);
        InputMSBBitstream subImageBitStream = new InputMSBBitstream(new BufferInputStreamWrapper(subImageScanlines));
        for(int i = 0; i < nScanlines; i++){
            for(int j = 0; j < nPixelsInScanline; j++){
                int value = 0;
                int xCoordinate = j * pixelDifferential + firstPixel;
                int yCoordinate = i * scanlineDifferential + firstScanline;
                switch(colorType){
                    case 0:
                        value = (int) subImageBitStream.getBits(bitdepth, false);
                        pixels[yCoordinate][xCoordinate][0] = value;
                        pixels[yCoordinate][xCoordinate][1] = value;
                        pixels[yCoordinate][xCoordinate][2] = value;
                        pixels[yCoordinate][xCoordinate][3] = 0xFFFF;
                        break;
                    case 2:
                        pixels[yCoordinate][xCoordinate][0] = (int)subImageBitStream.getBits(bitdepth, false);
                        pixels[yCoordinate][xCoordinate][1] = (int)subImageBitStream.getBits(bitdepth, false);
                        pixels[yCoordinate][xCoordinate][2] = (int)subImageBitStream.getBits(bitdepth, false);
                        pixels[yCoordinate][xCoordinate][3] = 0xFFFF;
                        break;
                    case 3:
                        value = (int) subImageBitStream.getBits(bitdepth, false);
                        pixels[yCoordinate][xCoordinate][0] = palette[value][0];
                        pixels[yCoordinate][xCoordinate][1] = palette[value][1];
                        pixels[yCoordinate][xCoordinate][2] = palette[value][2];
                        pixels[yCoordinate][xCoordinate][3] = 0xFFFF;
                        break;
                    case 4:
                        value = (int) subImageBitStream.getBits(bitdepth, false);
                        pixels[yCoordinate][xCoordinate][0] = value;
                        pixels[yCoordinate][xCoordinate][1] = value;
                        pixels[yCoordinate][xCoordinate][2] = value;
                        pixels[yCoordinate][xCoordinate][3] = (int) subImageBitStream.getBits(bitdepth, false);
                        break;
                    case 6:
                        pixels[yCoordinate][xCoordinate][0] = (int)subImageBitStream.getBits(bitdepth, false);
                        pixels[yCoordinate][xCoordinate][1] = (int)subImageBitStream.getBits(bitdepth, false);
                        pixels[yCoordinate][xCoordinate][2] = (int)subImageBitStream.getBits(bitdepth, false);
                        pixels[yCoordinate][xCoordinate][3] = (int)subImageBitStream.getBits(bitdepth, false);
                        break;
                }
            }
        }
    }

    private byte[] unfilterScanlines(byte[] filteredScanlines, int nScanlines, int filteredStride) throws Exception {
        int rawStride = filteredStride - 1;
        if(filteredScanlines.length == 0){
            return new byte[0];
        }
        byte[] rawScanlines = new byte[nScanlines * rawStride];
        for(int i = 0; i < nScanlines; i++) {
            int filter = filteredScanlines[filteredStride * i];
            for (int j = 0; j < rawStride; j++) {
                byte byteA = getByteA(i * rawStride + j, rawScanlines, rawStride);
                byte byteB = getByteB(i * rawStride + j, rawScanlines, rawStride);
                byte byteC = getByteC(i * rawStride + j, rawScanlines, rawStride);
                switch (filter) {
                    case 0://No Filter
                        rawScanlines[i * rawStride + j] = filteredScanlines[i * filteredStride + j + 1];
                        break;
                    case 1://Sub Filter
                        rawScanlines[i * rawStride + j] = (byte) (filteredScanlines[i * filteredStride + j + 1] + byteA);
                        break;
                    case 2://Up Filter
                        rawScanlines[i * rawStride + j] = (byte) (filteredScanlines[i * filteredStride + j + 1] + byteB);
                        break;
                    case 3://Average Filter
                        rawScanlines[i * rawStride + j] = (byte) (filteredScanlines[i * filteredStride + j + 1] + ((((int) byteA & 0xFF) + ((int) byteB & 0xFF)) >> 1) & 0xFF);
                        break;
                    case 4://Paeth Filter
                        rawScanlines[i * rawStride + j] = (byte) (filteredScanlines[i * filteredStride + j + 1] + computePaethPredictor(byteA, byteB, byteC));
                        break;
                }
            }
        }
        return rawScanlines;
    }

    private byte getByteA(int index, byte[] rawScanlines, int stride) throws Exception {
        int indexInScanline = index % stride;
        byte byteA = 0;
        if(BPP < 8 && indexInScanline != 0){
            byteA = rawScanlines[index - 1];
        }
        else if(BPP >= 8 && indexInScanline >= BPP / 8){
            byteA = rawScanlines[index - BPP/8];
        }
        return byteA;
    }

    private byte getByteB(int index, byte[] rawScanlines, int stride) throws Exception {
        byte byteB = 0;
        if(index >= stride){
            byteB = rawScanlines[index - stride];
        }
        return byteB;
    }

    private byte getByteC(int index, byte[] rawScanlines, int stride) throws Exception {
        int indexInScanline = index % stride;
        byte byteC = 0;
        if(BPP < 8 && indexInScanline != 0 && index >= stride){
            byteC = rawScanlines[index - 1 - stride];
        }
        else if(BPP >= 8 && indexInScanline >= BPP / 8 && index >= stride){
            byteC = rawScanlines[index - BPP/8 - stride];
        }
        return byteC;
    }

    private byte getByteA(int index, InputStreamWrapper currentScanline, InputStreamWrapper previousScanline) throws Exception {
        if(index - (BPP / 8) < 1 || index == 1){
            return 0;
        }
        byte byteA = 0;
        if(BPP < 8){
            byteA = currentScanline.peek(index - 1);
        }
        else{
            byteA = currentScanline.peek(index - BPP/8);
        }
        return byteA;
    }

    private byte getByteB(int index, InputStreamWrapper currentScanline, InputStreamWrapper previousScanline) throws Exception {
        //if(previousScanline.){
        //    return 0;
        //}
        byte byteB = previousScanline.peek(index);
        return byteB;
    }

    private byte getByteC(int index, InputStreamWrapper currentScanline, InputStreamWrapper previousScanline) throws Exception {
        if(index - (BPP / 8) < 1 || index == 1){
            return 0;
        }
        byte byteC = 0;
        if(BPP < 8){
            byteC = previousScanline.peek(index - 1);
        }
        else{
            byteC = previousScanline.peek(index - BPP/8);
        }
        return byteC;
    }

    private int computePaethPredictor(int a, int b, int c){

        if (a < 0)
            a+= 256;
        if (b < 0)
            b+= 256;
        if (c < 0)
            c+= 256;
        int p = a + b - c;
        int pa = Math.abs(p - a);
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);
        int pr = 0;
        if(pa <= pb && pa <= pc){
            pr = a;
        }
        else if(pb <= pc){
            pr = b;
        }
        else{
            pr = c;
        }
        return (byte) pr;
    }

    private void parsePalette(byte[] chunk) throws Exception {
        palette = new byte[(chunk.length - 12)/3][3];
        for(int i = 0; i < palette.length; i++){
            palette[i][0] = chunk[3*i + 0 + 8];
            palette[i][1] = chunk[3*i + 1 + 8];
            palette[i][2] = chunk[3*i + 2 + 8];
        }
    }

    public void write() throws Exception {
        //write PNG signature
        fout.write( new byte[] {(byte)137, 80, 78, 71, 13, 10, 26, 10}, 0, 8);
        //write IHDR Chunk
        byte[] chunkName = {73, 72, 68, 82};
        byte[] chunkData = new byte[13];
        System.arraycopy(ArrayUtil.integerToBytesBigEndian(width, 4), 0, chunkData, 0, 4);
        System.arraycopy(ArrayUtil.integerToBytesBigEndian(height, 4), 0, chunkData, 4, 4);
        chunkData[8] = (byte)bitdepth;
        chunkData[9] = 6;
        chunkData[10] = 0;
        chunkData[11] = 0;
        chunkData[12] = 0;
        writeChunk(chunkName, chunkData);
        //write IDAT Chunk
        chunkName[0] = 0x49;
        chunkName[1] = 0x44;
        chunkName[2] = 0x41;
        chunkName[3] = 0x54;
        BufferInputStreamWrapper encodedBitmap = encodeScanlines();
        chunkData = encodedBitmap.readAll();
        writeChunk(chunkName, chunkData);
        //write IEND Chunk
        chunkData = new byte[0];
        chunkName[0] = 0x49;
        chunkName[1] = 0x45;
        chunkName[2] = 0x4E;
        chunkName[3] = 0x44;
        writeChunk(chunkName, chunkData);
    }

    public void writeChunk(byte[] chunkName, byte[] chunkData) throws Exception{
        fout.write(ArrayUtil.integerToBytesBigEndian(chunkData.length, 4), 0, 4);
        fout.write(chunkName, 0, 4);
        fout.write(chunkData, 0, chunkData.length);
        //CRC calculation
        byte[] crcData = new byte[4 + chunkData.length];
        System.arraycopy(chunkName, 0, crcData, 0, 4);
        System.arraycopy(chunkData, 0, crcData, 4, chunkData.length);
        fout.write(calculateCRC(crcData), 0, 4);
    }

    private byte[] calculateCRC(byte[] input){ //See "Sample Cyclic Redundancy Code implementation" in the PNG specification
        long[] crcTable = new long[256];

        for (int n = 0; n < 256; n++) {
            long c = (long) n;
            for (int k = 0; k < 8; k++) {
                if ((c & 1) == 1)
                    c = 0xedb88320L ^ (c >> 1);
                else
                    c = c >> 1;
            }
            crcTable[n] = c;
        }

        long c = 0xffffffffL;
        for (int n = 0; n < input.length; n++) {
            c = crcTable[(int)( (c & 0xFF) ^ (input[n] & 0xFF) ) & 0xff] ^ (c >> 8);
        }

        c = c & 0xFFFFFFFFL;
        c = c ^ 0xFFFFFFFFL;
        return ArrayUtil.integerToBytesBigEndian((int)c, 4);

    }

    private BufferInputStreamWrapper encodeScanlines() throws Exception {
        if(height == 0){
            return new BufferInputStreamWrapper(new byte[0]);
        }
        int sampleMask = (1 << bitdepth) - 1;
        int stride = ((BPP * width - (BPP * width) % 8) + 8) / 8;
        if((BPP * width) % 8 != 0){
            stride++;
        }

        OutputMSBBitstream bs = new OutputMSBBitstream(new BufferOutputStreamWrapper());
        bs.write(0, 8, false);
        for(int j = 0; j < width; j++){
            bs.write(pixels[0][j][0], bitdepth, false);
            bs.write(pixels[0][j][1], bitdepth, false);
            bs.write(pixels[0][j][2], bitdepth, false);
            bs.write(pixels[0][j][3], bitdepth, false);
        }
        InputStreamWrapper previousScanline = bs.getStream().getInputStreamWrapper();
        for(int i = 1; i < height; i++){
            OutputMSBBitstream unfilteredScanlineBitstream = new OutputMSBBitstream(new BufferOutputStreamWrapper());
            unfilteredScanlineBitstream.write(0, 8, false);
            for(int j = 0; j < width; j++){
                unfilteredScanlineBitstream.write(pixels[i][j][0], bitdepth, false);
                unfilteredScanlineBitstream.write(pixels[i][j][1], bitdepth, false);
                unfilteredScanlineBitstream.write(pixels[i][j][2], bitdepth, false);
                unfilteredScanlineBitstream.write(pixels[i][j][3], bitdepth, false);
            }
            unfilteredScanlineBitstream.skipToNextByte();
            InputStreamWrapper unfilteredScanline = unfilteredScanlineBitstream.getStream().getInputStreamWrapper();
            int[] sums = new int[5];
            int scanlineIndex = 1;
            while(!unfilteredScanline.EOF()){
                byte byteA = getByteA(scanlineIndex, unfilteredScanline, previousScanline);
                byte byteB = getByteB(scanlineIndex, unfilteredScanline, previousScanline);
                byte byteC = getByteC(scanlineIndex, unfilteredScanline, previousScanline);

                sums[0] += Math.abs(unfilteredScanline.peek(scanlineIndex));
                sums[1] += Math.abs(unfilteredScanline.peek(scanlineIndex) - byteA);
                sums[2] += Math.abs(unfilteredScanline.peek(scanlineIndex) - byteB);
                sums[3] += Math.abs((byte) (unfilteredScanline.peek(scanlineIndex) + ((((int) byteA & 0xFF) + ((int) byteB & 0xFF)) >> 1) & 0xFF));
                sums[4] += Math.abs(unfilteredScanline.peek(scanlineIndex) - computePaethPredictor(byteA, byteB, byteC));
                unfilteredScanline.read();
                scanlineIndex++;
            }
            int filter = 0;
            //sums[0] = 0;
            for(int n = 1; n < 5; n++){
                if(sums[n] < sums[filter])
                    filter = n;
            }
            bs.write(filter, 8, false);
            //scanlineIndex = 1;
            for(int j = 1; j < stride; j++){
                byte byteA = getByteA(j, unfilteredScanline, previousScanline);
                byte byteB = getByteB(j, unfilteredScanline, previousScanline);
                byte byteC = getByteC(j, unfilteredScanline, previousScanline);
                switch(filter){
                    case 0:
                        bs.write(unfilteredScanline.peek(j), 8, false);
                        break;
                    case 1:
                        bs.write(unfilteredScanline.peek(j) - byteA, 8, false);
                        break;
                    case 2:
                        bs.write(unfilteredScanline.peek(j) - byteB, 8, false);
                        break;
                    case 3:
                        bs.write((byte) (unfilteredScanline.peek(j) + ((((int) byteA & 0xFF) + ((int) byteB & 0xFF)) >> 1) & 0xFF), 8, false);
                        break;
                    case 4:
                        bs.write(unfilteredScanline.peek(j) - (byte)(computePaethPredictor(byteA, byteB, byteC) & 0xFF), 8, false);
                        break;
                }
            }
            previousScanline = unfilteredScanline;
            bs.skipToNextByte();
        }
        //Finally we have outputbitstream with all the filtered data
        //Encode using deflate algorithm
        ZLibStream zls = new ZLibStream();

        return (BufferInputStreamWrapper)zls.deflate(bs.getStream().getInputStreamWrapper());
    }
}
