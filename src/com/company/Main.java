package com.company;

import java.awt.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.Math;
import java.util.Objects;


public class Main {

    static int intervalSize = 16;     //
    static int BIT = (int)(Math.log(Math.pow(256.0/intervalSize, 3))/Math.log(2.0));
    static int intervals = (256/intervalSize);

    static Color[] codebook = new Color[(int)Math.pow(intervals, 3)];
    //String path = "C:\\Users\\";

    /**
     * Creates a codebook of [intervals]^3 colors, where there are i values for R, G, B each
     */
    public static void createCodebook(){
        int i=0;
        for (int z = 0; z < intervals; z++){
            for (int y = 0; y < intervals; y++){
                for (int x = 0; x < intervals; x++){
                    codebook[i] = new Color(x*intervalSize, y*intervalSize, z*intervalSize);
                    //System.out.println(i +" "+ x +" "+ y +" "+ z);
                    //System.out.println(codebook[i]);
                    i++;
                }
            }
        }
        System.out.println("Interval size: " + intervalSize + "; Intervals: " + intervals + "; Bits: " + BIT + "; Codebook length " + codebook.length);
        System.out.println("Codebook initialized.");

    }

    /**
     * Finds the best matching codebook vector for any RGB color
     * @param c RGB color
     * @return index of matching codebook vector
     */
    public static int matchCodebookVector(Color c){
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        return ((r-(r%intervalSize))/intervalSize) + intervalSize*((g-(g%intervalSize))/intervalSize) + (int)Math.pow(intervalSize, 2)*((b-(b%intervalSize))/intervalSize);
    }

    /**
     * Looks up a decimal number in the codebook and returns it
     * @param index index of the codebook vector
     * @return color of the vector (RGB color can be viewed as 3-dimensional vector)
     */
    public static Color lookupCodebookVector(int index){
        return codebook[index];
    }

    /**
     * Returns a n-Bit binary representation of a given decimal number
     * @param dec Any non negative decimal number of type Integer
     * @param bit Bitlength of the output binary number
     * @return n-Bit binary representation of @dec
     * @throws NumberFormatException if decimal number is negative
     */
    public static String DecToBin(int dec, int bit) {
        if (dec < 0 || bit < 1) {
            System.out.println("Conversion of decimal to binary failed. Make sure both input and bitlength is of Integer value and not negative.");
            return null;
        }

        String bin = "";
        for (int i=0; i < bit; i++){
            bin = (Integer.toString(dec % 2)).concat(bin);
            dec = dec/2;
        }

        return bin;
    }

    /**
     * Returns corresponding decimal number to given binary number
     * @param bin binary number
     * @return decimal number
     */
    public static int BinToDec(String bin) throws NumberFormatException{
        int dec;
        dec = Integer.parseInt(bin, 2);
        return dec;
    }

    /**
     * Stores n Bit binary representation of the color of a given pixelblocksize into a .txt file
     * @param factor Sidelength of the pixelblock
     */
    public static void encoder(int factor) {
        System.out.println("----------Encoder----------");
        long start = System.currentTimeMillis();

        BufferedImage img = null;
        int width = 0;
        int height = 0;

        //read image
        try {
            File f = new File("in.jpg");
            img = ImageIO.read(f);
            width = img.getWidth();
            height = img.getHeight();
        }
        catch(IOException e) {
            System.out.println(e);
        }

        String codeVectors = DecToBin(width,12) + DecToBin(height,12) + DecToBin(factor,12);

        int counter = 0;
        for (int y = 0; y < height-(factor-1); y+=factor) {
            for (int x = 0; x < width-(factor-1); x +=factor) {
                int vector = 0;
                for (int i = 0; i < factor; i++){
                    for (int j = 0; j < factor; j++){
                        Color c = new Color(Objects.requireNonNull(img).getRGB(x+j, y+i));
                        //System.out.println(c);
                        vector += matchCodebookVector(c);
                    }
                }
                try {
                    vector /= (factor*factor);
                } catch (ArithmeticException ae) {
                    System.out.println(ae + ". Halting Encoder.");
                    return;
                }
                codeVectors = codeVectors.concat(Objects.requireNonNull(DecToBin(vector, BIT)));
                counter++;
            }
            System.out.println("Block: " + counter + "/" + ((width/factor)*(height/factor)) + "\t " + ((100*counter)/((width/factor)*(height/factor))) + "%");
        }

        store(codeVectors, "binary");

        //Get Encoder runtime
        long encodingTimeMillis = System.currentTimeMillis()-start;
        float encodingTimeSec = encodingTimeMillis/1000F;
        float encodingTimeMin = encodingTimeMillis/(60*1000F);
        System.out.println("Time: " + encodingTimeSec + " Sekunden / " + encodingTimeMin + " Minuten");

    }

