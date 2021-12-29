package com.nicholasdingler;

import com.nicholasdingler.InputBitstream.InputLSBBitstream;
import com.nicholasdingler.InputStreamWrapper.InputStreamWrapper;
import com.nicholasdingler.OutputBitstream.OutputLSBBitstream;
import com.nicholasdingler.OutputStreamWrapper.BufferOutputStreamWrapper;
import com.nicholasdingler.OutputStreamWrapper.OutputStreamWrapper;

public class ZLibStream {
    OutputStreamWrapper deflatedStream;
    OutputStreamWrapper inflatedStream;
    int inflatedStreamIndex;
    byte[] window;
    HuffmanTree hufLiteral;
    HuffmanTree hufDistance;
    HuffmanTree hufDictionary;
    int[] distanceBaseLookupTable = {1,2,3,4,5,7,9,13,17,25,33,49,65,97,129,193,257,385,513,769,1025,1537,2049,3073,4097,6145,8193,12289,16385,24577};
    int[] distanceExtraBitLookupTable = {0,0,0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13};
    int[] lengthBaseLookupTable = {3,4,5,6,7,8,9,10,11,13,15,17,19,23,27,31,35,43,51,59,67,83,99,115,131,163,195,227,258};
    int[] lengthExtraBitLookupTable = {0,0,0,0,0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,5,0};


    public ZLibStream(){
    }

    public InputStreamWrapper getInflatedStream() throws Exception {
        return inflatedStream.getInputStreamWrapper();
    }

    public InputStreamWrapper getDeflatedStream() throws Exception {
        return deflatedStream.getInputStreamWrapper();
    }

    public InputStreamWrapper inflate(InputStreamWrapper inStream) throws Exception {
        InputLSBBitstream bs = new InputLSBBitstream(inStream);
        inflatedStream = new BufferOutputStreamWrapper();
        byte[] metaData = new byte[2];
        byte CMFByte = (byte) bs.getBits(8, false);
        metaData[0] = CMFByte;
        //CMF bits 0-3 define compression method, only defined for 8 = deflate algorithm
        //If not 8, throw exception
        if ((CMFByte & 0x0F) != 0x08){
            throw new unrecognizedZLibStreamException();
        }
        //CMF bits 4-7 define LZ77 Compression window size
        int windowSize = (int) Math.pow(2,((CMFByte >> 4) + 8));
        window = new byte[windowSize];
        byte FLGByte = (byte) bs.getBits(8, false);
        metaData[1] = FLGByte;
        //FLG bits 0-4 must be set so that CMF concatenated with FLG is divisible by 31
        //If not, throw exception
        if((bytesToIntegerBigEndian(metaData, 2, 0) % 31) != 0){
            throw new unrecognizedZLibStreamException();
        }
        //FLG bit 5 (FDICT) designates whether a dictionary is used for the adler32 sum
        byte FDICT = (byte) (FLGByte & 0b00100000);
        int dataIndex = 0;
        if (FDICT == 0b00100000){
            //dataIndex = 6;
            byte[] dummy = new byte[4];
            for(int i = 0; i < 4; i++){
                dummy[i] = (byte) bs.getBits(8, false);
            }
        }
        else {
            //dataIndex = 2;
        }
        //FLG bits 6-7 designate compression level, not used by this program


        //
        //Decompress the data
        //
        //InputLSBBitstream bs = new InputLSBBitstream(inStream);


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
                readUnecryptedBlock(bs);
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
                    generateDynamicHuffmanTrees(bs);
                }
                else{
                    //Use default huffman code lengths
                    hufLiteral = new HuffmanTree();
                    hufDistance = new HuffmanTree(30);
                    for(int i = 0; i < 30; i++){
                        hufDistance.bits[i] = 5;
                        hufDistance.generateCodes(5);
                    }
                }


