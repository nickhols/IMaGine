package com.violetdingler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LSBBitstreamTest {

    LSBBitstream bs;

    @BeforeEach
    void setUp() {
        byte[] buf = {(byte)0xAA,(byte)0xFF,(byte)0xF0,(byte)0x00};
        bs = new LSBBitstream(buf);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void EOF() throws Exception {
        assert(!bs.EOF());
        bs.getBits(4,false);
        bs.skipToNextByte();
        bs.getBits(4,false);
        bs.skipToNextByte();
        bs.getBits(4,false);
        bs.skipToNextByte();
        bs.getBits(4,false);
        bs.skipToNextByte();
        assert(bs.EOF());
    }

    @Test
    void getBits() throws Exception {
        assertEquals(0xA, bs.getBits(4, false));
        assert(!bs.EOF());
        assertEquals(0x5F, bs.getBits(8, true));
        assertEquals(0xF, bs.getBits(4, false));
        assertEquals(0xF0, bs.getBits(16, false));
        assert(bs.EOF());
    }

    @Test
    void getNextBit() {
        assertEquals(0,bs.getNextBit());
        assertEquals(1,bs.getNextBit());
        assertEquals(0,bs.getNextBit());
        assertEquals(1,bs.getNextBit());
    }

    @Test
    void skipToNextByte() throws Exception {
        assert(!bs.EOF());
        bs.getBits(4,false);
        bs.skipToNextByte();
        bs.getBits(4,false);
        bs.skipToNextByte();
        bs.getBits(4,false);
        bs.skipToNextByte();
        bs.getBits(4,false);
        bs.skipToNextByte();
        assert(bs.EOF());
    }
}