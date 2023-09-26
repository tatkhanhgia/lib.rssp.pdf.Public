///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package vn.mobileid.exsig;
//
//import static com.itextpdf.text.Annotation.TEXT;
//import com.itextpdf.text.BadElementException;
//import com.itextpdf.text.BaseColor;
//import com.itextpdf.text.Chunk;
//import com.itextpdf.text.Document;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.Element;
//import com.itextpdf.text.Font;
//import com.itextpdf.text.Image;
//import com.itextpdf.text.Paragraph;
//import com.itextpdf.text.Phrase;
//import com.itextpdf.text.Rectangle;
//import com.itextpdf.text.io.RASInputStream;
//import com.itextpdf.text.io.RandomAccessSource;
//import com.itextpdf.text.io.RandomAccessSourceFactory;
//import com.itextpdf.text.io.StreamUtil;
//import com.itextpdf.text.pdf.AcroFields;
//import com.itextpdf.text.pdf.BaseFont;
//import com.itextpdf.text.pdf.ByteBuffer;
//import com.itextpdf.text.pdf.ColumnText;
//import com.itextpdf.text.pdf.PdfArray;
//import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfDictionary;
//import com.itextpdf.text.pdf.PdfName;
//import com.itextpdf.text.pdf.PdfPCell;
//import com.itextpdf.text.pdf.PdfPCellEvent;
//import com.itextpdf.text.pdf.PdfPTable;
//import com.itextpdf.text.pdf.PdfReader;
//import com.itextpdf.text.pdf.PdfSignatureAppearanceMI;
//import com.itextpdf.text.pdf.PdfStamperMI;
//import com.itextpdf.text.pdf.PdfTemplate;
//import com.itextpdf.text.pdf.PdfWriter;
//import com.itextpdf.text.pdf.security.BouncyCastleDigest;
//import com.itextpdf.text.pdf.security.DigestAlgorithms;
//import com.itextpdf.text.pdf.security.ExternalBlankSignatureContainer;
//import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
//import com.itextpdf.text.pdf.security.MakeSignature;
//import com.itextpdf.text.pdf.security.MakeSignatureMI;
//import com.itextpdf.text.pdf.security.PdfPKCS7CMS;
//import com.itextpdf.text.pdf.security.TSAClient;
//import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
//import java.awt.FontFormatException;
//import java.awt.Graphics2D;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.nio.file.Files;
//import java.nio.file.OpenOption;
//import java.nio.file.StandardOpenOption;
//import java.security.GeneralSecurityException;
//import java.security.cert.X509Certificate;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.imageio.ImageIO;
//import javax.xml.bind.DatatypeConverter;
//import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
//import org.bouncycastle.util.encoders.Base64;
//
///**
// *
// * @author GIATK
// */
//public class PdfEsealCMS extends PdfProfile {
//
//    public static final String titleEseal = "Advanced Electronic Seal with Qualified Certificate";
//    private int fontSizeBig = 7;
//    private int fontSizeSmall = 6;
//    private int defaultTitleEseal = 4;
//    private int width = 120;
//    private int height = 50;
//    private String signerCertificate;
//
//    class CellBackground implements PdfPCellEvent {
//
//        private Font font;
//
//        public CellBackground(Font font) {
//            this.font = font;
//        }
//
//        public void cellLayout(
//                PdfPCell cell,
//                Rectangle rect,
//                PdfContentByte[] canvas) {
//
//            Image image = null;
//            try {
//                image = Image.getInstance(Files.readAllBytes(new File("C:\\Users\\Admin\\Downloads\\44_icon_Chip_50x50_Background.png").toPath()));
//
//                image.setAbsolutePosition(rect.getWidth() * 7 / 100, rect.getHeight() * 81f / 100);
//                image.setBackgroundColor(new BaseColor(239, 236, 236));
//                float height = rect.getTop() * 25 / 100;
//                image.scaleToFit(height / 2, height / 2);
//            } catch (IOException ex) {
//                Logger.getLogger(PdfEsealCMS.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (BadElementException ex) {
//                Logger.getLogger(PdfEsealCMS.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//            PdfContentByte cb = canvas[PdfPTable.BACKGROUNDCANVAS];
//
//            cb.roundRectangle(
//                    0 + 1,
//                    (rect.getTop() - rect.getBottom()) * 75 / 100 + 1,
//                    rect.getWidth() - 1.5,
//                    (rect.getTop() - rect.getBottom()) * 25 / 100 - 1.5,
//                    6);
//            cb.setColorFill(new BaseColor(239, 236, 236));
//            cb.fill();
//
//            cb.rectangle(
//                    0 + 1,
//                    (rect.getTop() - rect.getBottom()) * 75 / 100 + 1,
//                    rect.getWidth() - 1.5,
//                    (rect.getTop() - rect.getBottom()) * 25 / 100 - 5);
//            cb.setColorFill(new BaseColor(239, 236, 236));
//            cb.fill();
//
//            cb.roundRectangle(
//                    0 + 1f,
//                    0 + 1f,
//                    rect.getWidth() - 1.5f,
//                    rect.getHeight() - 1.5f,
//                    6);
//            cb.setColorStroke(BaseColor.LIGHT_GRAY);
//            cb.stroke();
//
//            try {
//                cb.addImage(image, true);
//            } catch (DocumentException ex) {
//                Logger.getLogger(PdfEsealCMS.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//            Chunk c = new Chunk(titleEseal, new Font(font.getBaseFont(), defaultTitleEseal, Font.BOLD));
//            Paragraph p = new Paragraph();            
//            p.add(c);           
//            PdfContentByte canvas2 = canvas[PdfPTable.TEXTCANVAS];
//
//            ColumnText.showTextAligned(
//                    canvas2,
//                    Element.ALIGN_LEFT,
//                    p,
//                    rect.getWidth() * 13.5f / 100,
//                    rect.getHeight() * 83.75f / 100,
//                    0);
//
//        }
//    }
//
//    public static void main(String[] arhs) throws FileNotFoundException, DocumentException, IOException, FontFormatException, GeneralSecurityException, Exception {
//        File file = new File("file/eseal.pdf");
//        file.getParentFile().mkdirs();
//        PdfEsealCMS a = new PdfEsealCMS(PdfForm.B, Algorithm.SHA256);
//        List<byte[]> list = new ArrayList<>();
//        list.add(Files.readAllBytes(new File("file/Border2.pdf").toPath()));
////        a.setEsealFont(DefaultFont.Arial, 1f, Color.YELLOW);
////        a.setEsealFontSize(15, 8);
//        a.createEseal(1, 200, 0, "DOKOBIT TEST", "2023-05-09 12:14:18 GMT+3\nPurpose: Seal");
//        a.generateHash(list);
//    }
//
//    public PdfEsealCMS(PdfForm form, Algorithm algorithm) {
//        super(form, algorithm);
//    }
//
//    private PdfPTable createEsealTable(Font font) throws FileNotFoundException, DocumentException {
//        PdfPTable table = new PdfPTable(1);
//
//        table.setSpacingAfter(0);
//        table.setSpacingBefore(0);
//        table.setWidthPercentage(100);        
//        table.setWidths(new int[]{1});
//
//        PdfPCell cellTitle = new PdfPCell();
//        cellTitle.setLeft(position.getLeft());
//        cellTitle.setRight(position.getRight());
//        cellTitle.setBottom(position.getBottom());
//        cellTitle.setTop(position.getTop());
////        cellTitle.setLeft(0);
////        cellTitle.setRight(250);
////        cellTitle.setBottom(0);
////        cellTitle.setTop(100);
//
//        cellTitle.setBorder(Rectangle.NO_BORDER);
//        cellTitle.setNoWrap(false);
//        cellTitle.setLeading(1f, 5f);
////        cellTitle.setVerticalAlignment(Element.ALIGN_MIDDLE);
////        cellTitle.setHorizontalAlignment(Element.ALIGN_LEFT);        
//        cellTitle.setPaddingTop(calculateTopPadding(position));
////        cellTitle.setPaddingTop(30);
//        cellTitle.setPaddingLeft(calculateLeftPadding(position));
//        cellTitle.setFixedHeight(position.getTop() - cellTitle.getBottom());
//        cellTitle.setCellEvent(new CellBackground(font));
//
////        Paragraph par = new Paragraph();                
//        String title = textContent.split("_")[0];
//        Phrase p1 = new Phrase(
//                        new Chunk(title, new Font(font.getBaseFont(), fontSizeBig, Font.BOLD))                        
//                );        
////        par.add(p);            
//               
//        String content = textContent.split("_")[1];
//        Phrase p2 = new Phrase(new Chunk(content, new Font(font.getBaseFont(), fontSizeSmall, Font.NORMAL)));
////        p.setLeading(0f);                
////        par.add(p);
////        par.setExtraParagraphSpace(0);
////        par.setKeepTogether(false);        
////        par.setSpacingAfter(0);
////        par.setSpacingBefore(40);
////        par.setLeading(1f, 3.5f);
//        cellTitle.addElement(p1);
//        cellTitle.addElement(p2);
//        table.addCell(cellTitle);
//
//        return table;
//    }
//
//    private float calculateTopPadding(Rectangle iRect) {
//        float top = 0;
//        top = iRect.getHeight() - (iRect.getHeight() * 75f / 100);
//        return top;
//    }
//
//    private float calculateLeftPadding(Rectangle iRect) {
//        float left = 0;
//        left = iRect.getWidth() * 10 / 100;
//        return left;
//    }
//
//    public void createEseal(
//            int page,
//            int x,
//            int y,
//            String title,
//            String content
//    ) throws Exception {
//        this.position = new Rectangle(x, y, width + x, height + y);
//        this.textContent = title + "\n_" + content;
//        if (page <= 0) {
//            throw new Exception("page is null");
//        }
//        try {
//            signingPageInt = page;
//        } catch (Exception ex) {
//            throw new Exception("Invalid page number " + page, ex);
//        }
//    }
//
//    public void setEsealFontSize(int fontSizeBig, int fontSizeSmall) {
//        this.fontSizeBig = fontSizeBig;
//        this.fontSizeSmall = fontSizeSmall;
//    }
//
//    public void setEsealFont(DefaultFont font, float lineSpacing, Color textColor) throws Exception {
//        this.setFont(font, 10, lineSpacing, TextAlignment.ALIGN_LEFT, textColor);
//    }
//
//    @Override
//    void generateHash(List<byte[]> dataToBeSign) throws Exception {
//
//        Calendar signingTime = Calendar.getInstance();
//        signingTime.setTimeInMillis(timeMillis);
//        Date date = signingTime.getTime();
//
//        signatureId = "sig-"
//                + Calendar.getInstance().getTimeInMillis();
//
//        Font font = null;
//
//        if (position != null || textFinder != null || pageAndPosition != null) {
//            try {
//                BaseFont baseFont = getBaseFont();
//                if (fontName != null && baseFont == null) {
//                    baseFont = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//                }
//                font = new Font(baseFont, fontSize, Font.NORMAL, textColor);
//                X509Certificate signingCert = null;
//                if (signerCertificate != null) {
//                    CertificateFactory cf = new CertificateFactory();
//                    try ( ByteArrayInputStream bais = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(signerCertificate))) {
//                        signingCert = (X509Certificate) cf.engineGenerateCertificate(bais);
//                    }
//                }
//                initContent(date, signingCert);
//            } catch (DocumentException | IOException ex) {
//                ex.printStackTrace();
//                throw new NullPointerException("Can't load content");
//            }
//        }
//
//        for (int i = 0; i < dataToBeSign.size(); i++) {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            PdfReader.unethicalreading = true;
//            PdfReader reader;
//            if (passwordList == null) {
//                reader = new PdfReader(dataToBeSign.get(i));
//            } else {
//                if (passwordList.get(i) == null) {
//                    reader = new PdfReader(dataToBeSign.get(i), null);
//                } else {
//                    reader = new PdfReader(dataToBeSign.get(i), passwordList.get(i).getBytes());
//                }
//            }
//            PdfStamperMI stamper = PdfStamperMI.createSignature(reader, baos, '\0', null, true);
//            PdfSignatureAppearanceMI appearance = stamper.getSignatureAppearance();
//            if (position != null || textFinder != null || pageAndPosition != null) {
//                initPosition(reader); //initPosition                
//                sigTable = createEsealTable(font);
//
//                appearance.setVisibleSignature(position, signingPageInt, signatureId);
//                appearance.setSignDate(signingTime);
//
//                if (writeAll) {
//                    int[] pages = new int[totalNumOfPages];
//                    for (int j = 0; j < totalNumOfPages; j++) {
//                        pages[j] = j + 1;
//                    }
//                    appearance.setPagesForInitials(pages);
//                }
//
//                if (certified) {
//                    appearance.setCertificationLevel(PdfSignatureAppearanceMI.CERTIFIED_NO_CHANGES_ALLOWED);
//                }
//
//                PdfTemplate n2 = appearance.getLayer(2);
//                if (!dsImage) {
//                    ColumnText ct = new ColumnText(n2);
//                    ct.setSimpleColumn(0, 0, width, height);
//                    ct.setExtraParagraphSpace(0);
//                    ct.setLeading(0);
//                    ct.addElement(sigTable);
//                    ct.go();
//                }
//
////                if (image != null) {
////                    n2.addImage(image);
////                }
//                PdfTemplate n0 = appearance.getLayer(0);
////                if (background != null) {
////                    n0.addImage(background);
////                }
//
////                try {
////                    byte[] bg = ImageGenerator.createBackground((int) position.getWidth(), (int) position.getHeight(), defaultBackground);
////                    Image bgIMG = Image.getInstance(bg);
////                    bgIMG.setAbsolutePosition(0, 0);
////                    n0.addImage(bgIMG);
////                } catch (Exception ex) {
////                    throw new Exception("Can't add default background");
////                }
////                try {
////                    byte[] bg = ImageGenerator.createBorder((int) position.getWidth(), (int) position.getHeight(), defaultBorder);
////                    Image bgIMG = Image.getInstance(bg);
////                    bgIMG.setAbsolutePosition(0, 0);
////                    n0.addImage(bgIMG);
////                } catch (Exception ex) {
////                    throw new Exception("Can't add default border");
////                }
//                if (layer0Icons != null) {
//                    for (Image layer0Icon : layer0Icons) {
//                        n0.addImage(layer0Icon);
//                    }
//                }
//
////                appearance.setCheckMark(checkMark, checkMarkPosition);
////                appearance.setCheckText(checkText, checkTextPosition);
//                appearance.setSigPosList(sigPosList);
//
//            } else {
//                appearance.setVisibleSignature(new Rectangle(0, 0, 0, 0), 1, signatureId);
//            }
//            if (reason != null) {
//                appearance.setReason(reason);
//            }
//            if (location != null) {
//                appearance.setLocation(location);
//            }
//
//            ExternalSignatureContainer external = new ExternalBlankSignatureContainer(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
//            MakeSignatureMI.signExternalContainer(appearance, external, 10240 + ltvSize + tsaSize);
//            tempDataList.add(baos.toByteArray());
//
//            FileOutputStream outputStream = new FileOutputStream("file/test.pdf");
//
//            outputStream.write(baos.toByteArray());
//
//            outputStream.close();
//        }
//    }
//    
//    @Override
//    List<byte[]> appendSignautre(List<String> signatureList) throws Exception {
//        Calendar signingTime = Calendar.getInstance();
//        signingTime.setTimeInMillis(timeMillis);
//
//        X509Certificate[] cert = this.certificates.toArray(new X509Certificate[certificates.size()]);
//        List<byte[]> result = new ArrayList<>();
//        TSAClient tsaClient = null;
//        if (form != null && form.isTsa()) {
//            tsaClient = new TSAClientBouncyCastle(tsaData[0], tsaData[1], tsaData[2], 8192, algorithm.getValue());
//        }
//
//        for (int i = 0; i < tempDataList.size(); i++) {
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            BouncyCastleDigest digest = new BouncyCastleDigest();
//            PdfPKCS7CMS sgn = new PdfPKCS7CMS(null, cert, algorithm.getValue(), null, digest, false);
//            sgn.setSignDate(signingTime);
//            PdfReader.unethicalreading = true;
//            PdfReader reader;
//            if (passwordList == null) {
//                reader = new PdfReader(tempDataList.get(i));
//            } else {
//                if (passwordList.get(i) == null) {
//                    reader = new PdfReader(tempDataList.get(i), null);
//                } else {
//                    reader = new PdfReader(tempDataList.get(i), passwordList.get(i).getBytes());
//                }
//            }          
//
//            AcroFields af = reader.getAcroFields();
//            PdfDictionary v = af.getSignatureDictionary(signatureId);
//            if (v == null) {
//                throw new DocumentException("No field");
//            }
//            if (!af.signatureCoversWholeDocument(signatureId)) {
//                throw new DocumentException("Not the last signature");
//            }
//
//            PdfArray b = v.getAsArray(PdfName.BYTERANGE);
//            long[] gaps = b.asLongArray();
//            if (b.size() != 4 || gaps[0] != 0) {
//                throw new DocumentException("Single exclusion space supported");
//            }
//            RandomAccessSource readerSource = reader.getSafeFile().createSourceView();
//            byte[] extSignature = Base64.decode(signatureList.get(i));
//            sgn.setExternalDigest(extSignature, null, "RSA");
//
//            byte[] signedContent = sgn.getEncodedPKCS7(
//                    otherList.get(i),
//                    tsaClient,
//                    ocsp,
//                    crls,
//                    MakeSignature.CryptoStandard.CMS);
//            int spaceAvailable = (int) (gaps[2] - gaps[1]) - 2;
//            if ((spaceAvailable & 1) != 0) {
//                throw new DocumentException("Gap is not a multiple of 2");
//            }
//            spaceAvailable /= 2;
//            if (spaceAvailable < signedContent.length) {
//                throw new DocumentException("Not enough space");
//            }
//            StreamUtil.CopyBytes(readerSource, 0, gaps[1] + 1, baos);
//            ByteBuffer bb = new ByteBuffer(spaceAvailable * 2);
//            for (byte bi : signedContent) {
//                bb.appendHex(bi);
//            }
//            int remain = (spaceAvailable - signedContent.length) * 2;
//            for (int k = 0; k < remain; ++k) {
//                bb.append((byte) 48);
//            }
//            bb.writeTo(baos);
//            StreamUtil.CopyBytes(readerSource, gaps[2] - 1, gaps[3] + 1, baos);
//
//            byte[] completeData = baos.toByteArray();
//            reader.close();
//
//            if (form.isRevocation()) {
//                if (passwordList == null) {
//                    reader = new PdfReader(completeData);
//                } else {
//                    if (passwordList.get(i) == null) {
//                        reader = new PdfReader(completeData, null);
//                    } else {
//                        reader = new PdfReader(completeData, passwordList.get(i).getBytes());
//                    }
//                }
//                completeData = enrichLT(reader);
//                reader.close();
//            }
//            result.add(completeData);
//
//        }
//        return result;
//    }
//}
