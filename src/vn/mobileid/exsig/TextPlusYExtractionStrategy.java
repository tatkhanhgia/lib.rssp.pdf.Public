/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.exsig;

import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy.TextChunkLocationStrategy;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.Vector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

;

/**
 *
 * @author VUDP
 */
public class TextPlusYExtractionStrategy extends LocationTextExtractionStrategy {

    private static Field locationalResultField;
    private static Method startsWithSpaceMethod;
    private static Method endsWithSpaceMethod;
    private static Method textChunkSameLineMethod;

    static {
        try {
            locationalResultField = LocationTextExtractionStrategy.class.getDeclaredField("locationalResult");
            locationalResultField.setAccessible(true);
            startsWithSpaceMethod = LocationTextExtractionStrategy.class.getDeclaredMethod("startsWithSpace", String.class);
            startsWithSpaceMethod.setAccessible(true);
            endsWithSpaceMethod = LocationTextExtractionStrategy.class.getDeclaredMethod("endsWithSpace", String.class);
            endsWithSpaceMethod.setAccessible(true);
            textChunkSameLineMethod = TextChunk.class.getDeclaredMethod("sameLine", TextChunk.class);
            textChunkSameLineMethod.setAccessible(true);
        } catch (SecurityException e) {
            // Reflection failed
        } catch (NoSuchMethodException | NoSuchFieldException ex) {
            Logger.getLogger(TextPlusYExtractionStrategy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public TextPlusYExtractionStrategy() {
        super();
    }

    public TextPlusYExtractionStrategy(TextChunkLocationStrategy strat) {
        super(strat);
    }

    @Override
    public String getResultantText() {
        return getResultantTextPlusY().toString();
    }

    public TextPlusY getResultantTextPlusY() {
        try {
            //PrintWriter pw = new PrintWriter("D:\\MOBILE-ID\\H_Drive\\Tomica\\FIS OBC\\From_ThaoHM\\FIS_OBC\\file\\pdf\\log.txt", "UTF-8");
            List<TextChunk> textChunks = new ArrayList<>((List<TextChunk>) locationalResultField.get(this));
            Collections.sort(textChunks);

            TextPlusY textPlusY = new TextPlusY();
            TextChunk lastChunk = null;
            for (TextChunk chunk : textChunks) {
                float chunkY = chunk.getLocation().getStartLocation().get(Vector.I2);
                float chunkX = chunk.getLocation().getStartLocation().get(Vector.I1);
                //pw.print(chunk.getText() + " ");
                if (lastChunk == null) {
                    textPlusY.add(chunk.getText(), chunkY, chunkX);
                } else if ((Boolean) textChunkSameLineMethod.invoke(chunk, lastChunk)) {
                    // we only insert a blank space if the trailing character of the previous string wasn't a space, and the leading character of the current string isn't a space
                    if (isChunkAtWordBoundary(chunk, lastChunk)
                            && !(Boolean) startsWithSpaceMethod.invoke(this, chunk.getText())
                            && !(Boolean) endsWithSpaceMethod.invoke(this, lastChunk.getText())) {
                        textPlusY.add(" ", chunkY, chunkX);
                    }

                    textPlusY.add(chunk.getText(), chunkY, chunkX);
                } else {
                    textPlusY.add("\n", 
                            lastChunk.getLocation().getStartLocation().get(Vector.I2),
                            lastChunk.getLocation().getStartLocation().get(Vector.I1));
                    textPlusY.add(chunk.getText(), chunkY, chunkX);
                }
                lastChunk = chunk;
                //pw.println(chunk.getText());
                //System.out.println(chunk.getText());
            }
            //pw.close();
            return textPlusY;
        } catch (Exception e) {
            throw new RuntimeException("Reflection failed", e);
        }
    }
}
