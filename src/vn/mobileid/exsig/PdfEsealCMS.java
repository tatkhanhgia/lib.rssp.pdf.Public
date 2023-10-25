/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.mobileid.exsig;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.RASInputStream;
import com.itextpdf.text.io.RandomAccessSource;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ByteBuffer;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfBoolean;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearanceMI;
import com.itextpdf.text.pdf.PdfStamperMI;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalBlankSignatureContainer;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignatureMI;
import com.itextpdf.text.pdf.security.PdfPKCS7CMS;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InvalidNameException;
import javax.xml.bind.DatatypeConverter;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author GIATK
 */
public class PdfEsealCMS extends PdfProfile {

    private transient EsealTitle esealTitle = new EsealTitle();
    private transient boolean isTitleVisible = true;
    private transient String contact = null;
    private transient int width = 135;
    private transient int height = 39;
    private transient String signerCertificate;
    private transient Rectangle rect;
    private transient EsealContent esealContent = new EsealContent();
    private transient EsealIcon esealIcon = new EsealIcon();

    class CellBackground implements PdfPCellEvent {

        private Font font;

        public CellBackground(Font font) {
            this.font = font;
        }

        public void cellLayout(
                PdfPCell cell,
                Rectangle rect,
                PdfContentByte[] canvas) {

            Image image = null;
            try {
                if (esealIcon == null || esealIcon.getIcon() == null) {
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    InputStream is = loader.getResourceAsStream("resources/44_icon_Chip_50x50_Background.png");

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                    int nRead;
                    byte[] data = new byte[16384];

                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }

                    image = Image.getInstance(buffer.toByteArray());
                } else {
                    image = Image.getInstance(esealIcon.getIcon());
                }

                image.setAbsolutePosition(rect.getWidth() * 3 / 100 + esealIcon.paddingleft, rect.getHeight() * 75f / 100 - esealIcon.paddingtop);
                image.setBackgroundColor(new BaseColor(239, 236, 236));
                float height = rect.getHeight() * 30 / 100;
                if (esealIcon.getSize() >= 0) {
                    image.scaleToFit(height / 1.5f, height / 1.5f);
                } else {
                    image.scaleToFit(esealIcon.getSize(), esealIcon.getSize());
                }
            } catch (Exception ex) {
                try {
                    throw new Exception("Can't init EsealIcon");
                } catch (Exception ex1) {
                    Logger.getLogger(PdfEsealCMS.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }

            PdfContentByte cb = canvas[PdfPTable.BACKGROUNDCANVAS];

            cb.roundRectangle(
                    0 + 1,
                    (rect.getTop() - rect.getBottom()) * 70 / 100 + 1,
                    rect.getWidth() - 1.5,
                    (rect.getTop() - rect.getBottom()) * 30 / 100 - 1.5,
                    2);
            cb.setColorFill(new BaseColor(239, 236, 236));
            cb.fill();

            cb.rectangle(
                    0 + 1,
                    (rect.getTop() - rect.getBottom()) * 70 / 100 + 1,
                    rect.getWidth() - 1.5,
                    (rect.getTop() - rect.getBottom()) * 30 / 100 - 5);
            cb.setColorFill(new BaseColor(239, 236, 236));
            cb.fill();

            cb.roundRectangle(
                    0 + 1f,
                    0 + 1f,
                    rect.getWidth() - 1.5f,
                    rect.getHeight() - 1.5f,
                    2);
            cb.setColorStroke(BaseColor.LIGHT_GRAY);
            cb.stroke();

            try {
                cb.addImage(image, true);
            } catch (DocumentException ex) {
                Logger.getLogger(PdfEsealCMS.class.getName()).log(Level.SEVERE, null, ex);
            }

            Chunk c = new Chunk(
                    esealTitle.getTitle(),
                    new Font(font.getBaseFont(),
                            esealTitle.getSize(),
                            Font.BOLD));
            Paragraph p = new Paragraph();
            p.add(c);
            PdfContentByte canvas2 = canvas[PdfPTable.TEXTCANVAS];

            ColumnText.showTextAligned(
                    canvas2,
                    Element.ALIGN_LEFT,
                    p,
                    rect.getWidth() * 10f / 100 + esealTitle.getPaddingleft(),
                    rect.getHeight() * 81f / 100 - esealTitle.getPaddingtop(),
                    0);

        }
    }

    public PdfEsealCMS(PdfForm form, Algorithm algorithm) {
        super(form, algorithm);
        this.textContent = "";
    }

    private PdfPTable createEsealTable(Font fontTitle, Font fontContent) throws FileNotFoundException, DocumentException {
        PdfPTable table = new PdfPTable(1);

        table.setSpacingAfter(0);
        table.setSpacingBefore(0);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{1});

