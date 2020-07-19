package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Decoder {

    /**
     * Looks up a decimal number in the codebook and returns it
     * @param index index of the codebook vector
     * @return color of the vector (RGB color can be viewed as 3-dimensional vector)
     */
    public static Color lookupCodebookVector(int index){
        return Main.codebook[index];
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

            int width = Main.BinToDec(binary.substring(0, 12));
            int height = Main.BinToDec(binary.substring(12, 24));
            int factor = Main.BinToDec(binary.substring(24, 36));

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            binary = binary.substring(36);

            for (int j = 0; j < height - (factor-1); j+=factor) {
                for (int i = 0; i < width - (factor-1); i +=factor) {
                    int index = Main.BIT*((((j/factor)+1)*(width/factor))+(i/factor)-(width/factor));  //contains beginning position of the current n-Bit color block
                    Color color = lookupCodebookVector(Main.BinToDec(binary.substring(index, index+Main.BIT)));     //contains current color as Integer
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
}
