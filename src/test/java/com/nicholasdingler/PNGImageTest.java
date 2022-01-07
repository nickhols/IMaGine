package com.nicholasdingler;

import com.nicholasdingler.image.PNGImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class PNGImageTest {

    PNGImage image;
    File file;
    @BeforeEach
    void setUp() throws URISyntaxException {
        image = new PNGImage();
        URL res = getClass().getClassLoader().getResource("testImage.png");
        file = Paths.get(res.toURI()).toFile();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void readFile() throws Exception {
        image.read(file.getAbsolutePath());
        assertEquals(255, image.getPixels()[0][0][0]);
        assertEquals(255, image.getPixels()[0][0][1]);
        assertEquals(255, image.getPixels()[0][0][2]);
        assertEquals(255, image.getPixels()[0][128][0]);
        assertEquals(0, image.getPixels()[0][128][1]);
        assertEquals(0, image.getPixels()[0][128][2]);
        assertEquals(0, image.getPixels()[128][0][0]);
        assertEquals(255, image.getPixels()[128][0][1]);
        assertEquals(0, image.getPixels()[128][0][2]);
        assertEquals(0, image.getPixels()[128][128][0]);
        assertEquals(0, image.getPixels()[128][128][1]);
        assertEquals(255, image.getPixels()[128][128][2]);
    }

    @Test
    void calculateCRC() {
        byte[] data = {0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x03, 0x52, 0x00, 0x00, 0x02, (byte) 0x9B, 0x08, 0x06, 0x00, 0x00, 0x00};
        byte[] crc = image.calculateCRC(data);
        for(int i = 0; i < 4; i++){
            System.out.println(crc[i]);
        }
    }
}