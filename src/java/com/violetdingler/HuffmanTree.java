package com.violetdingler;

public class HuffmanTree {
    int[] code;
    int[] bits;
    int size;
    int[] lookupTableBits;
    int[] lookupTableSymbol;

    HuffmanTree(){
        size = 512;
        code = new int[size];
        bits = new int[size];

        //int currentCode = 48;
        for(int i = 0; i < 144; i++){
//            code[i] = currentCode++;
            bits[i] = 8;
        }
//        currentCode = 400;
        for(int i = 144; i < 256; i++){
//            code[i] = currentCode++;
            bits[i] = 9;
        }
//        currentCode = 0;
        for(int i = 256; i < 280; i++){
//            code[i] = currentCode++;
            bits[i] = 7;
        }
//        currentCode = 192;
        for(int i = 280; i < 288; i++) {
//            code[i] = currentCode++;
            bits[i] = 8;
        }
        generateCodes(9);
    }

    HuffmanTree(int size){
        code = new int[size];
        bits = new int[size];
        this.size = size;
    }

    void generateCodes(int maxBits){
        //
        //This method calculates the Huffman codes of this com.violetdingler.HuffmanTree object using the code bit lengths
        //
        int maxCode = (1 << maxBits) - 1;
        int currentCode = 0;
        int[] next_code = new int[maxBits + 1];
        int[] bl_count = new int[maxBits + 1];
        lookupTableBits = new int[maxCode + 1];
        lookupTableSymbol = new int[maxCode + 1];

        for(int i = 0; i < size; i++){
            if(bits[i] != 0) {
                bl_count[bits[i]]++;
            }
        }

        for (int bit = 1; bit <= maxBits; bit++) {
            currentCode = (currentCode + bl_count[bit-1]) << 1;
            next_code[bit] = currentCode;
        }

        for (int n = 0;  n < size; n++) {
            int len = bits[n];
            if (len != 0) {
                code[n] = next_code[len];
                lookupTableBits[next_code[len]] = len;
                lookupTableSymbol[next_code[len]] = n;
                next_code[len]++;
            }
        }
    }
}
