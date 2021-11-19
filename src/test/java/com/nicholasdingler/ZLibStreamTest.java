package com.nicholasdingler;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class ZLibStreamTest {

    private ZLibStream z;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("encryptedZLIB");
        byte[] b = new byte[is.available()];
        is.read(b);
        z = new ZLibStream(b);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void decode() throws Exception {
        z.decode();
        assertEquals(1, z.getDecodedStream()[0]);
        assertEquals(-1, z.getDecodedStream()[1]);
        assertEquals(-1, z.getDecodedStream()[2]);
        assertEquals(-1, z.getDecodedStream()[3]);
    }

    @org.junit.jupiter.api.Test
    void bytesToIntegerBigEndian() {
        byte[] test = {0,0,1,0,1,0};
        assertEquals(0x01000100, z.bytesToIntegerBigEndian(test,4,2));
    }

    @org.junit.jupiter.api.Test
    void bytesToIntegerLittleEndian() {
        byte[] test = {0,0,1,0,1,0};
        assertEquals(0x00010001, z.bytesToIntegerLittleEndian(test,4,2));
    }
}