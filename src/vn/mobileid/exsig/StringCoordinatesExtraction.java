/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.exsig;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author VUDP
 */
public class StringCoordinatesExtraction {

    private final PdfReader reader;

    public StringCoordinatesExtraction(PdfReader reader) {
        this.reader = reader;
    }

    //update 03/08/2021
    public List<TextPlusXY> getCoordinate(String stringIdentifier) {
        List<TextPlusXY> textPlusXYList = new ArrayList<>();
        try {
            int numberOfPage = reader.getNumberOfPages();
            for (int currentPage = 1; currentPage <= numberOfPage; currentPage++) {
                TextPlusXY textPlusXY = null;
                PdfReaderContentParser parser = new PdfReaderContentParser(reader);
                TextPlusYExtractionStrategy extractionStrategy = new TextPlusYExtractionStrategy();
                parser.processContent(currentPage, extractionStrategy);
                TextPlusY textPlusY = extractionStrategy.getResultantTextPlusY();

                Matcher matcher = Pattern.compile(stringIdentifier).matcher(textPlusY);
                while (matcher.find()) {
                    int start = matcher.start();
                    textPlusXY = new TextPlusXY();
                    textPlusXY.setY(textPlusY.yCoordAt(start));
                    textPlusXY.setX(textPlusY.getxCoord());
                    textPlusXY.setPage(currentPage);
                    //update 03/08/2021
                    //break;
                }

                if (textPlusXY == null) {
                    TextCoordinatesExtraction textCoordinatesExtraction = new TextCoordinatesExtraction(reader);
                    TextCoordinatesExtraction.TextLocation textLocation = textCoordinatesExtraction.getCoordinate(stringIdentifier, currentPage);
                    if (textLocation != null) {
                        textPlusXY = new TextPlusXY();
                        textPlusXY.setY(textLocation.Y);
                        textPlusXY.setX(textLocation.X);
                        textPlusXY.setPage(currentPage);
                        textPlusXYList.add(textPlusXY);
                    }
                }
                else{
                    textPlusXYList.add(textPlusXY);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return textPlusXYList;
    }

    public TextPlusXY getCoordinate(final String stringIdentifier, final int pageNoInfo) {

        TextPlusXY textPlusXY = null;
        try {
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            TextPlusYExtractionStrategy extractionStrategy = new TextPlusYExtractionStrategy();
            parser.processContent(pageNoInfo, extractionStrategy);
            TextPlusY textPlusY = extractionStrategy.getResultantTextPlusY();
            //System.out.println(textPlusY);
            Matcher matcher = Pattern.compile(stringIdentifier).matcher(textPlusY);
            while (matcher.find()) {
                int start = matcher.start();
                textPlusXY = new TextPlusXY();
                textPlusXY.setY(textPlusY.yCoordAt(start));
                textPlusXY.setX(textPlusY.getxCoord());
                //break; // no beak here to get the last match
            }
            
            if (textPlusXY == null) {
                TextCoordinatesExtraction textCoordinatesExtraction = new TextCoordinatesExtraction(reader);
                TextCoordinatesExtraction.TextLocation textLocation = textCoordinatesExtraction.getCoordinate(stringIdentifier, pageNoInfo);
                if (textLocation != null) {
                    textPlusXY = new TextPlusXY();
                    textPlusXY.setY(textLocation.Y);
                    textPlusXY.setX(textLocation.X);
                    textPlusXY.setPage(pageNoInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return textPlusXY;
    }

    public class TextCoordinatesExtraction {

        private final PdfReader reader;

        public TextCoordinatesExtraction(PdfReader reader) {
            this.reader = reader;
        }

        public TextLocation getCoordinate(String stringIdentifier, int pageNoInfo) throws IOException {
            MyLocationTextExtractionStrategy t = new MyLocationTextExtractionStrategy();
            PdfTextExtractor.getTextFromPage(reader, pageNoInfo, t);

            List<TextLocation> textLocations = t.myPoints;

            String trimStringIdentifierLv1 = stringIdentifier.replace(" ", "").replace("\n", "").replace("\t", "");
            //String trimStringIdentifierLv1 = stringIdentifier;
            String trimStringIdentifierLv2 = trimStringIdentifierLv1.toLowerCase();
            String trimStringIdentifierLv3 = unAccent(trimStringIdentifierLv1);
            String trimStringIdentifierLv4 = unAccent(trimStringIdentifierLv2);
            
            String content = "";
            int positionCount = 0;
            Boolean founded = false;

            List<FoundedPoint> foundedPoints = new ArrayList<>();

            for (TextLocation tl : textLocations) {

                content = content + tl.Text;

                positionCount++;

                String trimContentLv1 = content.replace(" ", "").replace("\n", "").replace("\t", "");
                if (trimContentLv1.contains(trimStringIdentifierLv1)) {
                    foundedPoints.add(new FoundedPoint(positionCount, Level.ONE));
                    content = "";
                    continue;
                }
                
                String trimContentLv2 = trimContentLv1.toLowerCase();
                if (trimContentLv2.contains(trimStringIdentifierLv2)) {
                    foundedPoints.add(new FoundedPoint(positionCount, Level.TWO));
                    content = "";
                    continue;
                }
                
                String trimContentLv3 = unAccent(trimContentLv1);
                if (trimContentLv3.contains(trimStringIdentifierLv3)) {
                    foundedPoints.add(new FoundedPoint(positionCount, Level.THREE));
                    content = "";
                    continue;
                }
                
                String trimContentLv4 = unAccent(trimContentLv2);
                if (trimContentLv4.contains(trimStringIdentifierLv4)) {
                    foundedPoints.add(new FoundedPoint(positionCount, Level.FOUR));
                    content = "";
                }
            }

            if (foundedPoints.size() == 0) {
                return null;
            }

            content = "";
            
            for (int i = foundedPoints.get(foundedPoints.size() - 1).point - 1; i >= 0; i--) {
                String temp = textLocations.get(i).Text;
                content = temp + content;
                String trimContentLv1 = content.replace(" ", "").replace("\n", "").replace("\t", "");

                if (Level.ONE == foundedPoints.get(foundedPoints.size() - 1).level) {
                    if (trimContentLv1.contains(trimStringIdentifierLv1)) {
                        positionCount = i;
                        founded = true;
//                    System.out.println("DETECT #" + content + "# position :" + i + " X:" + textLocations.get(i).X + " Y:" + textLocations.get(i).Y);
                        break;
                    }
                }

                String trimContentLv2 = trimContentLv1.toLowerCase();

                if (Level.TWO == foundedPoints.get(foundedPoints.size() - 1).level) {
                    if (trimContentLv2.contains(trimStringIdentifierLv2)) {
                        positionCount = i;
                        founded = true;
//                    System.out.println("DETECT #" + content + "# position :" + i + " X:" + textLocations.get(i).X + " Y:" + textLocations.get(i).Y);
                        break;
                    }
                }
                String trimContentLv3 = unAccent(trimContentLv1);

                if (Level.THREE == foundedPoints.get(foundedPoints.size() - 1).level) {
                    if (trimContentLv3.contains(trimStringIdentifierLv3)) {
                        positionCount = i;
                        founded = true;
//                    System.out.println("DETECT #" + content + "# position :" + i + " X:" + textLocations.get(i).X + " Y:" + textLocations.get(i).Y);
                        break;
                    }
                }

                String trimContentLv4 = unAccent(trimContentLv2);
                if (Level.FOUR == foundedPoints.get(foundedPoints.size() - 1).level) {
                    if (trimContentLv4.contains(trimStringIdentifierLv4)) {
                        positionCount = i;
                        founded = true;
//                    System.out.println("DETECT #" + content + "# position :" + i + " X:" + textLocations.get(i).X + " Y:" + textLocations.get(i).Y);
                        break;
                    }
                }

            }

            if (founded) {
                return textLocations.get(positionCount);
            } else {
                return null;
            }
        }

        public class MyLocationTextExtractionStrategy extends LocationTextExtractionStrategy {

            //Hold each coordinate
            private List<TextLocation> myPoints = new ArrayList<>();

            public List<TextLocation> getMyPoints() {
                return myPoints;
            }
            
            @Override
            public void renderText(TextRenderInfo renderInfo) {
                super.renderText(renderInfo);

                //Get the bounding box for the chunk of text
                Vector bottomLeft = renderInfo.getDescentLine().getStartPoint();
                Vector topRight = renderInfo.getAscentLine().getEndPoint();

                //Create a rectangle from it
                Rectangle rect = new Rectangle(bottomLeft.get(Vector.I1), bottomLeft.get(Vector.I2), topRight.get(Vector.I1), topRight.get(Vector.I2));

                //Add this to our main collection
                this.myPoints.add(new TextLocation(rect.getLeft(), rect.getBottom(), renderInfo.getText()));
            }
        }

        class TextLocation {

            public TextLocation(float x, float y, String text) {
                X = x;
                Y = y;
                Text = text;
            }

            public float X;
            public float Y;
            public String Text;

            public String getText() {
                return Text;
            }

            public float getX() {
                return X;
            }

            public float getY() {
                return Y;
            }

        }

    }
    
    enum Level {
        ONE, TWO, THREE, FOUR;
    }

    public static String unAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
//        return pattern.matcher(temp).replaceAll("");
        return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D").replace("đ", "");
    }
    
    class FoundedPoint{

        FoundedPoint(Integer point, Level level) {
            this.point = point;
            this.level = level;
        }
        
        Integer point;
        Level level;
    }
}
