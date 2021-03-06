package com.nicholasdingler;

import com.nicholasdingler.InputStreamWrapper.FileInputStreamWrapper;
import com.nicholasdingler.image.BMPImage;
import com.nicholasdingler.image.PNGImage;

import java.io.File;

public class Main {

    public static void  main (String[] args){
        if(args.length < 2 || (!args[0].equals("-d") && !args[0].equals("-i") && !args[0].equals("-pngTest"))){
            System.out.println("Unrecognized Arguments.");
            return;
        }
        if(args[0].equals("-pngTest")){
            String outputFilename;
            String inputFilename = args[1];
            outputFilename = args[1].substring(0, args[1].length() - 4) + "Test.png";

            convertPNGtoPNG(inputFilename, outputFilename);

        }
        else if(args[0].equals("-d")){
            File directoryPath = new File(args[1]);
            File[] fileList = directoryPath.listFiles();
            for (File file : fileList) {
                if(file.getAbsolutePath().toLowerCase().endsWith(".png")){
                    String inputFilename = file.getAbsolutePath();
                    String outputFilename = inputFilename.substring(0, inputFilename.length() - 4) + ".bmp";
                    System.out.println("Converting file " + inputFilename + " to " + outputFilename + "...");

                    convertPNGtoBMP(inputFilename, outputFilename);
                }
            }
        }
        else if(args[0].equals("-i")){
            if(!args[1].endsWith(".png")){
                System.out.println("This Program only supports reading png files at the current time.");
                return;
            }
            if(args.length > 2 && !args[2].endsWith(".bmp")){
                System.out.println("This Program only supports writing bmp files at the current time.");
                return;
            }
            String outputFilename;
            String inputFilename = args[1];
            if (args.length > 2 && args[2].endsWith(".bmp")){
                outputFilename = args[2];
            }
            else{
                outputFilename = args[1].substring(0, args[1].length() - 4) + ".bmp";
            }

            convertPNGtoBMP(inputFilename, outputFilename);
        }
    }

    public static void convertPNGtoBMP(String inputFilename, String outputFilename){
        try {
            PNGImage inputImage = new PNGImage();
            inputImage.read(inputFilename);
            BMPImage outputImage = new BMPImage(inputImage);
            outputImage.write(outputFilename);
        } catch(Exception e){
            System.out.println("There was an error converting " + inputFilename + " into " + outputFilename + ". Please ensure the input file is correctly formatted.");
            e.printStackTrace();
        }
    }

    public static void convertPNGtoPNG(String inputFilename, String outputFilename){
        try {
            PNGImage inputImage = new PNGImage();
            inputImage.read(inputFilename);
            PNGImage outputImage = new PNGImage(inputImage);
            outputImage.write(outputFilename);
        } catch(Exception e){
            System.out.println("There was an error converting " + inputFilename + " into " + outputFilename + ". Please ensure the input file is correctly formatted.");
            e.printStackTrace();
        }
    }

}


