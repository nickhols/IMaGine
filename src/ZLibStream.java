public class ZLibStream {
    byte[] encodedStream;
    byte[] decodedStream;
    int decodedStreamIndex;
    byte[] window;
    HuffmanTree hufLiteral;
    HuffmanTree hufDistance;
    HuffmanTree hufDictionary;
    LSBBitstream bs;
    int[] distanceBaseLookupTable = {1,2,3,4,5,7,9,13,17,25,33,49,65,97,129,193,257,385,513,769,1025,1537,2049,3073,4097,6145,8193,12289,16385,24577};
    int[] distanceExtraBitLookupTable = {0,0,0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13};

    ZLibStream(byte[] encodedStream){
        this.encodedStream = encodedStream;
        decodedStream = new byte[100000000];
    }
    
    public byte[] decode(){
        try {
            byte CMFByte = encodedStream[0];
            //CMF bits 0-3 define compression method, only defined for 8 = deflate algorithm
            //If not 8, throw exception
            if ((CMFByte & 0x0F) != 0x08){
                throw new unrecognizedZLibStreamException();
            }
            //CMF bits 4-7 define LZ77 Compression window size
            int windowSize = (int) Math.pow(2,((CMFByte >> 4) + 8));
            window = new byte[windowSize];
            byte FLGByte = encodedStream[1];
            //FLG bits 0-4 must be set so that CMF concatenated with FLG is divisible by 31
            //If not, throw exception
            if((bytesToIntegerBigEndian(encodedStream, 2, 0) % 31) != 0){
                throw new unrecognizedZLibStreamException();
            }
            //FLG bit 5 (FDICT) designates whether a dictionary is used for the adler32 sum
            byte FDICT = (byte) (FLGByte & 0b00100000);
            int dataIndex = 0;
            if (FDICT == 0b00100000){
                dataIndex = 6;
            }
            else {
                dataIndex = 2;
            }
            //FLG bits 6-7 designate compression level, not used by this program


            //
            //Decompress the data
            //
            bs = new LSBBitstream(encodedStream, dataIndex/*, encodedStream.length - 4 - dataIndex*/);


            //
            //read blocks until end of data
            //
            int BFINAL = 0; //Indicates if this is the final block of data
            int BTYPE = 0; //2 bit number indicating compression of block: 00=none, 01=fixed Huffman codes, 10 = dynamic huffman codes
            while(BFINAL != 1 && !bs.EOF()){
                BFINAL = (int)bs.getNextBit();
                BTYPE = (int) bs.getBits(2, false);
                if(BTYPE == 0){
                    //
                    //Read Unecrypted Block
                    //
                    readUnecryptedBlock();
                }
                else if(BTYPE == 1 || BTYPE == 2){
                    //
                    //Block is encrypted
                    //
                    //Generate Huffman Trees for this block
                    //BTYPE == 1, Fixed Huffman Codes
                    //BTYPE == 2, Dynamic Huffman Codes
                    //
                    if(BTYPE == 2){
                        //Use given huffman code lengths
                        //Do something...
                        generateDynamicHuffmanTrees();
                    }
                    else{
                        //Use default huffman code lengths
                        hufLiteral = new HuffmanTree();
                        hufDistance = new HuffmanTree();
                    }


                    //
                    //Read Bytes until end of the encrypted block
                    //
                    int streamState = 1;
                    while(streamState != 0){
                        streamState = outputNextBytes();
                    }
                }
                else{
                    throw new unrecognizedZLibStreamException();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] decodedStream = new byte[decodedStreamIndex];
        System.arraycopy(this.decodedStream, 0, decodedStream, 0, decodedStreamIndex);
        return decodedStream;
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

    public int bytesToIntegerLittleEndian(byte[] buffer, int bytes, int offset){
        if (bytes > 4) {
            System.out.println("Only Integers up to 4 bytes are supported by this function");
            return 0;
        }
        int output = 0;
        for (int i = 0; i < bytes; i++){
            output += ( ( (int)buffer[i + offset]) & 0xff) << (i * 8);
        }
        return output;
    }
    
    public int outputNextBytes(){
        int symbol = getNextLiteral();
        if(symbol == 256){
            return 0;
        }
        if(symbol < 256){
            decodedStream[decodedStreamIndex] = (byte)symbol;
            window[decodedStreamIndex % window.length] = decodedStream[decodedStreamIndex];
            decodedStreamIndex++;
            return 1;
        }
        //
        //If execution gets here, a length/distance pair is involved
        //
        int length = 0;
        if(symbol < 265){
            length = symbol - 254;
        }
        else if(symbol < 269){
            length = (symbol << 1) - 520 + 1;
            length += bs.getBits(1, false);
        }
        else if(symbol < 273){
            length = (symbol << 2) - 1057;
            length += bs.getBits(2, false);
        }
        else if(symbol < 277){
            length = (symbol << 3) - 2149;
            length += bs.getBits(3, false);
        }
        else if(symbol < 281){
            length = (symbol << 4) - 4365;
            length += bs.getBits(4, false);
        }
        else if(symbol < 285){
            length = (symbol << 5) - 8861;
            length += bs.getBits(5, false);
        }
        else if (symbol == 285){
            length = 258;
        }
        else{
            //throw new unrecognizedZLibStreamException();
        }
        int distance = getNextDistance();
        for(int i = 0; i < length; i++){
            decodedStream[decodedStreamIndex] = window[(decodedStreamIndex + window.length - distance) % window.length];
            window[decodedStreamIndex % window.length] = decodedStream[decodedStreamIndex];
            //decodedStream[decodedStreamIndex] = decodedStream[(decodedStreamIndex + window.length - distance) % window.length];
            decodedStreamIndex++;
        }
        return 1;
    }

    public int getNextCodeLengths(int index){
        //
        //index = which code length to fill in using the next found code length
        //      this counts hufDistance as an extension of hufLiteral when index goes above the size of hufLiteral
        //return = number of code Lengths filled in by this function call
        //This is because Huffman Codes 16-18 repeat code lengths some amount of times
        //
        int huffmanCode = (int)bs.getNextBit();
        int codeBits = 1;
        int repeat = 0;
        int i = 0;
        while(hufDictionary.lookupTableBits[huffmanCode] == 0 || hufDictionary.lookupTableBits[huffmanCode] != codeBits){
            huffmanCode = (huffmanCode << 1) | (int)bs.getNextBit();
            codeBits++;
        }


        switch(hufDictionary.lookupTableSymbol[huffmanCode]){
            case 16:
                repeat = (int)bs.getBits(2, false) + 3;
                for(i = 0; i < repeat; i++){

                    if(index + i > hufLiteral.size){
                        hufDistance.bits[index + i - hufLiteral.size] = hufDistance.bits[index + i - hufLiteral.size - 1];
                    }
                    else if(index + i == hufLiteral.size){
                        hufDistance.bits[index + i - hufLiteral.size] = hufLiteral.bits[index + i - 1];
                    }
                    else{
                        hufLiteral.bits[index + i] = hufLiteral.bits[index + i - 1];
                    }
                }
                break;
            case 17:
                repeat = (int)bs.getBits(3, false) + 3;
                for(i = 0; i < repeat; i++){
                    if(index >= hufLiteral.size){
                        hufDistance.bits[index + i - hufLiteral.size] = 0;
                    }
                    else{
                        hufLiteral.bits[index + i] = 0;
                    }
                }
                break;
            case 18:
                repeat = (int)bs.getBits(7, false) + 11;
                for(i = 0; i < repeat; i++){
                    if(index >= hufLiteral.size){
                        hufDistance.bits[index + i - hufLiteral.size] = 0;
                    }
                    else{
                        hufLiteral.bits[index + i] = 0;
                    }
                }
                break;
            default:
                if(index >= hufLiteral.size){
                    hufDistance.bits[index - hufLiteral.size] = hufDictionary.lookupTableSymbol[huffmanCode];
                }
                else{
                    hufLiteral.bits[index] = hufDictionary.lookupTableSymbol[huffmanCode];
                }
                i++;
                break;
        }
        return i;
    }

    public int getNextLiteral(){
        int huffmanCode = (int) bs.getNextBit();
        int codeBits = 1;
        while (hufLiteral.lookupTableBits[huffmanCode] == 0 || hufLiteral.lookupTableBits[huffmanCode] != codeBits) {
            huffmanCode = (huffmanCode << 1) | (int) bs.getNextBit();
            codeBits++;
        }
        return hufLiteral.lookupTableSymbol[huffmanCode];
    }
    
    public int getNextDistance(){
        int huffmanCode = (int) bs.getNextBit();
        int codeBits = 1;
        while (hufDistance.lookupTableBits[huffmanCode] == 0 || hufDistance.lookupTableBits[huffmanCode] != codeBits) {
            huffmanCode = (huffmanCode << 1) | (int) bs.getNextBit();
            codeBits++;
        }
        int distance = distanceBaseLookupTable[hufDistance.lookupTableSymbol[huffmanCode]];
        int extraBits = distanceExtraBitLookupTable[hufDistance.lookupTableSymbol[huffmanCode]];
        distance += bs.getBits(extraBits, false);
        return distance;
    }

    public void generateDynamicHuffmanTrees(){

        int HLIT = (int)bs.getBits(5,false);
        int HDIST = (int)bs.getBits(5,false);
        int HCLEN = (int)bs.getBits(4, false);
//        System.out.println(HLIT);
//        System.out.println(HDIST);
//        System.out.println(HCLEN);
        int code = 0;
        //HLIT = # of Literal/Length codes - 257
        //HDIST = # of Distance Codes - 1
        //HCLEN = # of Code Length Codes - 4


        //
        //Get the Code Lengths for the Huffman Codes for the OTHER Huffman Codes
        //
        hufDictionary = new HuffmanTree(19);
        hufDictionary.bits[16] = (int)bs.getBits(3, false);
        hufDictionary.bits[17] = (int)bs.getBits(3, false);
        hufDictionary.bits[18] = (int)bs.getBits(3, false);
        hufDictionary.bits[0] = (int)bs.getBits(3, false);
        for(int i = 0; i < (HCLEN + 1) / 2; i++){
            hufDictionary.bits[8+i] = (int)bs.getBits(3, false);
            if((HCLEN % 2) == 0 || i != ((HCLEN / 2))){
                hufDictionary.bits[7-i] = (int)bs.getBits(3, false);
            }
        }
        hufDictionary.generateCodes(7);//2^3 = 8 - 1 = 7
        //This creates the new Huffman Tree for finding the block's literal/length and distance huffman trees


        hufLiteral = new HuffmanTree(HLIT + 257);
        hufDistance = new HuffmanTree(HLIT + 257);

        //
        //Fill the 2 trees using the next HLIT + HDIST + 258 huffman Codes
        //
        int count = 0;
        while(count < HLIT + HDIST + 258){
            count += getNextCodeLengths(count);
        }
        hufLiteral.generateCodes(18);
        hufDistance.generateCodes(18);
    }

    public void readUnecryptedBlock(){
        //Skip to the next byte boundary
        bs.skipToNextByte();
        int len = bytesToIntegerLittleEndian(bs.inputBuffer, 2, bs.nBytesRead);
        int nlen = bytesToIntegerLittleEndian(bs.inputBuffer, 2, bs.nBytesRead + 2);
        for(int i = 0; i < len; i++){
            decodedStream[decodedStreamIndex] = bs.inputBuffer[bs.nBytesRead + 4 + i];
            window[decodedStreamIndex % window.length] = decodedStream[decodedStreamIndex];
            decodedStreamIndex++;
        }
        bs.nBytesRead += len + 4;
    }
}
