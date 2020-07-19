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

    static int intervalSize = 16;
    static int BIT = (int)(Math.log(Math.pow(256.0/intervalSize, 3))/Math.log(2.0));
    static int intervals = (256/intervalSize);

    static Color[] codebook = new Color[(int)Math.pow(intervals, 3)];
    //String path = "C:\\Users\\";

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
        Encoder.createCodebook();
        Encoder.encoder(1);
        Decoder.decoder();
        analyze();
        //dialogue();
    }
}