        PdfPCell cellTitle = new PdfPCell();
        cellTitle.setLeft(position.getLeft());
        cellTitle.setRight(position.getRight());
        cellTitle.setBottom(position.getBottom());
        cellTitle.setTop(position.getTop());

        cellTitle.setBorder(Rectangle.NO_BORDER);
        cellTitle.setNoWrap(false);
        cellTitle.setLeading(1f, 5f);
        cellTitle.setPaddingTop(calculateTopPadding(position));
        cellTitle.setPaddingLeft(calculateLeftPadding(position));
        cellTitle.setFixedHeight(position.getTop() - cellTitle.getBottom());
        if (isTitleVisible) {
            cellTitle.setCellEvent(new CellBackground(fontTitle));
        }

        String title = esealContent.textBold;
        Phrase p1 = new Phrase(
                new Chunk(title, new Font(fontTitle.getBaseFont(), esealContent.fontSizeBig, Font.BOLD))
        );

        String content = esealContent.textNormal;
        Phrase p2 = new Phrase(new Chunk(content, new Font(fontContent.getBaseFont(), esealContent.fontSizeSmall, Font.NORMAL)));

        cellTitle.addElement(p1);
        cellTitle.addElement(p2);
        table.addCell(cellTitle);

        return table;
    }

    private float calculateTopPadding(Rectangle iRect) {
        float top = 0;
        top = iRect.getHeight() - (iRect.getHeight() * 73f / 100);
        return top;
    }

    private float calculateLeftPadding(Rectangle iRect) {
        float left = 0;
        if (image != null && imageProfile.equals(ImageProfile.IMAGE_LEFT)) {
            left = iRect.getWidth() * 3 / 100 + image.getPlainWidth() + 2f;
            return left;
        }
        left = iRect.getWidth() * 3 / 100;
        return left;
    }

