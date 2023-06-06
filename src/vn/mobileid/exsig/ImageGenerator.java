/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.exsig;

import com.itextpdf.text.pdf.BaseFont;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import vn.mobileid.font.FontPath;

/**
 *
 * @author Minhgalc
 */
public class ImageGenerator {
    
    public static BaseFont basefont;
    public static float fontSize;
    public float lineSpacing;
    public TextAlignment alignment;
    public vn.mobileid.exsig.Color textColor;
    
    public static byte[] createBackground(int width, int height, Color color) throws IOException, FontFormatException {
        BufferedImage image;
        if (color == null) {
            image = CreateBackground(width, height);
        } else {
            image = CreateColorBackground(width, height, color);
        }
        byte[] bArray;
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            baos.flush();
            bArray = baos.toByteArray();
        }
        return bArray;
    }
    
    public static byte[] createBorder(int width, int height, Color color) throws IOException, FontFormatException {
        
        BufferedImage border = CreateBackground(width, height);
        if (color != null) {
            Graphics2D graphics = (Graphics2D) border.getGraphics();
            graphics.setStroke(new BasicStroke(1));
            graphics.setColor(color);
            graphics.drawRect(0, 0, border.getWidth() - 1, border.getHeight() - 1);
        }
        byte[] bArray;
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(border, "png", baos);
            baos.flush();
            bArray = baos.toByteArray();
        }
        return bArray;
    }        
    
    
    public static byte[] Border(int width, int height) throws IOException, FontFormatException {
        
        BufferedImage border = CreateBackground(width + 4, height);
        Graphics2D graphics = (Graphics2D) border.getGraphics();
        graphics.setStroke(new BasicStroke(2));
        graphics.setColor(Color.RED);
        graphics.drawRect(1, 1, border.getWidth() - 2, border.getHeight() - 2);
        
        byte[] bArray;
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(border, "png", baos);
            baos.flush();
            bArray = baos.toByteArray();
        }
        return bArray;
    }
    
    private static BufferedImage CreateBackground(int width, int height) throws IOException, FontFormatException {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, width, height);
        graphics.setComposite(AlphaComposite.Src);
        return bufferedImage;
    }
    
    private static BufferedImage CreateColorBackground(int width, int height, Color color) throws IOException, FontFormatException {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
        return bufferedImage;
    }
    
    public static byte[] remoteSign(String header, String titleFont, String sigFont, String name, String SN) throws IOException, FontFormatException {
        int height = 300;
        int width = (int) (height * 2.5);
        
        name = name.toLowerCase();
        String[] words = name.split(" ");
        name = "";
        for (String word : words) {
            name = name + word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
        }
        
        String temp = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        pattern.matcher(temp).replaceAll("");
        
        Color color = new Color(0, 0, 0);
        BufferedImage border = CreateBackground(width, height);
        Graphics2D graphics = (Graphics2D) border.getGraphics();
        graphics.setStroke(new BasicStroke((float)height / 30));
        graphics.setColor(color);
        int boxCoordinate = (height - height * 4 / 5) / 2;
        int h1 = height - 2 * boxCoordinate;
        int w1 = width - 2 * boxCoordinate;
//        graphics.drawRoundRect(boxCoordinate, boxCoordinate, w1, h1, height/2, height/2);
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(5 * boxCoordinate, 0, w1, height);
        graphics.setComposite(AlphaComposite.Src);
        
        Font title = Font.createFont(
                Font.TRUETYPE_FONT,
                new FontPath()
                        .getClass()
                        .getResourceAsStream(titleFont))
                .deriveFont((float) ((double)height / 12));
        
        graphics.setPaint(color);
        graphics.setFont(title);
        graphics.drawString(
                header,
                5 * boxCoordinate + 4,
                boxCoordinate + height / 24 - height / 120);
        graphics.setFont(title);
        graphics.drawString(
                SN,
                5 * boxCoordinate + 4,
                boxCoordinate + h1 + height / 24 - height / 120);
        
        int lengthInPixel = graphics.getFontMetrics().stringWidth(name);
        float fontSize = 83;
        int fixSpace;
        if (lengthInPixel > 150) {
            fontSize = 150 * fontSize / lengthInPixel;
            fixSpace = 3 * boxCoordinate;
        } else {
            fixSpace = width / 2 - (int) (lengthInPixel * 2.3);
        }
        
        Font sig = Font.createFont(
                Font.TRUETYPE_FONT,
                new FontPath()
                        .getClass()
                        .getResourceAsStream(sigFont))
                .deriveFont((float) (fontSize));
        
        graphics.setFont(sig);
        
        graphics.drawString(temp, fixSpace, height / 2);
        
        byte[] bArray;
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(border, "png", baos);
            baos.flush();
            bArray = baos.toByteArray();
        }
        return bArray;
    }

    //Update 04-11-2022 by Gia
    //Combine 2 file pdf
    /**
     * This function is using to combine Image
     *
     * @param pic1 byte[] of picture1
     * @param pic2 byte[] of picture 2
     * @param nameFinalFile path File name
     */
    public static void combineImage(byte[] pic1, byte[] pic2, String nameFinalFile) {
        try {
            InputStream input, input2;
            BufferedImage bi = null;
            BufferedImage bi2 = null;
            try {
                input = new ByteArrayInputStream(pic1);
                bi = ImageIO.read(input);
                
                input2 = new ByteArrayInputStream(pic2);
                bi2 = ImageIO.read(input2);
            } catch (IOException exx) {
                exx.printStackTrace();
                Logger.getLogger(ImageGenerator.class.getName()).log(Level.SEVERE, null, exx);
                return;
            }
            int width = bi.getWidth() + bi2.getWidth();
            int height = bi2.getHeight();
            
            java.awt.Color color = new java.awt.Color(255, 255, 255);
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = bufferedImage.createGraphics();
            graphics.setColor(color);
            //Calculator        
            int y = bi2.getHeight() / 2 - bi.getHeight() / 2;
            graphics.fillRect(0, 0, width, height);
            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(0, 0, width, height);
            graphics.setComposite(AlphaComposite.Src);
            graphics.setColor(color);
            
            graphics.drawImage(bi, 0, y, null);
            graphics.drawImage(bi2, bi.getWidth(), 0, null);
            graphics.setColor(java.awt.Color.WHITE);
            ImageIO.write(bufferedImage, "png", new File(nameFinalFile));
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(ImageGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static byte[] remoteSignWithPathFont(String header, String titleFont, String sigFont, String name, String SN) throws IOException, FontFormatException {
        int height = 300;
        int width = (int) (height * 2.5);
        
        name = name.toLowerCase();
        String[] words = name.split(" ");
        name = "";
        for (String word : words) {
            name = name + word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
        }
        
        String temp = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        pattern.matcher(temp).replaceAll("");
        
        Color color = new Color(0, 0, 0);
        BufferedImage border = CreateBackground(width, height);
        Graphics2D graphics = (Graphics2D) border.getGraphics();
        graphics.setStroke(new BasicStroke((float)height / 30));
        graphics.setColor(color);
        int boxCoordinate = (height - height * 4 / 5) / 2;        
        int h1 = height - 2 * boxCoordinate;
        int w1 = width - 2 * boxCoordinate;

//        graphics.drawRoundRect(boxCoordinate, boxCoordinate, w1, h1, height/2, height/2);
        graphics.setComposite(AlphaComposite.Clear);
//        graphics.fillRect(5 * boxCoordinate, 0, w1, height);
        graphics.fillRect(0, 0, width, height);
//        graphics.fillRect(0, 0, w1, height);
        graphics.setComposite(AlphaComposite.Src);
        
        Font title = Font.createFont(
                Font.TRUETYPE_FONT,
                new File(titleFont))
                .deriveFont((float) ((double)height / 12));
        
        graphics.setPaint(color);
        graphics.setFont(title);
        graphics.drawString(
                header,
                5 * boxCoordinate + 4,
                boxCoordinate + height / 24 - height / 120);
        graphics.setFont(title);
        graphics.drawString(
                SN,
                5 * boxCoordinate + 4,
                boxCoordinate + h1 + height / 24 - height / 120);
        
        int fontSize = 150;
        
        while (true) {
                   Font sig = Font.createFont(
                Font.TRUETYPE_FONT,
                    new File(sigFont))
                    .deriveFont((float) (fontSize));
                   graphics.setFont(sig);
                   FontMetrics metrics = graphics.getFontMetrics(graphics.getFont());
                   if(metrics.stringWidth(name) < width)
                   {
                       break;
                   }
                   fontSize -=10;
        }
        
        
//        int lengthInPixel = graphics.getFontMetrics().stringWidth(name);        
        
//        float fontSize = 150;
//        int fixSpace;
//        if (lengthInPixel > 150) {
//            fontSize = 50 * fontSize / lengthInPixel;
//            fixSpace = 3 * boxCoordinate;
//        } else {
//            fixSpace = width / 2 - (int) (lengthInPixel * 2.3);
//        }
        
        Font sig = Font.createFont(
                Font.TRUETYPE_FONT,
                new File(sigFont))
                .deriveFont((float) (fontSize));

        graphics.setFont(sig);

//        graphics.drawString(temp, fixSpace, height / 2);
        graphics.drawString(temp, 0, (float)height / 2);
        
        byte[] bArray;
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(border, "png", baos);
            baos.flush();
            bArray = baos.toByteArray();
        }
        return bArray;
    }
    
    public static byte[] remoteSignWithPathFont(String header, InputStream titleFont, InputStream sigFont, String name, String SN) throws IOException, FontFormatException {
        int height = 300;
        int width = (int) (height * 2.5);
        
        name = name.toLowerCase();
        String[] words = name.split(" ");
        name = "";
        for (String word : words) {
            name = name + word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
        }
        
        String temp = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        pattern.matcher(temp).replaceAll("");
        
        Color color = new Color(0, 0, 0);
        BufferedImage border = CreateBackground(width, height);
        Graphics2D graphics = (Graphics2D) border.getGraphics();
        graphics.setStroke(new BasicStroke((float)height / 30));
        graphics.setColor(color);
        int boxCoordinate = (height - height * 4 / 5) / 2;
        int h1 = height - 2 * boxCoordinate;
        int w1 = width - 2 * boxCoordinate;
//        graphics.drawRoundRect(boxCoordinate, boxCoordinate, w1, h1, height/2, height/2);
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(5 * boxCoordinate, 0, w1, height);
        graphics.setComposite(AlphaComposite.Src);
        
        Font title = Font.createFont(
                Font.TRUETYPE_FONT,
                titleFont)
                .deriveFont((float) ((double)height / 12));
        
        graphics.setPaint(color);
        graphics.setFont(title);
        graphics.drawString(
                header,
                5 * boxCoordinate + 4,
                boxCoordinate + height / 24 - height / 120);
        graphics.setFont(title);
        graphics.drawString(
                SN,
                5 * boxCoordinate + 4,
                boxCoordinate + h1 + height / 24 - height / 120);
        
        int lengthInPixel = graphics.getFontMetrics().stringWidth(name);
        float fontSize = 150;
        int fixSpace;
        if (lengthInPixel > 150) {
            fontSize = 150 * fontSize / lengthInPixel;
            fixSpace = 3 * boxCoordinate;
        } else {
            fixSpace = width / 2 - (int) (lengthInPixel * 2.3);
        }
        
        Font sig = Font.createFont(
                Font.TRUETYPE_FONT,
                sigFont)
                .deriveFont((float) (fontSize));
        
        graphics.setFont(sig);

//        graphics.drawString(temp, fixSpace, height / 2);
        graphics.drawString(temp, 0, (float)height / 2);
        
        byte[] bArray;
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(border, "png", baos);
            baos.flush();
            bArray = baos.toByteArray();
        }
        return bArray;
    }
    
    public static byte[] remoteSignWithPathFont_UsingClassLoader(String header, String titleFont, String sigFont, String name, String SN) throws IOException, FontFormatException {
        int height = 300;
        int width = (int) (height * 2.5);
        
        name = name.toLowerCase();
        String[] words = name.split(" ");
        name = "";
        for (String word : words) {
            name = name + word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
        }
        
        String temp = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        pattern.matcher(temp).replaceAll("");
        
        Color color = new Color(0, 0, 0);
        BufferedImage border = CreateBackground(width, height);
        Graphics2D graphics = (Graphics2D) border.getGraphics();
        graphics.setStroke(new BasicStroke((float)height / 30));
        graphics.setColor(color);
        int boxCoordinate = (height - height * 4 / 5) / 2;
        int h1 = height - 2 * boxCoordinate;
        int w1 = width - 2 * boxCoordinate;
//        graphics.drawRoundRect(boxCoordinate, boxCoordinate, w1, h1, height/2, height/2);
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(5 * boxCoordinate, 0, w1, height);
        graphics.setComposite(AlphaComposite.Src);
        
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream input = loader.getResourceAsStream(titleFont);
        Font title = Font.createFont(
                Font.TRUETYPE_FONT,
                input
        )
                .deriveFont((float) ((double)height / 12));
        
        graphics.setPaint(color);
        graphics.setFont(title);
        graphics.drawString(
                header,
                5 * boxCoordinate + 4,
                boxCoordinate + height / 24 - height / 120);
        graphics.setFont(title);
        graphics.drawString(
                SN,
                5 * boxCoordinate + 4,
                boxCoordinate + h1 + height / 24 - height / 120);            

        input = loader.getResourceAsStream(sigFont);
        
        int fontSize = 150;
        
        while (true) {
                   Font sig = Font.createFont(
                Font.TRUETYPE_FONT,
                    loader.getResourceAsStream(sigFont))
                    .deriveFont((float) (fontSize));
                   graphics.setFont(sig);
                   FontMetrics metrics = graphics.getFontMetrics(graphics.getFont());
                   if(metrics.stringWidth(name) < width)
                   {
                       break;
                   }
                   fontSize -=10;
       }                
//        int lengthInPixel = graphics.getFontMetrics().stringWidth(name);                
//        float fontSize = 150;
//        int fixSpace;
//        if (lengthInPixel > 150) {
//            fontSize = 50 * fontSize / lengthInPixel;
//            fixSpace = 3 * boxCoordinate;
//        } else {
//            fixSpace = width / 2 - (int) (lengthInPixel * 2.3);
//        }
        
        Font sig = Font.createFont(
                Font.TRUETYPE_FONT,
                input)
                .deriveFont((float) (fontSize));

        graphics.setFont(sig);

//        graphics.drawString(temp, fixSpace, height / 2);
        graphics.drawString(temp, 0,(float) height / 2);
        
        byte[] bArray;
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(border, "png", baos);
            baos.flush();
            bArray = baos.toByteArray();
        }
        return bArray;
    }

    /**
     * Combine Image base on 2 path of picture
     *
     * @param pic1 : a path of picture 1
     * @param pic2 : a path of picture 2
     * @param nameFinalFile : name final file
     */
    public static void combineImage(String pic1, String pic2, String nameFinalFile) {
        try {
//            InputStream input, input2;
            BufferedImage bi = null;
            BufferedImage bi2 = null;
            try {
//                input = new ByteArrayInputStream(pic1);
                bi = ImageIO.read(new File(pic1));
//                input2 = new ByteArrayInputStream(pic2);
                bi2 = ImageIO.read(new File(pic2));
            } catch (IOException exx) {
                exx.printStackTrace();
                Logger.getLogger(ImageGenerator.class.getName()).log(Level.SEVERE, null, exx);
                return;
            }
            int width = bi.getWidth() + bi2.getWidth();
            int height = bi2.getHeight();
            
            java.awt.Color color = new java.awt.Color(255, 255, 255);
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = bufferedImage.createGraphics();
            graphics.setColor(color);
            //Calculator        
            int y = bi2.getHeight() / 2 - bi.getHeight() / 2;
            graphics.fillRect(0, 0, width, height);
            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(0, 0, width, height);
            graphics.setComposite(AlphaComposite.Src);
            graphics.setColor(color);
            
            graphics.drawImage(bi, 0, y, null);
            graphics.drawImage(bi2, bi.getWidth(), 0, null);
            graphics.setColor(java.awt.Color.WHITE);
            ImageIO.write(bufferedImage, "png", new File(nameFinalFile));
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(ImageGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Update 04-11-2022 by Gia
    //Combine 2 file pdf
    /**
     * This function is using to combine Image
     *
     * @param pic1 byte[] of picture1
     * @param pic2 byte[] of picture 2
     * @param nameFinalFile path File name
     */
    public static byte[] combineImage(byte[] pic1, byte[] pic2) throws Exception {
        try {
            InputStream input, input2;
            BufferedImage bi = null;
            BufferedImage bi2 = null;
            try {
                input = new ByteArrayInputStream(pic1);
                bi = ImageIO.read(input);
                
                input2 = new ByteArrayInputStream(pic2);
                bi2 = ImageIO.read(input2);
            } catch (IOException exx) {
                throw new Exception(exx);
            }
            int width = bi.getWidth() + bi2.getWidth();
            int height = bi2.getHeight();
            
            java.awt.Color color = new java.awt.Color(255, 255, 255);
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = bufferedImage.createGraphics();
            graphics.setColor(color);
            //Calculator        
            int y = bi2.getHeight() / 2 - bi.getHeight() / 2;
            graphics.fillRect(0, 0, width, height);
            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(0, 0, width, height);
            graphics.setComposite(AlphaComposite.Src);
            graphics.setColor(color);
            
            graphics.drawImage(bi, 0, y, null);
            graphics.drawImage(bi2, bi.getWidth(), 0, null);
            graphics.setColor(java.awt.Color.WHITE);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new Exception(ex);
        }
    }
}
