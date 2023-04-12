///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package vn.mobileid.exsig;
//
//import java.awt.AlphaComposite;
//import java.awt.BasicStroke;
//import java.awt.Font;
//import java.awt.FontFormatException;
//import java.awt.Graphics2D;
//
//import java.awt.image.BufferedImage;
//import java.io.BufferedInputStream;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.file.Files;
//import java.text.Normalizer;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.regex.Pattern;
//import javax.imageio.ImageIO;
//import sun.nio.ch.IOUtil;
//
///**
// *
// * @author Admin
// */
//public class TestConvertTextToImage {
//
//    public static void main(String[] args) throws IOException, FontFormatException {
//
//        String text = "this is a long long long very long text";
//        byte[] picture = ImageGenerator.remoteSignWithPathFont("Header", "C:\\Users\\Admin\\Downloads\\verdana-font-family\\verdana-font-family\\verdana.ttf", "C:\\Users\\Admin\\Downloads\\verdana-font-family\\verdana-font-family\\verdana.ttf", text, "SerialNumber");
////
////        byte[] picture2 = Files.readAllBytes(new File("C:\\Users\\Admin\\Downloads\\decoded-20230214071407.jpeg").toPath());
////        byte[] picture3 = Files.readAllBytes(new File("file\\Blue300x400.png").toPath());
//////        byte[] picture = Files.readAllBytes(new File("file/YellowSignature.png").toPath());
//        writeToFile(picture, new File("C:\\Users\\Admin\\Downloads\\hello.png"));
////        ImageGenerator.combineImage(picture, picture2, "C:\\Users\\Admin\\Downloads\\CombineImage110.png");
//    
//       
//    
//    }
//
//    public static void writeToFile(byte[] bytes, File output) {
//        try {
//            OutputStream os = new FileOutputStream(output);
//            os.write(bytes);
//            os.close();
//            System.out.println("Successfully");
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(TestConvertTextToImage.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(TestConvertTextToImage.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//    }
//
//}