    @Override
    void generateHash(List<byte[]> dataToBeSign) throws Exception {

        Calendar signingTime = Calendar.getInstance();
        signingTime.setTimeInMillis(timeMillis);
        Date date = signingTime.getTime();

        if (this.signatureId == null || this.signatureId.isEmpty()) {
            signatureId = "sig-"
                    + Calendar.getInstance().getTimeInMillis();
        }

        if (position != null || textFinder != null || pageAndPosition != null) {
            try {
                if (this.esealContent.fontTitle == null) {
                    createDefaultTitleFont();
                }
                if (this.esealContent.fontContent == null) {
                    createDefaultContentFont();
                }

                X509Certificate signingCert = null;
                if (signerCertificate != null) {
                    CertificateFactory cf = new CertificateFactory();
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(signerCertificate))) {
                        signingCert = (X509Certificate) cf.engineGenerateCertificate(bais);
                    }
                }
                initContent(date, signingCert);
            } catch (DocumentException | IOException ex) {
                ex.printStackTrace();
                throw new NullPointerException("Can't load content");
            }
        }

        for (int i = 0; i < dataToBeSign.size(); i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfReader.unethicalreading = true;
            PdfReader reader;
            if (passwordList == null) {
                reader = new PdfReader(dataToBeSign.get(i));
            } else {
                if (passwordList.get(i) == null) {
                    reader = new PdfReader(dataToBeSign.get(i), null);
                } else {
                    reader = new PdfReader(dataToBeSign.get(i), passwordList.get(i).getBytes());
                }
            }
            PdfStamperMI stamper = PdfStamperMI.createSignature(reader, baos, '\0', null, true);
            PdfSignatureAppearanceMI appearance = stamper.getSignatureAppearance();
            appearance = createEsealAttribute(appearance);
            if (position != null || textFinder != null || pageAndPosition != null) {
                initPosition(reader); //initPosition        
                this.rect = position;
                this.createImage(null);
                sigTable = createEsealTable(esealContent.fontTitle, esealContent.fontContent);
                appearance.setVisibleSignature(position, signingPageInt, signatureId);
                appearance.setSignDate(signingTime);

                if (writeAll) {
                    int[] pages = new int[totalNumOfPages];
                    for (int j = 0; j < totalNumOfPages; j++) {
                        pages[j] = j + 1;
                    }
                    appearance.setPagesForInitials(pages);
                }

                if (certified) {
                    appearance.setCertificationLevel(PdfSignatureAppearanceMI.CERTIFIED_NO_CHANGES_ALLOWED);
                }

                PdfTemplate n2 = appearance.getLayer(2);

                if (!dsImage) {
                    ColumnText ct = new ColumnText(n2);
                    ct.setSimpleColumn(0, 0, this.position.getWidth(), this.position.getHeight());
                    ct.setExtraParagraphSpace(0);
                    ct.setLeading(0);
                    ct.addElement(sigTable);
                    ct.go();
                }

                if (image != null) {
                    n2.addImage(image);
                }

                PdfTemplate n0 = appearance.getLayer(0);
                if (background != null) {
                    n0.addImage(background);
                }

//                try {
//                    byte[] bg = ImageGenerator.createBackground((int) position.getWidth(), (int) position.getHeight(), defaultBackground);
//                    Image bgIMG = Image.getInstance(bg);
//                    bgIMG.setAbsolutePosition(0, 0);
//                    n0.addImage(bgIMG);
//                } catch (Exception ex) {
//                    throw new Exception("Can't add default background");
//                }
//                try {
//                    byte[] bg = ImageGenerator.createBorder((int) position.getWidth(), (int) position.getHeight(), defaultBorder);
//                    Image bgIMG = Image.getInstance(bg);
//                    bgIMG.setAbsolutePosition(0, 0);
//                    n0.addImage(bgIMG);
//                } catch (Exception ex) {
//                    throw new Exception("Can't add default border");
//                }
                if (layer0Icons != null) {
                    for (Image layer0Icon : layer0Icons) {
                        n0.addImage(layer0Icon);
                    }
                }

                appearance.setCheckMark(checkMark, checkMarkPosition);
                appearance.setCheckText(checkText, checkTextPosition);
                appearance.setSigPosList(sigPosList);

            } else {
                appearance.setVisibleSignature(new Rectangle(0, 0, 0, 0), 1, signatureId);
            }
            if (reason != null) {
                appearance.setReason(reason);
            }
            if (location != null) {
                appearance.setLocation(location);
            }
            if (contact != null) {
                appearance.setContact(contact);
            }

            ExternalSignatureContainer external = new ExternalBlankSignatureContainer(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
            MakeSignatureMI.signExternalContainer(appearance, external, 10240 + ltvSize + tsaSize);
            tempDataList.add(baos.toByteArray());
        }

        for (int i = 0; i < tempDataList.size(); i++) {
            PdfReader.unethicalreading = true;
            PdfReader reader;
            if (passwordList == null) {
                reader = new PdfReader(tempDataList.get(i));
            } else {
                if (passwordList.get(i) == null) {
                    reader = new PdfReader(tempDataList.get(i), null);
                } else {
                    reader = new PdfReader(tempDataList.get(i), passwordList.get(i).getBytes());
                }
            }

            AcroFields af = reader.getAcroFields();
            PdfDictionary v = af.getSignatureDictionary(signatureId);
            if (v == null) {
                throw new DocumentException("No field");
            }
            if (!af.signatureCoversWholeDocument(signatureId)) {
                throw new DocumentException("Not the last signature");
            }
            PdfArray b = v.getAsArray(PdfName.BYTERANGE);
            long[] gaps = b.asLongArray();
            if (b.size() != 4 || gaps[0] != 0) {
                throw new DocumentException("Single exclusion space supported");
            }
            RandomAccessSource readerSource = reader.getSafeFile().createSourceView();
            InputStream rg = new RASInputStream(new RandomAccessSourceFactory().createRanged(readerSource, gaps));
            BouncyCastleDigest digest = new BouncyCastleDigest();
            PdfPKCS7CMS sgn = new PdfPKCS7CMS(null, null, algorithm.getValue(), null, digest, false);
            sgn.setSignDate(signingTime);
            byte[] hash = DigestAlgorithms.digest(rg, digest.getMessageDigest(algorithm.getValue()));
            otherList.add(hash);
            byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, signingTime.getTime(), ocsp, crls, MakeSignature.CryptoStandard.CMS);
//            byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, ocsp, crls, MakeSignature.CryptoStandard.CMS);
            byte[] hashData = DigestAlgorithms.digest(new ByteArrayInputStream(sh), digest.getMessageDigest(algorithm.getValue()));
            hashList.add(new String(Base64.encode(hashData)));
        }
    }

    @Override
    List<byte[]> appendSignautre(List<String> signatureList) throws Exception {
        Calendar signingTime = Calendar.getInstance();
        signingTime.setTimeInMillis(timeMillis);

        X509Certificate[] cert = this.certificates.toArray(new X509Certificate[certificates.size()]);
        List<byte[]> result = new ArrayList<>();
        TSAClient tsaClient = null;
        if (form != null && form.isTsa()) {
            tsaClient = new TSAClientBouncyCastle(tsaData[0], tsaData[1], tsaData[2], 8192, algorithm.getValue());
        }

        for (int i = 0; i < tempDataList.size(); i++) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BouncyCastleDigest digest = new BouncyCastleDigest();
            PdfPKCS7CMS sgn = new PdfPKCS7CMS(null, cert, algorithm.getValue(), null, digest, false);
//            sgn.setSignDate(signingTime);
            PdfReader.unethicalreading = true;
            PdfReader reader;
            if (passwordList == null) {
                reader = new PdfReader(tempDataList.get(i));
            } else {
                if (passwordList.get(i) == null) {
                    reader = new PdfReader(tempDataList.get(i), null);
                } else {
                    reader = new PdfReader(tempDataList.get(i), passwordList.get(i).getBytes());
                }
            }

            AcroFields af = reader.getAcroFields();
            PdfDictionary v = af.getSignatureDictionary(signatureId);
            if (v == null) {
                throw new DocumentException("No field");
            }
            if (!af.signatureCoversWholeDocument(signatureId)) {
                throw new DocumentException("Not the last signature");
            }

            PdfArray b = v.getAsArray(PdfName.BYTERANGE);
            long[] gaps = b.asLongArray();
            if (b.size() != 4 || gaps[0] != 0) {
                throw new DocumentException("Single exclusion space supported");
            }
            RandomAccessSource readerSource = reader.getSafeFile().createSourceView();
            byte[] extSignature = Base64.decode(signatureList.get(i));
            sgn.setExternalDigest(extSignature, null, "RSA");

            byte[] signedContent = sgn.getEncodedPKCS7(
                    otherList.get(i),
                    tsaClient,
                    ocsp,
                    crls,
                    MakeSignature.CryptoStandard.CMS);
            int spaceAvailable = (int) (gaps[2] - gaps[1]) - 2;
            if ((spaceAvailable & 1) != 0) {
                throw new DocumentException("Gap is not a multiple of 2");
            }
            spaceAvailable /= 2;
            if (spaceAvailable < signedContent.length) {
                throw new DocumentException("Not enough space");
            }
            StreamUtil.CopyBytes(readerSource, 0, gaps[1] + 1, baos);
            ByteBuffer bb = new ByteBuffer(spaceAvailable * 2);
            for (byte bi : signedContent) {
                bb.appendHex(bi);
            }
            int remain = (spaceAvailable - signedContent.length) * 2;
            for (int k = 0; k < remain; ++k) {
                bb.append((byte) 48);
            }
            bb.writeTo(baos);
            StreamUtil.CopyBytes(readerSource, gaps[2] - 1, gaps[3] + 1, baos);

            byte[] completeData = baos.toByteArray();
            reader.close();

            if (form.isRevocation()) {
                if (passwordList == null) {
                    reader = new PdfReader(completeData);
                } else {
                    if (passwordList.get(i) == null) {
                        reader = new PdfReader(completeData, null);
                    } else {
                        reader = new PdfReader(completeData, passwordList.get(i).getBytes());
                    }
                }
                completeData = enrichLT(reader);
                reader.close();
            }
            result.add(completeData);

        }
        return result;
    }

    @Override
    public byte[] createTemporalFile(SigningMethodAsync signingMethod, List<byte[]> dataToBeSign) throws Exception {
        if (signingMethod == null) {
            throw new Exception("Signing Method can't be null");
        }
        if (dataToBeSign == null) {
            throw new Exception("Data to be sign can't be null");
        }
        try {
            generateHash(dataToBeSign);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Cannot generate hash: ", e);
        }
        signingMethod.generateTempFile(hashList);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream objectOut = new ObjectOutputStream(baos)) {
                objectOut.writeObject(this);
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new Exception("Can't create temp file: ", ex);
        }
    }

    public static List<byte[]> sign(SigningMethodAsync signingMethod, byte[] temp) throws Exception {
        List<byte[]> listTemp = new ArrayList<>();
        listTemp.add(temp);
        try {
            Profile profile;
            List<String> sigList = signingMethod.pack();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(temp)) {
                ObjectInputStream oi = new ObjectInputStream(bais);
                profile = (Profile) oi.readObject();
            }

            profile.initCerts(signingMethod.getCert());
            return profile.appendSignautre(sigList);
        } catch (IOException | ClassNotFoundException ex) {
            throw new Exception("Can't load temp file : ", ex);
        }
    }

    @Override
    public byte[] createTemporalFile(SigningMethodAsync signingMethod, List<byte[]> dataToBeSign, List<String> passwordList) throws Exception {
        this.passwordList = passwordList;
        return createTemporalFile(signingMethod, dataToBeSign);
    }

    public void setSignerCertificate(String signerCertificate) {
        this.signerCertificate = signerCertificate;
    }

    private void createDefaultTitleFont() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("resources\\Roboto-Bold.ttf");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        BaseFont baseFont = BaseFont.createFont(
                "myfont.ttf",
                BaseFont.IDENTITY_H,
                true,
                true,
                buffer.toByteArray(),
                null);
        esealContent.fontTitle = new Font(baseFont, esealContent.fontSizeBig, Font.BOLD, BaseColor.BLACK);
    }

    private void createDefaultContentFont() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream("resources\\Roboto-Regular.ttf");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        BaseFont baseFont = BaseFont.createFont(
                "myfont2.ttf",
                BaseFont.IDENTITY_H,
                true,
                true,
                buffer.toByteArray(),
                null);
        esealContent.fontContent = new Font(baseFont, esealContent.fontSizeSmall, Font.BOLD, BaseColor.BLACK);
    }

    @Override
    protected PdfPTable createImage(Font font) throws Exception {
        try {
            if (dsImage) {
                image = Image.getInstance(ImageGenerator.remoteSign(header,
                        titleFont,
                        signatureFont,
                        getCertificateInfo((X509Certificate) certificates.get(0), "CN"),
                        ((X509Certificate) certificates.get(0)).getSerialNumber().toString()));
                image.scaleAbsolute(position);
                image.setAbsolutePosition((position.getRight() - position.getLeft()) / 2 - image.getScaledWidth() / 2,
                        (position.getTop() - position.getBottom()) / 2 - image.getScaledHeight() / 2);
                return null;
            } else {
                if (image != null) {
                    switch (imageProfile) {
                        case IMAGE_LEFT:
                            imageProfileLeft();
                            break;
                        case IMAGE_RIGHT:
                            imageProfileRight();
                            break;
                        case IMAGE_BOTTOM:
//                            imageProfileBottom();
//                            break;
                        case IMAGE_BOTTOM_TEXT_BOTTOM:
//                            imageProfileBottom();
//                            break;
                        case IMAGE_TOP:
//                            imageProfileTop();
//                            break;
                        case IMAGE_TOP_TEXT_TOP:
//                            imageProfileTop();
//                            break;
                        case IMAGE_CENTER:
//                            imageProfileCenter();                            
//                            break;
                        default:
//                            iRec = new Rectangle(
//                                    (position.getWidth() / 2 - (position.getWidth() * 0.9f) / 2) + this.paddingLeft,
//                                    position.getHeight() / 2 - (position.getHeight() * 0.9f) / 2,
//                                    (position.getWidth() / 2 + (position.getWidth() * 0.9f) / 2) - this.paddingRight,
//                                    position.getHeight() / 2 + (position.getHeight() * 0.9f) / 2);
                            throw new Exception("NOT SUPPORT THIS TYPE OF IMAGE PROFILE");
                    }

                }
            }
            return null;
        } catch (DocumentException | FontFormatException | IOException | InvalidNameException ex) {
            throw new Exception("Can't generate signature text box", ex);
        }
    }

    @Override
    protected void imageProfileLeft() {
        image.scaleToFit((position.getHeight()) * 85 / 100 - imageProfile.border * 2,
                (position.getHeight()) * 85 / 100 - imageProfile.border * 2
        );
        image.setAbsolutePosition(rect.getWidth() * 3 / 100, 2.3f);
        rect = new Rectangle(
                image.getAbsoluteX() + image.getScaledWidth() + imageProfile.border + this.paddingLeft,
                image.getAbsoluteY(),
                position.getWidth() - this.paddingRight,
                position.getHeight());
    }

    @Override
    protected void imageProfileRight() {
        image.scaleToFit((position.getHeight()) * 85 / 100 - imageProfile.border * 2,
                (position.getHeight()) * 85 / 100 - imageProfile.border * 2
        );
        image.setAbsolutePosition(position.getWidth() - image.getScaledWidth() - 2.3f, 2.3f);
//        iRec = new Rectangle(
//                0 + this.paddingLeft,
//                0,
//                position.getWidth() - image.getScaledWidth() - imageProfile.border - this.paddingRight,
//                position.getHeight());
    }

    @Override
    protected void imageProfileBottom() {
        image.scaleToFit(position.getRight() - position.getLeft() - imageProfile.border * 2,
                (position.getTop() - position.getBottom()) / 2 - imageProfile.border * 2
        );
        if (imageAlgin == null) {
            image.setAbsolutePosition((position.getRight() - position.getLeft()) / 2 - image.getScaledWidth() / 2, 0);
        } else {
            if (imageAlgin == ImageAlgin.ALIGN_LEFT) {
                image.setAbsolutePosition(
                        0 + imageProfile.border,
                        0);
            } else if (imageAlgin == ImageAlgin.ALIGN_RIGHT) {
                image.setAbsolutePosition(
                        position.getRight() - position.getLeft() - image.getScaledWidth() - imageProfile.border,
                        0);
            } else {
                image.setAbsolutePosition((position.getRight() - position.getLeft()) / 2 - image.getScaledWidth() / 2, 0);
            }
        }
        iRec = new Rectangle(
                0 + this.paddingLeft,
                position.getHeight() - image.getScaledHeight() - imageProfile.border,
                position.getWidth() - this.paddingRight,
                position.getHeight());
    }

    @Override
    protected void imageProfileTop() {
        image.scaleToFit(
                position.getRight() - position.getLeft() - imageProfile.border * 2,
                (position.getTop() - position.getBottom()) / 2 - imageProfile.border * 2
        );
        if (imageAlgin == null) {
            image.setAbsolutePosition(
                    (position.getRight() - position.getLeft()) / 2 - image.getScaledWidth() / 2,
                    (position.getTop() - position.getBottom()) / 2 + imageProfile.border / 2
            );
        } else {
            if (imageAlgin == ImageAlgin.ALIGN_LEFT) {
                image.setAbsolutePosition(
                        0 + imageProfile.border,
                        (position.getTop() - position.getBottom()) / 2 + imageProfile.border / 2
                );
            } else if (imageAlgin == ImageAlgin.ALIGN_RIGHT) {
                image.setAbsolutePosition(
                        position.getRight() - position.getLeft() - image.getScaledWidth() - imageProfile.border,
                        (position.getTop() - position.getBottom()) / 2 + imageProfile.border / 2
                );
            } else {
                image.setAbsolutePosition(
                        (position.getRight() - position.getLeft()) / 2 - image.getScaledWidth() / 2,
                        (position.getTop() - position.getBottom()) / 2 + imageProfile.border / 2
                );
            }
        }
        iRec = new Rectangle(
                0 + this.paddingLeft,
                0,
                position.getWidth() - this.paddingRight,
                position.getHeight() - image.getScaledHeight() - imageProfile.border);
    }

    @Override
    protected void imageProfileCenter() {
        image.scaleToFit((position.getRight() - position.getLeft() - imageProfile.border * 2),
                (position.getTop() - position.getBottom() - imageProfile.border * 2)
        );
        image.setAbsolutePosition((position.getRight() - position.getLeft() - image.getScaledWidth()) / 2,
                (position.getTop() - position.getBottom() - image.getScaledHeight()) / 2
        );
        iRec = new Rectangle(
                0 + this.paddingLeft,
                0,
                position.getWidth() - this.paddingRight,
                position.getHeight()
        );
    }

    //=============================EXTERNAL=====================================
    /**
     * Create Default Eseal Frame
     *
     * @param page
     * @param x
     * @param y
     * @param title
     * @param content
     * @throws Exception
     */
    public void createEseal(
            int page,
            int x,
            int y,
            String title,
            String content
    ) throws Exception {
        this.position = new Rectangle(x, y, width + x, height + y);
        if (title != null && !title.isEmpty() && this.esealContent.textBold == null) {
            this.esealContent.textBold = title;
        }
        if (content != null && !content.isEmpty() && this.esealContent.textNormal == null) {
            this.esealContent.textNormal = content;
        }
        if (page <= 0) {
            throw new Exception("page is null");
        }
        try {
            signingPageInt = page;
        } catch (Exception ex) {
            throw new Exception("Invalid page number " + page, ex);
        }
    }

    /**
     * Create Custom Eseal Frame
     *
     * @param page
     * @param x
     * @param y
     * @param width
     * @param height
     * @throws Exception
     */
    public void createEsealFrame(
            int page,
            int x,
            int y,
            int width,
            int height
    ) throws Exception {
        this.position = new Rectangle(x, y, width + x, height + y);
        try {
            signingPageInt = page;
        } catch (Exception ex) {
            throw new Exception("Invalid page number " + page, ex);
        }
    }

    /**
     * Create Font Size of Contents in Eseal Frame
     *
     * @param fontSizeBig
     * @param fontSizeSmall
     */
    public void setEsealFontSize(int fontSizeBig, int fontSizeSmall) {
        esealContent.fontSizeBig = fontSizeBig;
        esealContent.fontSizeSmall = fontSizeSmall;
    }

    /**
     * Custome font of Contents in Eseal Frame
     *
     * @param fontData
     * @param encoding
     * @param embedded
     * @param fontSize
     * @param lineSpacing
     * @param alignment
     * @param textColor
     * @throws Exception
     */
    public void setEsealTitleFont(byte[] fontData,
            String encoding,
            boolean embedded,
            float fontSize,
            float lineSpacing,
            TextAlignment alignment,
            Color textColor) throws Exception {
        BaseFont baseFont = BaseFont.createFont(
                "myfont.ttf",
                encoding,
                embedded,
                true,
                fontData,
                null);
        esealContent.fontTitle = new Font(baseFont, fontSize, Font.BOLD, BaseColor.BLACK);
    }

    /**
     * Custom font of Contents in Eseal Frame
     *
     * @param fontData
     * @param encoding
     * @param embedded
     * @param fontSize
     * @param lineSpacing
     * @param alignment
     * @param textColor
     * @throws Exception
     */
    public void setEsealContentFont(byte[] fontData,
            String encoding,
            boolean embedded,
            float fontSize,
            float lineSpacing,
            TextAlignment alignment,
            Color textColor) throws Exception {
        BaseFont baseFont = BaseFont.createFont(
                "myfont.ttf",
                encoding,
                embedded,
                true,
                fontData,
                null);
        esealContent.fontContent = new Font(baseFont, fontSize, Font.NORMAL, BaseColor.BLACK);
    }

    /**
     * Custom Title of Eseal
     *
     * @param title
     */
    public void setEsealTitle(EsealTitle title) {
        this.esealTitle = title;
    }

    /**
     * Custom Title of Eseal
     *
     * @param title
     * @param size
     * @param paddingtop
     * @param paddingleft
     */
    public void setEsealTitle(String title, float size, float paddingtop, float paddingleft) {
        this.esealTitle = new EsealTitle(title, size, paddingtop, paddingleft);
    }

    /**
     * Create Default Title of Eseal
     *
     * @param title
     */
    public void setEsealTitle(String title) {
        this.esealTitle = new EsealTitle();
        this.esealTitle.setTitle(title);
    }

    /**
     * Custom Icon of Eseal
     *
     * @param icon
     * @param size
     * @param paddingtop
     * @param paddingleft
     */
    public void setEsealIcon(byte[] icon, float size, float paddingtop, float paddingleft) {
        this.esealIcon = new EsealIcon(icon, size, paddingtop, paddingleft);
    }

    /**
     * Create Default Icon of Eseal
     *
     * @param icon
     */
    public void setEsealIcon(byte[] icon) {
        this.esealIcon = new EsealIcon(icon, 0, 0, 0);
    }

    /**
     * Custome Icon of Eseal
     *
     * @param esealIcon
     */
    public void setEsealIcon(EsealIcon esealIcon) {
        this.esealIcon = esealIcon;
    }

    public void setEsealContent(EsealContent esealContent) {
        this.esealContent.fontSizeBig = esealContent.fontSizeBig;
        this.esealContent.fontSizeSmall = esealContent.fontSizeSmall;
        this.esealContent.textBold = esealContent.textBold;
        this.esealContent.textNormal = esealContent.textNormal;
        if (this.esealContent.fontContent == null) {
            this.esealContent.fontContent = esealContent.fontContent;
        }
        if (this.esealContent.fontTitle == null) {
            this.esealContent.fontTitle = esealContent.fontTitle;
        }
    }

    /**
     * Set the name of the signature. Otherwise, library will generate random
     * name for that signature
     *
     * @param name
     */
    public void setSignatureName(String name) {
        this.signatureId = name;
    }

    public String getSignatureName() {
        return this.signatureId;
    }

    @Override
    protected void initContent(Date date, X509Certificate cert) throws Exception {
        try {
            SimpleDateFormat format = new SimpleDateFormat(timeFormat);
            esealContent.textBold = esealContent.textBold.replace("{location}", location);
            esealContent.textBold = esealContent.textBold.replace("{reason}", reason);
            esealContent.textBold = esealContent.textBold.replace("{date}", format.format(date));
            esealContent.textBold = esealContent.textBold.replace("{signby}", getCertificateInfo(cert, "2.5.4.3"));
            esealContent.textBold = esealContent.textBold.replace("{organize}", getCertificateInfo(cert, "2.5.4.10"));
            esealContent.textBold = esealContent.textBold.replace("{organizationunit}", getCertificateInfo(cert, "2.5.4.11"));
            esealContent.textBold = esealContent.textBold.replace("{email}", getCertificateInfo(cert, "1.2.840.113549.1.9.1"));
            esealContent.textBold = esealContent.textBold.replace("{phone}", getCertificateInfo(cert, "2.5.4.20"));

            esealContent.textBold = esealContent.textBold.replace("{title}", getCertificateInfo(cert, "2.5.4.12"));
            esealContent.textBold = esealContent.textBold.replace("{givenname}", getCertificateInfo(cert, "2.5.4.42"));
//            textContent = textContent.replace("{serialnumber}", getSerialNumber(cert));
//            textContent = textContent.replace("{personalid}", getPersonalID(cert));
//            textContent = textContent.replace("{enterpriseid}", getEnterpriseID(cert));

            esealContent.textNormal = esealContent.textNormal.replace("{location}", location);
            esealContent.textNormal = esealContent.textNormal.replace("{reason}", reason);
            esealContent.textNormal = esealContent.textNormal.replace("{date}", format.format(date));
            esealContent.textNormal = esealContent.textNormal.replace("{signby}", getCertificateInfo(cert, "2.5.4.3"));
            esealContent.textNormal = esealContent.textNormal.replace("{organize}", getCertificateInfo(cert, "2.5.4.10"));
            esealContent.textNormal = esealContent.textNormal.replace("{organizationunit}", getCertificateInfo(cert, "2.5.4.11"));
            esealContent.textNormal = esealContent.textNormal.replace("{email}", getCertificateInfo(cert, "1.2.840.113549.1.9.1"));
            esealContent.textNormal = esealContent.textNormal.replace("{phone}", getCertificateInfo(cert, "2.5.4.20"));
            esealContent.textNormal = esealContent.textNormal.replace("{title}", getCertificateInfo(cert, "2.5.4.12"));
            esealContent.textNormal = esealContent.textNormal.replace("{givenname}", getCertificateInfo(cert, "2.5.4.42"));
        } catch (UnsupportedEncodingException | InvalidNameException ex) {
            throw new Exception("Can't prepare text content", ex);
        }
    }

    public static class EsealIcon {

        public static final float defaultSize = 0;
        private transient byte[] icon;
        private transient float size;
        private transient float paddingtop;
        private transient float paddingleft;

        public EsealIcon() {
            icon = null;
            paddingtop = 0;
            paddingleft = 0;
            size = 0;
        }

        public EsealIcon(byte[] icon, float size, float paddingtop, float paddingleft) {
            this.icon = icon;
            this.size = size;
            this.paddingtop = paddingtop;
            this.paddingleft = paddingleft;
        }

        public byte[] getIcon() {
            return icon;
        }

        public void setIcon(byte[] icon) {
            this.icon = icon;
        }

        public float getSize() {
            return size;
        }

        public void setSize(float size) {
            this.size = size;
        }

        public float getPaddingtop() {
            return paddingtop;
        }

        public void setPaddingtop(float paddingtop) {
            this.paddingtop = paddingtop;
        }

        public float getPaddingleft() {
            return paddingleft;
        }

        public void setPaddingleft(float paddingleft) {
            this.paddingleft = paddingleft;
        }

    }

    public static class EsealTitle {

        public static final float defaultSize = 4;
        private transient String title = "Advanced Electronic Seal with Qualified Certificate";
        private transient float size;
        private transient float paddingtop;
        private transient float paddingleft;

        public EsealTitle(String title, float size, float paddingtop, float paddingleft) {
            this.title = title;
            this.size = size;
            this.paddingtop = paddingtop;
            this.paddingleft = paddingleft;
        }

        public EsealTitle() {
            this.size = 4;
            this.paddingleft = 0;
            this.paddingtop = 0;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public float getSize() {
            return size;
        }

        public void setSize(float size) {
            this.size = size;
        }

        public float getPaddingtop() {
            return paddingtop;
        }

        public void setPaddingtop(float paddingtop) {
            this.paddingtop = paddingtop;
        }

        public float getPaddingleft() {
            return paddingleft;
        }

        public void setPaddingleft(float paddingleft) {
            this.paddingleft = paddingleft;
        }
    }

    public static class EsealContent {

        private transient int fontSizeBig = 6;
        private transient int fontSizeSmall = 4;
        private transient Font fontTitle;
        private transient Font fontContent;
        private transient String textBold;
        private transient String textNormal;

        public EsealContent() {
        }

        public int getFontSizeBig() {
            return fontSizeBig;
        }

        public void setFontSizeBig(int fontSizeBig) {
            this.fontSizeBig = fontSizeBig;
        }

        public int getFontSizeSmall() {
            return fontSizeSmall;
        }

        public void setFontSizeSmall(int fontSizeSmall) {
            this.fontSizeSmall = fontSizeSmall;
        }

        public Font getFontTitle() {
            return fontTitle;
        }

        public void setFontTitle(Font fontTitle) {
            this.fontTitle = fontTitle;
        }

        public Font getFontContent() {
            return fontContent;
        }

        public void setFontContent(Font fontContent) {
            this.fontContent = fontContent;
        }

        public String getTextBold() {
            return textBold;
        }

        public void setTextBold(String textBold) {
            this.textBold = textBold;
        }

        public String getTextNormal() {
            return textNormal;
        }

        public void setTextNormal(String textNormal) {
            this.textNormal = textNormal;
        }
    }

    public void setSignerContact(String contact) {
        this.contact = contact;
    }

    private PdfSignatureAppearanceMI createEsealAttribute(PdfSignatureAppearanceMI appearance) {
//        PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKMS, PdfName.ADBE_PKCS7_DETACHED);
        PdfDictionary dict = new PdfDictionary(new PdfName("isEseal"));
        dict.put(new PdfName("isESeal"), new PdfBoolean(true));
//        dic.put(PdfName.REASON, new PdfString("Custom property: isESeal"));
//        dic.put(new PdfName("isESeal"), new PdfBoolean(true));
        appearance.setCryptoDictionary(dict);
        return appearance;
    }

    public void setVisibleTitle(boolean isTitleVisible) {
        this.isTitleVisible = isTitleVisible;
    }
}
