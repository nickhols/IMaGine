package com.nicholasdingler;

public class ArrayUtil {
    static public int bytesToIntegerLittleEndian(byte[] buffer, int nBytes){
        return bytesToIntegerLittleEndian(buffer, nBytes, 0);
    }

    static public int bytesToIntegerLittleEndian(byte[] buffer, int nBytes, int offset){
        if (nBytes > 4) {
            System.out.println("Only Integers up to 4 bytes are supported by this function");
            return 0;
        }
        int output = 0;
        for (int i = 0; i < nBytes; i++){
            output += ((int)buffer[i + offset]) << (i * 8);
        }
        return output;
    }

    static public int bytesToIntegerBigEndian(byte[] buffer, int nBytes){
        return bytesToIntegerBigEndian(buffer, nBytes, 0);
    }

    static public int bytesToIntegerBigEndian(byte[] buffer, int nBytes, int offset){
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

    static public byte[] integerToBytesLittleEndian(int input, int nBytes) {
        if (nBytes > 4) {
            System.out.println("Only Integers up to 4 bytes are supported by this function");
            return new byte[0];
        }
        byte[] output = new byte[nBytes];
        for (int i = 0; i < nBytes; i++){
            output[i] = (byte)( (input >> (i * 8)) & 0xFF );
        }
        return output;
    }

    static public byte[] integerToBytesBigEndian(int input, int nBytes) {
        if (nBytes > 4) {
            System.out.println("Only Integers up to 4 bytes are supported by this function");
            return new byte[0];
        }
        byte[] output = new byte[nBytes];
        for (int i = 0; i < nBytes; i++){
            output[i] = (byte)( (input >> ((nBytes - 1 - i) * 8)) & 0xFF );
        }
        return output;
    }
}
