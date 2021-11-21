package com.nicholasdingler;

import com.nicholasdingler.InputStreamWrapper.BufferInputStreamWrapper;
import com.nicholasdingler.InputStreamWrapper.InputStreamWrapper;

import java.io.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ZLibStreamTest {

    private ZLibStream z;
    private byte[] b;
    private BufferInputStreamWrapper isw;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("encryptedZLIB");
        byte[] b = new byte[is.available()];
        is.read(b);
        isw = new BufferInputStreamWrapper(b);
        z = new ZLibStream();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void decode() throws Exception {
        z.inflate(isw);
        isw = (BufferInputStreamWrapper) z.getInflatedStream();
        assertEquals(1, isw.read());
        assertEquals(-1, isw.read());
        assertEquals(-1, isw.read());
        assertEquals(-1, isw.read());
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

    @org.junit.jupiter.api.Test
    void write() throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("Lorem.txt");
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        while(stream.available() > 0){
            ba.write(stream.read());
        }

        byte[] input = ba.toByteArray();
        InputStreamWrapper test = z.deflate(new BufferInputStreamWrapper(input));
        z = new ZLibStream();
        z.inflate(test);
        for(int i = 0; i < input.length; i++){
            System.out.print((char)input[i]);
        }
        System.out.println();
        byte[] output = z.getInflatedStream().readAll();
        for(int i = 0; i < output.length; i++){
            System.out.print((char)output[i]);
        }
        assert(Arrays.equals(input,z.getInflatedStream().readAll()));
    }
}