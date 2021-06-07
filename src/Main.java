import java.io.File;
import java.io.IOException;

public class Main {

    public static void  main (String[] args){
        if(args.length < 2 || (!args[0].equals("-d") && !args[0].equals("-i"))){
            System.out.println("Unrecognized Arguments.");
            return;
        }
        if(args[0].equals("-d")){
            File directoryPath = new File(args[1]);
            File[] fileList = directoryPath.listFiles();
            for (File file : fileList) {
                if(file.getAbsolutePath().endsWith(".png")){
                    String inputFilename = file.getAbsolutePath();
                    String outputFilename = inputFilename.substring(0, inputFilename.length() - 4) + ".bmp";
                    System.out.println("Converting file " + inputFilename + " to " + outputFilename + "...");
                    PNGImage inputImage = new PNGImage();
                    inputImage.read(inputFilename);
                    BMPImage outputImage = new BMPImage(inputImage);
                    outputImage.write(outputFilename);
                }
            }
        }
        if(args[0].equals("-i")){
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




            PNGImage inputImage = new PNGImage();
            inputImage.read(inputFilename);
            BMPImage outputImage = new BMPImage(inputImage);
            outputImage.write(outputFilename);

        }
    }
}


