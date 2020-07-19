package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Encoder {

    /**
     * Creates a codebook of [intervals]^3 colors, where there are i values for R, G, B each
     */
    public static void createCodebook(){
        int i=0;
        for (int z = 0; z < Main.intervals; z++){
            for (int y = 0; y < Main.intervals; y++){
                for (int x = 0; x < Main.intervals; x++){
                    Main.codebook[i] = new Color(x*Main.intervalSize, y*Main.intervalSize, z*Main.intervalSize);
                    //System.out.println(i +" "+ x +" "+ y +" "+ z);
                    //System.out.println(codebook[i]);
                    i++;
                }
            }
        }
        System.out.println("Interval size: " + Main.intervalSize + "; Intervals: " + Main.intervals + "; Bits: " + Main.BIT + "; Codebook length " + Main.codebook.length);
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
        return ((r-(r%Main.intervalSize))/Main.intervalSize) + Main.intervalSize*((g-(g%Main.intervalSize))/Main.intervalSize) + (int)Math.pow(Main.intervalSize, 2)*((b-(b%Main.intervalSize))/Main.intervalSize);
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

        String codeVectors = Main.DecToBin(width,12) + Main.DecToBin(height,12) + Main.DecToBin(factor,12);

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
                codeVectors = codeVectors.concat(Objects.requireNonNull(Main.DecToBin(vector, Main.BIT)));
                counter++;
            }
            System.out.println("Block: " + counter + "/" + ((width/factor)*(height/factor)) + "\t " + ((100*counter)/((width/factor)*(height/factor))) + "%");
        }

        Main.store(codeVectors, "binary");

        //Get Encoder runtime
        long encodingTimeMillis = System.currentTimeMillis()-start;
        float encodingTimeSec = encodingTimeMillis/1000F;
        float encodingTimeMin = encodingTimeMillis/(60*1000F);
        System.out.println("Time: " + encodingTimeSec + " Sekunden / " + encodingTimeMin + " Minuten");

    }
}
