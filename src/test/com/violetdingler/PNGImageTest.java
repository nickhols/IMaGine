package com.violetdingler;

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
        image.readFile(file.getAbsolutePath());
        assertEquals(255, image.pixels[0][0][0]);
        assertEquals(255, image.pixels[0][0][1]);
        assertEquals(255, image.pixels[0][0][2]);
        assertEquals(255, image.pixels[0][128][0]);
        assertEquals(0, image.pixels[0][128][1]);
        assertEquals(0, image.pixels[0][128][2]);
        assertEquals(0, image.pixels[128][0][0]);
        assertEquals(255, image.pixels[128][0][1]);
        assertEquals(0, image.pixels[128][0][2]);
        assertEquals(0, image.pixels[128][128][0]);
        assertEquals(0, image.pixels[128][128][1]);
        assertEquals(255, image.pixels[128][128][2]);
    }
}