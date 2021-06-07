public class PNGImage extends Image{
    private int colorType;
    private int compression;
    private int interlacing;
    private int filter;
    private byte[] IDAT = {};
    private byte[] dataStream;
    private byte[] rawScanlines;
    private byte[][] palette;

    //private int stride;
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

    public void read(String filename){
        try {
            FileInputStreamWrapper fin = new FileInputStreamWrapper(filename);
            readMetaData(fin);
            readData(fin);
            ZLibStream zl = new ZLibStream(IDAT);
            dataStream = zl.decode();
            //parseScanlines();
            parseDatastream();
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readMetaData(FileInputStreamWrapper fin){
        try {
            //Ensure that the
            int bufferSize = 10000;
            byte[] buffer = new byte[bufferSize];
            fin.read(buffer, 0, 8);
            byte[] signature = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};
            if(!bufferEquals(buffer, signature, 8)){
                System.out.println("Invalid PNG Format.");
                throw new unrecognizedFormatException();
            }


            //Read IHDR Chunk
            byte[] IHDRChunk = readChunk(fin);
            //Get important data from the IHDR buffer, including dimensions, BPP, Color type, Compression method, Filter type, and Interlacing format
            setWidth(bytesToIntegerBigEndian(IHDRChunk, 4, 8));
            setHeight(bytesToIntegerBigEndian(IHDRChunk, 4, 12));
            setBitdepth(bytesToIntegerBigEndian(IHDRChunk, 1, 16));
            setColorType(bytesToIntegerBigEndian(IHDRChunk, 1, 17));
            setCompression(bytesToIntegerBigEndian(IHDRChunk, 1, 18));
            setFilter(bytesToIntegerBigEndian(IHDRChunk, 1, 19));
            setInterlacing(bytesToIntegerBigEndian(IHDRChunk, 1, 20));
            switch(colorType){
                case 0://Greyscale
                    BPP = bitdepth;
                    break;
                case 2:
                    BPP = bitdepth * 3;
                    break;
                case 3:
                    BPP = bitdepth;
                    break;
                case 4:
                    BPP = bitdepth * 2;
                    break;
                case 6:
                    BPP = bitdepth * 4;
                    break;
                default:
                    throw new unrecognizedFormatException();
            }
            //stride = 1 + ( ( BPP * width + ((BPP * width) % 8)) / 8);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readData(FileInputStreamWrapper fin){
        byte[] IDATSignature = {'I', 'D', 'A', 'T'};
        byte[] IENDSignature = {'I', 'E', 'N', 'D'};
        byte[] PLTESignature = {'P', 'L', 'T', 'E'};
        try{
            while (!fin.EOF()){ //read chunks until end of file
                byte[] chunk = readChunk(fin);
                if(bufferEqualsOffset(chunk, IDATSignature, 4, 0, 4)){//Chunk is an IDAT Chunk, Critical/Public
                    //Create new IDAT Array to replace old one
                    //Because arrays can't be resized, and multiple IDAT Chunks are possible and should be concatenated
                    byte[] IDATNew = new byte[IDAT.length + bytesToIntegerBigEndian(chunk, 4, 0)];
                    System.arraycopy(IDAT, 0, IDATNew, 0, IDAT.length);
                    System.arraycopy(chunk, 8, IDATNew, IDAT.length, bytesToIntegerBigEndian(chunk, 4, 0));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    private byte[] readChunk(FileInputStreamWrapper fin){
        try {
            byte[] lengthBuffer = new byte[4];
            fin.read(lengthBuffer, 0, 4);
            int chunkLength = bytesToIntegerBigEndian(lengthBuffer,4, 0);
            byte[] buffer = new byte[chunkLength + 12];
            for(int i = 0; i < 4; i ++){
                buffer[i] = lengthBuffer[i];
            }
            fin.read(buffer, 4, chunkLength + 8);
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parseDatastream() {
        pixels = new int[height][width][4];
        //MSBBitstream bs = new MSBBitstream(filteredScanlines);
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

    private int parseSubImage(int pass, int dataStreamOffset){
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

    private void getNextScanline(int nBytesInScanline, int inputOffset, byte[] output, int outputOffset){
        System.arraycopy(dataStream, inputOffset, output, outputOffset, nBytesInScanline);
//        for(int i = 0; i < nBytesInScanline; i++){
//            output[i + outputOffset] = dataStream[]
//        }
    }

//    private int[] parseScanline(MSBBitstream bs, int scanlineIndex, int pass){
//        //
//        //This method will extract the next scanline from the datastream
//        //for a non-interlaced image passNumber will be set to 0
//        //
//        int nPixels = 0;
//        int pixelDifferential = 0;
//        int firstPixel = 0;
//        switch(pass){
//            case 1:
//                nPixels = (width + 7)/8;
//                pixelDifferential = 8;
//                firstPixel = 0;
//                break;
//            case 2:
//                nPixels = (width + 3)/8;
//                pixelDifferential = 8;
//                firstPixel = 4;
//                break;
//            case 3:
//                nPixels = (width + 3)/4;
//                pixelDifferential = 4;
//                firstPixel = 0;
//                break;
//            case 4:
//                nPixels = (width + 1)/4;
//                pixelDifferential = 4;
//                firstPixel = 2;
//                break;
//            case 5:
//                nPixels = (width + 1)/2;
//                pixelDifferential = 2;
//                firstPixel = 0;
//                break;
//            case 6:
//                nPixels = (width)/2;
//                pixelDifferential = 2;
//                firstPixel = 1;
//                break;
//            case 7:
//            case 0:
//                nPixels = (width);
//                pixelDifferential = 1;
//                firstPixel = 0;
//                break;
//        }
//        for(int i = 0; i < nPixels; i++){
//            int value = 0;
//            int pixelNumber = i * pixelDifferential + firstPixel;
////            switch(colorType){
////                case 0:
////                    value = (int) bs.getBits(bitdepth, false);
////                    pixels[scanlineIndex][pixelNumber][0] = value;
////                    pixels[scanlineIndex][pixelNumber][1] = value;
////                    pixels[scanlineIndex][pixelNumber][2] = value;
////                    pixels[scanlineIndex][pixelNumber][3] = 0xFFFF;
////                    break;
////                case 2:
////                    pixels[scanlineIndex][pixelNumber][0] = (int)bs.getBits(bitdepth, false);
////                    pixels[scanlineIndex][pixelNumber][1] = (int)bs.getBits(bitdepth, false);
////                    pixels[scanlineIndex][pixelNumber][2] = (int)bs.getBits(bitdepth, false);
////                    pixels[scanlineIndex][pixelNumber][3] = 0xFFFF;
////                    break;
////                case 3:
////                    value = (int) bs.getBits(bitdepth, false);
////                    pixels[scanlineIndex][pixelNumber][0] = palette[value][0];
////                    pixels[scanlineIndex][pixelNumber][1] = palette[value][1];
////                    pixels[scanlineIndex][pixelNumber][2] = palette[value][2];
////                    pixels[scanlineIndex][pixelNumber][3] = 0xFFFF;
////                    break;
////                case 4:
////                    value = (int) bs.getBits(bitdepth, false);
////                    pixels[scanlineIndex][pixelNumber][0] = value;
////                    pixels[scanlineIndex][pixelNumber][1] = value;
////                    pixels[scanlineIndex][pixelNumber][2] = value;
////                    pixels[scanlineIndex][pixelNumber][3] = (int) bs.getBits(bitdepth, false);
////                    break;
////                case 6:
////                    pixels[scanlineIndex][pixelNumber][0] = (int)bs.getBits(bitdepth, false);
////                    pixels[scanlineIndex][pixelNumber][1] = (int)bs.getBits(bitdepth, false);
////                    pixels[scanlineIndex][pixelNumber][2] = (int)bs.getBits(bitdepth, false);
////                    pixels[scanlineIndex][pixelNumber][3] = (int)bs.getBits(bitdepth, false);
////                    break;
////            }
//        }
//        bs.skipToNextByte();
//
//    }

    private void pushSubImage(byte[] subImageScanlines, int nPixelsInScanline, int nScanlines, int firstScanline, int scanlineDifferential, int pixelDifferential, int firstPixel){
//        int nPixelsInScanline = 0;
//        int nScanlines = 0;
//        int firstScanline = 0;
//        int scanlineDifferential = 0;
//        int pixelDifferential = 0;
//        int firstPixel = 0;
//        switch(pass){
//            case 1:
//                nPixelsInScanline = (width + 7)/8;
//                nScanlines = (height + 7) / 8;
//                firstScanline = 0;
//                scanlineDifferential = 0;
//                pixelDifferential = 8;
//                firstPixel = 0;
//                break;
//            case 2:
//                nPixelsInScanline = (width + 3)/8;
//                nScanlines = (height + 7) / 8;
//                firstScanline = 0;
//                scanlineDifferential = 0;
//                pixelDifferential = 8;
//                firstPixel = 4;
//                break;
//            case 3:
//                nPixelsInScanline = (width + 3)/4;
//                nScanlines = (height + 3) / 8;
//                firstScanline = 4;
//                scanlineDifferential = 8;
//                pixelDifferential = 4;
//                firstPixel = 0;
//                break;
//            case 4:
//                nPixelsInScanline = (width + 1)/4;
//                nScanlines = (height + 3) / 4;
//                firstScanline = 0;
//                scanlineDifferential = 4;
//                pixelDifferential = 4;
//                firstPixel = 2;
//                break;
//            case 5:
//                nPixelsInScanline = (width + 1)/2;
//                nScanlines = (height + 1) / 4;
//                firstScanline = 2;
//                scanlineDifferential = 4;
//                pixelDifferential = 2;
//                firstPixel = 0;
//                break;
//            case 6:
//                nPixelsInScanline = (width)/2;
//                nScanlines = (height + 1) / 2;
//                firstScanline = 0;
//                scanlineDifferential = 2;
//                pixelDifferential = 2;
//                firstPixel = 1;
//                break;
//            case 7:
//                nPixelsInScanline = (width);
//                nScanlines = (height) / 2;
//                firstScanline = 1;
//                scanlineDifferential = 2;
//                break;
//            case 0:
//                nPixelsInScanline = (width);
//                nScanlines = height;
//                firstScanline = 0;
//                scanlineDifferential = 1;
//                pixelDifferential = 1;
//                firstPixel = 0;
//                break;
//        }
        MSBBitstream subImageBitStream = new MSBBitstream(subImageScanlines);
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

    private byte[] unfilterScanlines(byte[] filteredScanlines, int nScanlines, int filteredStride){
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

/*    private void parseScanlines(){
        reconstructScanlines();
        if(filteredScanlines.length != height * stride && interlacing == 0){
            System.out.println("Unexpected data length.");
            return;
        }
        pixels = new int[height][width][4];

        MSBBitstream bs = new MSBBitstream(rawScanlines);
        int value = 0;
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                switch(colorType){
                    case 0:
                        value = (int) bs.getBits(bitdepth, false);
                        pixels[i][j][0] = value;
                        pixels[i][j][1] = value;
                        pixels[i][j][2] = value;
                        pixels[i][j][3] = 0xFFFF;
                        break;
                    case 2:
                        pixels[i][j][0] = (int)bs.getBits(bitdepth, false);
                        pixels[i][j][1] = (int)bs.getBits(bitdepth, false);
                        pixels[i][j][2] = (int)bs.getBits(bitdepth, false);
                        pixels[i][j][3] = 0xFFFF;
                        break;
                    case 3:
                        value = (int) bs.getBits(bitdepth, false);
                        pixels[i][j][0] = palette[value][0];
                        pixels[i][j][1] = palette[value][1];
                        pixels[i][j][2] = palette[value][2];
                        pixels[i][j][3] = 0xFFFF;
                        break;
                    case 4:
                        value = (int) bs.getBits(bitdepth, false);
                        pixels[i][j][0] = value;
                        pixels[i][j][1] = value;
                        pixels[i][j][2] = value;
                        pixels[i][j][3] = (int) bs.getBits(bitdepth, false);
                        break;
                    case 6:
                        pixels[i][j][0] = (int)bs.getBits(bitdepth, false);
                        pixels[i][j][1] = (int)bs.getBits(bitdepth, false);
                        pixels[i][j][2] = (int)bs.getBits(bitdepth, false);
                        pixels[i][j][3] = (int)bs.getBits(bitdepth, false);
                        break;
                }
            }
        }
    }

    private void reconstructScanlines() {
        rawScanlines = new byte[stride * height];
        for(int i = 0; i < height; i++){
            int filter = filteredScanlines[i*stride];
            for(int j = 0; j < stride - 1; j++){
                byte byteA = getByteA(i * (stride - 1) + j);
                byte byteB = getByteB(i * (stride - 1) + j);
                byte byteC = getByteC(i * (stride - 1) + j);
                switch(filter){
                    case 0://No Filter
                        rawScanlines[i * (stride - 1) + j] = filteredScanlines[i * stride + j + 1];
                    break;
                    case 1://Sub Filter
                        rawScanlines[i * (stride - 1) + j] = (byte) (filteredScanlines[i * stride + j + 1] + byteA);
                    break;
                    case 2://Up Filter
                        rawScanlines[i * (stride - 1) + j] = (byte) (filteredScanlines[i * stride + j + 1] + byteB);
                        break;
                    case 3://Average Filter
                        rawScanlines[i * (stride - 1) + j] = (byte) (filteredScanlines[i * stride + j + 1] + ((((int)byteA & 0xFF) + ((int)byteB & 0xFF) ) >> 1) & 0xFF);
                        break;
                    case 4://Paeth Filter
                        rawScanlines[i * (stride - 1) + j] = (byte) (filteredScanlines[i * stride + j + 1] + computePaethPredictor(byteA, byteB, byteC));
                        break;
                }
            }
        }
    }*/

    private byte getByteA(int index, byte[] rawScanlines, int stride){
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

    private byte getByteB(int index, byte[] rawScanlines, int stride){
        byte byteB = 0;
        if(index >= stride){
            byteB = rawScanlines[index - stride];
        }
        return byteB;
    }

    private byte getByteC(int index, byte[] rawScanlines, int stride){
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

    private void parsePalette(byte[] chunk){
        palette = new byte[(chunk.length - 12)/3][3];
        for(int i = 0; i < palette.length; i++){
            palette[i][0] = chunk[3*i + 0 + 8];
            palette[i][1] = chunk[3*i + 1 + 8];
            palette[i][2] = chunk[3*i + 2 + 8];
        }
    }
}