                //
                //Read Bytes until end of the encrypted block
                //
                int streamState = 1;
                while(streamState != 0){
                    streamState = outputNextBytes(bs);
                }
            }
            else{
                throw new unrecognizedZLibStreamException();
            }
        }
        return inflatedStream.getInputStreamWrapper();
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
    
    public int outputNextBytes(InputLSBBitstream bs) throws Exception {
        int symbol = getNextLiteral(bs);
        if(symbol == 256){
            return 0;
        }
        if(symbol < 256){
            inflatedStream.write(symbol);
            window[inflatedStreamIndex % window.length] = (byte) symbol;
            inflatedStreamIndex++;
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
            throw new com.nicholasdingler.unrecognizedZLibStreamException();
        }
        int distance = getNextDistance(bs);
        for(int i = 0; i < length; i++){
            inflatedStream.write(window[(inflatedStreamIndex + window.length - distance) % window.length]);
            //inflatedStream[inflatedStreamIndex] = window[(inflatedStreamIndex + window.length - distance) % window.length];
            window[inflatedStreamIndex % window.length] = window[(inflatedStreamIndex + window.length - distance) % window.length];
            //decodedStream[decodedStreamIndex] = decodedStream[(decodedStreamIndex + window.length - distance) % window.length];
            inflatedStreamIndex++;
        }
        return 1;
    }

    public int getNextCodeLengths(int index, InputLSBBitstream bs) throws Exception {
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

    public int getNextLiteral(InputLSBBitstream bs) throws Exception {
        int huffmanCode = (int) bs.getNextBit();
        int codeBits = 1;
        while (hufLiteral.lookupTableBits[huffmanCode] == 0 || hufLiteral.lookupTableBits[huffmanCode] != codeBits) {
            huffmanCode = (huffmanCode << 1) | (int) bs.getNextBit();
            codeBits++;
        }
        return hufLiteral.lookupTableSymbol[huffmanCode];
    }
    
    public int getNextDistance(InputLSBBitstream bs) throws Exception {
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

    public void generateDynamicHuffmanTrees(InputLSBBitstream bs) throws Exception {

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
            count += getNextCodeLengths(count, bs);
        }
        hufLiteral.generateCodes(18);
        hufDistance.generateCodes(18);
    }

    public void readUnecryptedBlock(InputLSBBitstream bs) throws Exception {
        //Skip to the next byte boundary
        bs.skipToNextByte();
        int len = (int)bs.getBits(16, false);
        int nlen = (int)bs.getBits(16, false);
        for(int i = 0; i < len; i++){
            byte nextByte = (byte)bs.getBits(8,false);
            inflatedStream.write(nextByte);
            //inflatedStream[inflatedStreamIndex] = (byte)bs.getBits(8, false);
            window[inflatedStreamIndex % window.length] = nextByte;
            //window[inflatedStreamIndex % window.length] = inflatedStream[inflatedStreamIndex];
            inflatedStreamIndex++;
        }
    }

    //
    //Deflate Functions
    //

    public InputStreamWrapper deflate(InputStreamWrapper inStream) throws Exception{
        //this.inflatedStream = inflatedStream;
        //inflatedStreamMaxIndex = inflatedStream.length - 1;
        OutputLSBBitstream bs = new OutputLSBBitstream(new BufferOutputStreamWrapper());
        window = new byte[0x8000];
        //
        //Chained Hashmap data structure used by the deflate algorithm
        //Hashes 3 bytes from an index, stores it for future use in the compression algorithm
        //Later hashes will look through the chained hash at the relevant key to select the position with the longest overlap
        //
        class HashMapNode {
            int position;
            HashMapNode nextNode;
            HashMapNode(){
                position = 0;
                nextNode = null;
            }
            HashMapNode(int position, HashMapNode nextNode){
                this.position = position;
                this.nextNode = nextNode;
            }
            int getPosition(){
                return position;
            }
            int findOverlap() throws Exception {
                int overlapDistance = 0;
                while(inStream.addressable(inflatedStreamIndex + overlapDistance) &&
                        inStream.peek(position + overlapDistance) == inStream.peek(inflatedStreamIndex + overlapDistance)){
                    overlapDistance++;
                }
                return overlapDistance;
            }
        }
        class HashMapLinkedList{
            HashMapNode firstNode;
            HashMapLinkedList(){
                firstNode = null;
            }
            void push(int position){
                firstNode = new HashMapNode(position, firstNode);
            }
            HashMapNode getHighestOverlap() throws Exception {
                int overlap = 0;
                HashMapNode hashMapIterator = firstNode;
                HashMapNode returnNode = null;
                while(hashMapIterator != null && hashMapIterator.getPosition() >= inflatedStreamIndex - window.length){
                    int tempOverlap = hashMapIterator.findOverlap();
                    if(tempOverlap > overlap){
                        overlap = tempOverlap;
                        returnNode = hashMapIterator;
                    }
                    hashMapIterator = hashMapIterator.nextNode;
                }
                return returnNode;
            }
        }
        //
        //
        //
        int hashTableSize = 101;
        HashMapLinkedList[] hashMap = new HashMapLinkedList[hashTableSize];
        for(int i = 0; i < hashTableSize; i++){
            hashMap[i] = new HashMapLinkedList();
        }
        //Use default huffman code lengths
        hufLiteral = new HuffmanTree();
        hufDistance = new HuffmanTree(30);
        for(int i = 0; i < 30; i++){
            hufDistance.bits[i] = 5;
            hufDistance.generateCodes(5);
        }
        bs.write(0x78, 8, false);
        bs.write(0x1,8,false);
        bs.write(0x1, 1, false);
        bs.write(0x01, 2, false);
        //
        //Iterate through data
        //
        while(inStream.addressable(inflatedStreamIndex + 2)){
            int hashInput = ((int)inStream.peek(inflatedStreamIndex)) << 16;
            hashInput += ((int)inStream.peek(inflatedStreamIndex)) << 8;
            hashInput += ((int)inStream.peek(inflatedStreamIndex));
            int key = getKey(hashInput, hashTableSize);

            HashMapNode overlapNode = hashMap[key].getHighestOverlap();
            if(overlapNode != null && overlapNode.findOverlap() > 2){
                //Use length/distance huffman codes
                //
                //Find the closest distance base value, and add the extra bits to reach the actual distance
                //Do the same for the overlap length
                //
                int distance = inflatedStreamIndex - overlapNode.getPosition();
                int length = overlapNode.findOverlap();
                int distanceLookupIndex = 0;
                while(distanceLookupIndex <= 30 && distanceBaseLookupTable[distanceLookupIndex] <= distance){
                    distanceLookupIndex++;
                }
                int distanceExtraBits = distance - distanceBaseLookupTable[--distanceLookupIndex];
                int lengthLookupIndex = 0;
                while(lengthLookupIndex <= 29 && lengthBaseLookupTable[lengthLookupIndex] <= length){
                    lengthLookupIndex++;
                }
                int lengthExtraBits = length - lengthBaseLookupTable[--lengthLookupIndex];
                bs.write(hufLiteral.code[lengthLookupIndex + 257], hufLiteral.bits[lengthLookupIndex+257],true);
                bs.write(lengthExtraBits, lengthExtraBitLookupTable[lengthLookupIndex], false);
                bs.write(hufDistance.code[distanceLookupIndex], hufDistance.bits[distanceLookupIndex], true);
                bs.write(distanceExtraBits, distanceExtraBitLookupTable[distanceLookupIndex], false);
                //Store position and increment
                hashMap[key].push(inflatedStreamIndex);
                inflatedStreamIndex += length;
            }
            else{
                //Use raw data code
                bs.write(hufLiteral.code[(int)inStream.peek(inflatedStreamIndex)], hufLiteral.bits[(int)inStream.peek(inflatedStreamIndex)], true);
                //Store position and increment
                hashMap[key].push(inflatedStreamIndex++);
            }
        }
        //
        //Use Huffman codes for final 2 literals, assuming they weren't copied from earlier in stream
        //
        while(inStream.addressable(inflatedStreamIndex)){
            bs.write(hufLiteral.code[(int)inStream.peek(inflatedStreamIndex)], hufLiteral.bits[(int)inStream.peek(inflatedStreamIndex)], true);
            inflatedStreamIndex++;
        }
        bs.write(hufLiteral.code[256], hufLiteral.bits[256], true);
        bs.close();
        return bs.getStream().getInputStreamWrapper();
    }

    public int getKey(int value, int modulo){
        int key;
        key = (value >> 24) & 0xFF;
        key += (value >> 16) & 0xFF;
        key += (value >> 8) & 0xFF;
        key += (value) & 0xFF;
        key = key % modulo;
        return key;
    }
}