    /**
     * Reads binary file from path and reconstructs .jpg image from that.
     * Notice that the first 30 Bit contain information about width, length and blocksize of the image
     */
    public static void decoder() {
        System.out.println("----------Decoder----------");
        long start = System.currentTimeMillis();

        try {
            String binary = Files.readString(Paths.get("binary.txt"), StandardCharsets.UTF_8);

            System.out.println("Successfully read data from the file binary.txt");

            int width = BinToDec(binary.substring(0, 12));
            int height = BinToDec(binary.substring(12, 24));
            int factor = BinToDec(binary.substring(24, 36));

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            binary = binary.substring(36);

            for (int j = 0; j < height - (factor-1); j+=factor) {
                for (int i = 0; i < width - (factor-1); i +=factor) {
                    int index = BIT*((((j/factor)+1)*(width/factor))+(i/factor)-(width/factor));  //contains beginning position of the current n-Bit color block
                    Color color = lookupCodebookVector(BinToDec(binary.substring(index, index+BIT)));     //contains current color as Integer
                    for (int y = j; y < j+factor; y++){
                        for (int x = i; x < i+factor; x++){
                            img.setRGB(x, y, color.getRGB());
                            //System.out.println(color);
                        }
                    }
                }
            }
            System.out.println("Image format: " + width + "x" + height + ", Block size: " + factor);
            System.out.println("Block format: " + (width/factor) + "x" + (height/factor) + ", Border right: " + (width%factor) + ", Border bottom: " + (height%factor));

            //write image
            File f = new File("out.jpg");
            ImageIO.write(img, "jpg", f);
        }
        catch (IOException e){
            System.out.println("Could not read from file. " + e);
        }

        //get Decoder runtime
        long decodingTimeMillis = System.currentTimeMillis()-start;
        float decodingTimeSec = decodingTimeMillis/1000F;
        float decodingTimeMin = decodingTimeMillis/(60*1000F);
        System.out.println("Time: " + decodingTimeSec + " Sekunden / " + decodingTimeMin + " Minuten");
    }

    /**
     * Stores String in .txt file
     * @param text String text to store
     * @param filename storage file name
     */
    public static void store(String text, String filename) {
        Path path = Paths.get(filename + ".txt");

        try {
            Files.writeString(path, text);
            System.out.println("Successfully written data to the file " + filename + ".txt");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates some means of compression performance to evaluate the performance of the VQ
     */
    static void analyze(){
        System.out.println("----------Analyzer----------");
        BufferedImage in = null;
        BufferedImage out = null;
        int width=0;
        int height=0;

        //read image
        try {
            File f = new File("in.jpg");
            in = ImageIO.read(f);
             f = new File("out.jpg");
            out = ImageIO.read(f);
            width = in.getWidth();
            height = in.getHeight();
        }
        catch(IOException e) {
            System.out.println(e);
        }

        int maxError = 0;
        double totalDistortion = 0;
        double meanSquaredError = 0;
        double quadraticOutput = 0;
        for (int y=0; y < height; y++){
            for (int x = 0; x < width; x++){
                Color a = new Color(in.getRGB(x,y));
                Color b = new Color(out.getRGB(x,y));
                int tmp = (Math.abs(a.getRed()-b.getRed()) + Math.abs(a.getGreen()-b.getGreen()) + Math.abs(a.getBlue()-b.getBlue())); //difference between a and b
                quadraticOutput += Math.pow((b.getRed()+ b.getGreen() + b.getBlue()), 2);
                if (tmp > maxError)
                    maxError = tmp;
                totalDistortion += tmp;
                meanSquaredError += Math.pow(tmp, 2);
            }
        }

        System.out.println("Total Distortion: " + totalDistortion);
        System.out.println("Average Distortion per Pixel: " + totalDistortion/(width*height));
        System.out.println("Mean squared error: " + meanSquaredError/(width*height));
        System.out.println("Signal-to-noise ratio: " + (quadraticOutput/meanSquaredError));
        System.out.println("Maximum error: " + maxError);


    }

    public static void main(String[] args) throws IOException {
        createCodebook();
        encoder(4);
        decoder();
        analyze();
        //dialogue();
    }

    static void dialogue() throws IOException {
        System.out.println("########## Image vector quantizer ##########");

        int factor = 1;
        String action = "";

        System.out.println("Please specify your desired action. (E)ncode/(D)ecode/(A)nalyze/()Default");
        try {
            BufferedReader stringReader = new BufferedReader(new InputStreamReader(System.in));
            action = stringReader.readLine();
            switch (action){
                case "E":{
                    System.out.println("Please enter the block size to be used. Default value is " + factor + ".");
                    try {
                        BufferedReader intReader = new BufferedReader(new InputStreamReader(System.in));
                        factor = Integer.parseInt(intReader.readLine());
                        encoder(factor);
                    } catch (NumberFormatException nfe){
                        System.out.println("Proceeding with default input of " + factor + ".");
                    }
                }
                case "D":
                    decoder();
                case "A":
                    analyze();
                case "":{
                    System.out.println("Please enter the block size to be used. Default value is " + factor + ".");
                    try {
                        BufferedReader intReader = new BufferedReader(new InputStreamReader(System.in));
                        factor = Integer.parseInt(intReader.readLine());
                        encoder(factor);
                    } catch (NumberFormatException nfe){
                        System.out.println("Proceeding with default input of " + factor + ".");
                    }
                    encoder(factor);
                    decoder();
                    analyze();
                }
            }
        } catch (NumberFormatException nfe){
            System.out.println("Proceeding with default input of " + action + ".");
        }

    }
}
