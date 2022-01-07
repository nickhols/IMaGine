package com.nicholasdingler;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;

import static org.junit.jupiter.api.Assertions.*;

class ArrayUtilTest {

    @Test
    void bytesToIntegerLittleEndian() {
        byte[] test = {0x13, 0x25, 0x62, 0x01, 0x06, 0x78};
        assertEquals(0x78060162, ArrayUtil.bytesToIntegerLittleEndian(test, 4, 2));
    }

    @Test
    void bytesToIntegerBigEndian() {
        byte[] test = {0x13, 0x25, 0x62, 0x01, 0x06, 0x78};
        assertEquals(0x62010678, ArrayUtil.bytesToIntegerBigEndian(test, 4, 2));
    }

    @Test
    void integerToBytesLittleEndian() {
        byte[] test = {0x62, 0x01, 0x06, 0x78};
        for(int i = 0; i < 4; i++){
            assertEquals( test[i], ArrayUtil.integerToBytesLittleEndian(0x78060162, 4)[i]);
        }
    }

    @Test
    void integerToBytesBigEndian() {
        byte[] test = {0x62, 0x01, 0x06, 0x78};
        for(int i = 0; i < 4; i++){
            assertEquals( test[i], ArrayUtil.integerToBytesBigEndian(0x62010678, 4)[i]);
        }
    }
}