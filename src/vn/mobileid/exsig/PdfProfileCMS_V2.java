/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.exsig;

import com.itextpdf.text.BadElementException;
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
import com.itextpdf.text.pdf.AcroFields.Item;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ByteBuffer;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearanceMI.RenderingMode;
import com.itextpdf.text.pdf.PdfSignatureAppearanceMI;
import com.itextpdf.text.pdf.PdfStamperMI;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.SignaturePosition;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.naming.InvalidNameException;
import javax.xml.bind.DatatypeConverter;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.mobileid.serialize.ProfileJSONSerializer;

/**
 *
 * @author GIATK
 */
public class PdfProfileCMS_V2 extends PdfProfile implements Serializable {

    private transient final Logger log = LoggerFactory.getLogger(PdfProfileCMS_V2.class);

    private String signerCertificate;
    protected List<Long> listTimeStamp = new ArrayList<>();
    protected transient List<TextFinder> textFinderArray = new ArrayList<>();    
    private Algorithm signingAlgorithm = Algorithm.RSA;

    public PdfProfileCMS_V2(Algorithm algorithm) {
        super(PdfForm.B, algorithm);
    }

    public PdfProfileCMS_V2(PdfForm form, Algorithm algorithm) {
        super(form, algorithm);
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
            sgn.setSignDate(signingTime);
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

            //Check Permission
            //Check Security of the PDF file
            if (!checkPermission(reader)) {
                throw new Exception("This document was Locked or Certified");
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
            sgn.setExternalDigest(extSignature, null, signingAlgorithm.getValue());

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
    void generateHash(List<byte[]> dataToBeSign) throws Exception {

        Calendar signingTime = Calendar.getInstance();
        signingTime.setTimeInMillis(timeMillis);
        Date date = signingTime.getTime();

        if(signatureId == null || signatureId.isEmpty()){
        signatureId = "sig-"
                + Calendar.getInstance().getTimeInMillis();
        }

        Font font = null;
        BaseFont baseFont = null;
        
        if (position != null || textFinder != null || pageAndPosition != null) {
            try {
                 baseFont = getBaseFont();                 
                if (fontName != null && baseFont == null) {
                    baseFont = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                }
//                BaseFont baseFont = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                font = new Font(baseFont, fontSize, Font.NORMAL, textColor);
                X509Certificate signingCert = null;
                if (signerCertificate != null) {
                    CertificateFactory cf = new CertificateFactory();
                    try ( ByteArrayInputStream bais = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(signerCertificate))) {
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
//            sigPosList.clear(); // FIX MULTIPLE FILE, DIFFERENT PAGES NUMBER
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
            if (position != null || textFinder != null || pageAndPosition != null || !textFinderArray.isEmpty()) {
                initPosition(reader); //initPosition
                initPositionOfTextFinderArray(reader);
                sigTable =  createImage(font);

                position.setRight(iRec.getRight() + position.getLeft());
                position.setTop(position.getBottom() + iRec.getTop());
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
                    ct.setSimpleColumn(iRec);
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

                try {
                    byte[] bg = ImageGenerator.createBackground((int) position.getWidth(), (int) position.getHeight(), defaultBackground);
                    Image bgIMG = Image.getInstance(bg);
                    bgIMG.setAbsolutePosition(0, 0);
                    n0.addImage(bgIMG);
                } catch (Exception ex) {
                    throw new Exception("Can't add default background");
                }

                try {
                    byte[] bg = ImageGenerator.createBorder((int) position.getWidth(), (int) position.getHeight(), defaultBorder);
                    Image bgIMG = Image.getInstance(bg);
                    bgIMG.setAbsolutePosition(0, 0);
                    n0.addImage(bgIMG);
                } catch (Exception ex) {
                    throw new Exception("Can't add default border");
                }

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
            byte[] hashData = DigestAlgorithms.digest(new ByteArrayInputStream(sh), digest.getMessageDigest(algorithm.getValue()));
            hashList.add(new String(Base64.encode(hashData)));
        }
    }

    void generateHashMultipleFiles(List<PDFSignatureProperties> pdfSignaturePropertieses) throws Exception {
        signatureId = "sig-"
                + Calendar.getInstance().getTimeInMillis();

        for (PDFSignatureProperties pdfSignatureProperties : pdfSignaturePropertieses) {
            //clear signature appearance
            this.position = null;
            this.textFinder = null;
            this.pageAndPosition = null;
            this.boxSize = null;
            this.signingPageInt = 0;
            this.image = null;
            this.imageAlgin = null;
            this.imageProfile = null;
            this.layer0Icons.clear();
            this.sigPosList = pdfSignatureProperties.getSigPosLis();
            this.textFinderArray = pdfSignatureProperties.getTextFinderArray();            
            this.fontName = DefaultFont.Times.getPath();
            this.fontSize = 13;
            this.textAlignment = TextAlignment.ALIGN_LEFT;
            this.textColor = BaseColor.BLACK;            

            //set variables
            setTextContent(pdfSignatureProperties.getTextContent());
            setSigningTime(pdfSignatureProperties.getTimeMillis(), pdfSignatureProperties.getTimeFormat());
            if (pdfSignatureProperties.getReason() != null) {
                setReason(pdfSignatureProperties.getReason());
            }
            Calendar signingTime = Calendar.getInstance();
            signingTime.setTimeInMillis(timeMillis);
            Date date = signingTime.getTime();
            this.listTimeStamp.add(timeMillis);
            if (pdfSignatureProperties.getVisibleSignatureType() != 0) {
                switch (pdfSignatureProperties.getVisibleSignatureType()) {
                    case 1:
                        setVisibleSignature(pdfSignatureProperties.getPage(), pdfSignatureProperties.getOffset(), pdfSignatureProperties.getBoxSize(), pdfSignatureProperties.getText());
                        break;
                    case 2:
                        setVisibleSignature(pdfSignatureProperties.getOffset(), pdfSignatureProperties.getBoxSize(), pdfSignatureProperties.getText(), pdfSignatureProperties.isPlaceAll());
                        break;
                    case 3:
                        setVisibleSignature(pdfSignatureProperties.getBoxSize(), pdfSignatureProperties.getPageAndPosition());
                        break;
                    case 4:
                        setVisibleSignature(pdfSignatureProperties.getOffset(), pdfSignatureProperties.getBoxSize(), pdfSignatureProperties.getText());
                        break;
                    default:
                        setVisibleSignature(pdfSignatureProperties.getPage(), pdfSignatureProperties.getPosition());
                        break;
                }
            }

            if (pdfSignatureProperties.getImageType() != 0) {
                switch (pdfSignatureProperties.getImageType()) {
                    case 1:
                        setImage(pdfSignatureProperties.getImage(), pdfSignatureProperties.getImageProfile());
                        break;
                    default:
                        setImage(pdfSignatureProperties.getImage(), pdfSignatureProperties.getImageProfile(), pdfSignatureProperties.getImageAlign());
                        break;
                }
            }

            if (pdfSignatureProperties.getFontType() != 0) {
                switch (pdfSignatureProperties.getFontType()) {
                    case 1:
                        setFont(pdfSignatureProperties.getFont(), pdfSignatureProperties.getFontSize(), pdfSignatureProperties.getLineSpacing(), pdfSignatureProperties.getTextAlignment(), pdfSignatureProperties.getTextColor());
                        break;
                    case 2:
                        setFont(pdfSignatureProperties.getFontName(), pdfSignatureProperties.getFontSize(), pdfSignatureProperties.getLineSpacing(), pdfSignatureProperties.getTextAlignment(), pdfSignatureProperties.getTextColor());
                        break;
                    default:
                        setFont(pdfSignatureProperties.getFontData(),
                                pdfSignatureProperties.getEncoding(),
                                pdfSignatureProperties.isEmbedded(),
                                pdfSignatureProperties.getFontSize(),
                                pdfSignatureProperties.getLineSpacing(),
                                pdfSignatureProperties.getTextAlignment(),
                                pdfSignatureProperties.getTextColor());
                        break;
                }
            }

            if (pdfSignatureProperties.getSignerCertificate() != null) {
                setSignerCertificate(pdfSignatureProperties.getSignerCertificate());
            }

            if (pdfSignatureProperties.getLayer0Icon() != null
                    && pdfSignatureProperties.getLayer0IconPosition() != null) {
                addLayer0Icon(pdfSignatureProperties.getLayer0Icon(), pdfSignatureProperties.getLayer0IconPosition());
            }

            Font font = null;
            if (position != null || textFinder != null || pageAndPosition != null) {
                try {
                    BaseFont baseFont = getBaseFont();
                    if (fontName != null && baseFont == null) {
                        baseFont = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    }
                    font = new Font(baseFont, fontSize, Font.NORMAL, textColor);
                    X509Certificate signingCert = null;
                    if (signerCertificate != null) {
                        CertificateFactory cf = new CertificateFactory();
                        try ( ByteArrayInputStream bais = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(signerCertificate))) {
                            signingCert = (X509Certificate) cf.engineGenerateCertificate(bais);
                        }
                    }
                    initContent(date, signingCert);
                } catch (DocumentException | IOException ex) {
                    throw new NullPointerException("Can't load content");
                }
            }

            //sigPosList.clear(); // FIX MULTIPLE FILE, DIFFERENT PAGES NUMBER
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfReader.unethicalreading = true;
            PdfReader reader;
            if (passwordList == null) {
                reader = new PdfReader(pdfSignatureProperties.getPdfData());
            } else {
                if (pdfSignatureProperties.getPdfPassword() == null) {
                    reader = new PdfReader(pdfSignatureProperties.getPdfData(), null);
                } else {
                    reader = new PdfReader(pdfSignatureProperties.getPdfData(), pdfSignatureProperties.getPdfPassword().getBytes());
                }
            }
            PdfStamperMI stamper = PdfStamperMI.createSignature(reader, baos, '\0', null, true);
            PdfSignatureAppearanceMI appearance = stamper.getSignatureAppearance();
            if (position != null || textFinder != null || pageAndPosition != null) {
                //if (i == 0) {
                initPosition(reader); //initPosition
                initPositionOfTextFinderArray(reader);
                sigTable = createImage(font);
                //}
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
                    ct.setSimpleColumn(iRec);
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

                try {
                    byte[] bg = ImageGenerator.createBackground((int) position.getWidth(), (int) position.getHeight(), defaultBackground);
                    Image bgIMG = Image.getInstance(bg);
                    bgIMG.setAbsolutePosition(0, 0);
                    n0.addImage(bgIMG);
                } catch (Exception ex) {
                    throw new Exception("Can't add default background");
                }

                try {
                    byte[] bg = ImageGenerator.createBorder((int) position.getWidth(), (int) position.getHeight(), defaultBorder);
                    Image bgIMG = Image.getInstance(bg);
                    bgIMG.setAbsolutePosition(0, 0);
                    n0.addImage(bgIMG);
                } catch (Exception ex) {
                    throw new Exception("Can't add default border");
                }

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

            ExternalSignatureContainer external = new ExternalBlankSignatureContainer(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
            MakeSignatureMI.signExternalContainer(appearance, external, 10240 + ltvSize + tsaSize);
            tempDataList.add(baos.toByteArray());
        }

        for (int i = 0; i < tempDataList.size(); i++) {
            Calendar signingTime = Calendar.getInstance();
            signingTime.setTimeInMillis(listTimeStamp.get(i));
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
            byte[] hash = DigestAlgorithms.digest(rg, digest.getMessageDigest(algorithm.getValue()));
            otherList.add(hash);
            byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, signingTime.getTime(), ocsp, crls, MakeSignature.CryptoStandard.CMS);
            byte[] hashData = DigestAlgorithms.digest(new ByteArrayInputStream(sh), digest.getMessageDigest(algorithm.getValue()));
            hashList.add(new String(Base64.encode(hashData)));
        }
    }

    @Override
    public List<byte[]> sign(SigningMethodSync signingMethod, List<byte[]> dataToBeSign) throws Exception {
        if (signingMethod == null) {
            throw new NullPointerException("Signing Method can't be null");
        }

        generateHash(dataToBeSign);
        initCerts(signingMethod.getCert());
        List<String> sigList = signingMethod.sign(hashList);

        return appendSignautre(sigList);
    }

    @Override
    public List<byte[]> sign(SigningMethodSync signingMethod, List<byte[]> dataToBeSign, List<String> passwordList) throws Exception {
        this.passwordList = passwordList;
        return sign(signingMethod, dataToBeSign);
    }

    public static List<byte[]> sign(SigningMethodAsync signingMethod, byte[] temp) throws Exception {
        List<byte[]> listTemp = new ArrayList<>();
        listTemp.add(temp);
        try {
            Profile profile;
            List<String> sigList = signingMethod.pack();
            try ( ByteArrayInputStream bais = new ByteArrayInputStream(temp)) {
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
            throw new Exception("Cannot generate hash: ", e);
        }
        signingMethod.generateTempFile(hashList);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try ( ObjectOutputStream objectOut = new ObjectOutputStream(baos)) {
                objectOut.writeObject(this);
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new Exception("Can't create temp file: ", ex);
        }
    }

    public byte[] createTemporalMultipleFiles(SigningMethodAsync signingMethod, List<PDFSignatureProperties> pdfSignatureProperties) throws Exception {
        if (signingMethod == null) {
            throw new Exception("Signing Method can't be null");
        }
        if (pdfSignatureProperties == null) {
            throw new Exception("pdfSignatureProperties can't be null");
        }
        if (pdfSignatureProperties.isEmpty()) {
            throw new Exception("pdfSignatureProperties can't be empty");
        }

        try {
            generateHashMultipleFiles(pdfSignatureProperties);
        } catch (Exception e) {
            throw new Exception("Cannot generate hash: ", e);
        }
        signingMethod.generateTempFile(hashList);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try ( ObjectOutputStream objectOut = new ObjectOutputStream(baos)) {
                objectOut.writeObject(this);
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new Exception("Can't create temp file: ", ex);
        }
    }

    public void setSignerCertificate(String signerCertificate) {
        this.signerCertificate = signerCertificate;
    }

    @Override
    public void addLayer0Icon(byte[] data, String position) throws Exception {

        if (data == null) {
            throw new Exception("data is null");
        }
        if (position == null) {
            throw new Exception("position is null");
        }

        int[] iGrid = new int[4];
        Rectangle scale;
        try {
            String[] sGrid = position.replace("\n", "").replace("\t", "").replace(" ", "").split(",");
            for (int i = 0; i < 4; i++) {
                iGrid[i] = Integer.parseInt(sGrid[i]);
                if ((iGrid[i] < 0)) {
                    throw new Exception("Invalid position parameter");
                }
            }
            scale = new Rectangle(iGrid[0], iGrid[1], iGrid[2], iGrid[3]);
        } catch (Exception ex) {
            throw new Exception("Invalid position parameter" + ex);
        }

        try {
            Image ic = Image.getInstance(data);
            ic.scaleAbsolute(scale);
            ic.setAbsolutePosition(scale.getLeft(), scale.getBottom());
            layer0Icons.add(ic);
        } catch (BadElementException | IOException ex) {
            log.error("setBackground", ex);
            throw new Exception("Can't load image " + ex.getMessage());
        }
    }

    public void setRevocationData(byte[] ocsp, List<byte[]> crls) {
        this.ocsp = ocsp;
        this.crls = crls;
        if(ocsp != null){
            ltvSize += 4192;
        }
        if(crls != null && !crls.isEmpty()){
            ltvSize += 4192;
        }
    }

    //Update 20222311 by GiaTK    
    public boolean checkPermission(List<byte[]> data) throws Exception {
        if (data == null) {
            throw new Exception("Data check lock can't be null");
        }
        try {
            for (int i = 0; i < data.size(); i++) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfReader.unethicalreading = true;
                PdfReader reader;
                if (passwordList == null) {
                    reader = new PdfReader(data.get(i));
                } else {
                    if (passwordList.get(i) == null) {
                        reader = new PdfReader(data.get(i), null);
                    } else {
                        reader = new PdfReader(data.get(i), passwordList.get(i).getBytes());
                    }
                }
                AcroFields acro = reader.getAcroFields();
                Map<String, Item> maps = acro.getFields();
                for (String key : maps.keySet()) {
                    PdfDictionary v = acro.getSignatureDictionary(key);

                    Item item = maps.get(key);
                    PdfDictionary dict = item.getValue(0);

                    if (v.contains(PdfName.REFERENCE)) {
                        PdfArray ref = v.getAsArray(PdfName.REFERENCE);
                        for (int j = 0; j < ref.size(); j++) {
                            PdfName method = ref.getAsDict(j).getAsName(PdfName.TRANSFORMMETHOD);
                            if (method == null) {
                                continue;
                            }
                            if (method.compareTo(PdfName.DOCMDP) == 0) {
                                PdfNumber num = ref.getAsDict(j).getAsDict(PdfName.TRANSFORMPARAMS).getAsNumber(PdfName.P);
                                if (num.intValue() == 1) {
//                                throw new DocumentException("Error:CERTIFIED_NO_CHANGES_ALLOWED!");
                                    return false;
                                } else if (num.intValue() == 2) {
                                    return true;
//                                throw new DocumentException("Error:CERTIFIED_FILLING_FORM");
                                } else if (num.intValue() == 3) {
                                    return true;
//                                throw new DocumentException("Error:CERTIFIED_FILLING_FORM_AND_ANNOTATION");
                                }
                            }
                            if (method.compareTo(PdfName.FIELDMDP) == 0) {
                                PdfNumber num = ref.getAsDict(j).getAsDict(PdfName.TRANSFORMPARAMS).getAsNumber(PdfName.P);
                                if (num == null) {
                                    if (!checkLock(dict)) {
                                        return false;
                                    }
                                    continue;
                                }
                                if (num.intValue() == 1) {
                                    return false;
//                                throw new DocumentException("Error:LOCKED_NO_CHANGES_ALLOWED!");
                                } else if (num.intValue() == 2) {
                                    return true;
//                                throw new DocumentException("Error:LOCKED_FILLING_FORM");
                                } else if (num.intValue() == 3) {
                                    return true;
//                                throw new DocumentException("Error:LOCKED_FILLING_FORM_AND_ANNOTATION");
                                }
                            }
                        }
                    }

                    boolean lock = checkLock(dict);
                    if (!lock) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception ex) {
//            ex.printStackTrace();
            throw new Exception("Error while checking locked Document!", ex);
        }
    }

    /**
     * Using to check Lock field in PDFDictionary input.
     *
     * @param field PdfDictionary of which data wants to be check
     * @return true if PdfDict don't contain otherwise false.
     */
    private static boolean checkLock(PdfDictionary field) throws DocumentException {
        if (field == null) {
            throw new DocumentException("Field check lock is null!");
        }
        if (!field.contains(PdfName.LOCK)) {
            return true;
        }

        PdfDictionary lock = field.getAsDict(PdfName.LOCK);
        PdfNumber P = lock.getAsNumber(PdfName.P);
        if (P != null) {
            if (P.intValue() == 1) {
                return false;
            }
        }
        return true;
    }

    public boolean checkPermission(PdfReader reader) throws Exception {
        AcroFields acro = reader.getAcroFields();
        Map<String, Item> maps = acro.getFields();
        if (acro == null || maps == null) {
            return true;
        }

        for (String key : maps.keySet()) {
            PdfDictionary v = acro.getSignatureDictionary(key);

            Item item = maps.get(key);
            PdfDictionary dict = item.getValue(0);

            if (v != null) {
                if (v.contains(PdfName.REFERENCE)) {
                    PdfArray ref = v.getAsArray(PdfName.REFERENCE);
                    for (int j = 0; j < ref.size(); j++) {
                        PdfName method = ref.getAsDict(j).getAsName(PdfName.TRANSFORMMETHOD);
                        if (method == null) {
                            continue;
                        }
                        if (method.compareTo(PdfName.DOCMDP) == 0) {
                            PdfNumber num = ref.getAsDict(j).getAsDict(PdfName.TRANSFORMPARAMS).getAsNumber(PdfName.P);
                            if (num.intValue() == 1) {
//                                throw new DocumentException("Error:CERTIFIED_NO_CHANGES_ALLOWED!");
                                return false;
                            }
//                            else if (num.intValue() == 2) {
//                                continue;
////                                throw new DocumentException("Error:CERTIFIED_FILLING_FORM");
//                            } else if (num.intValue() == 3) {
//                                continue;
////                                throw new DocumentException("Error:CERTIFIED_FILLING_FORM_AND_ANNOTATION");
//                            }
                        }
                        if (method.compareTo(PdfName.FIELDMDP) == 0) {
                            PdfNumber num = ref.getAsDict(j).getAsDict(PdfName.TRANSFORMPARAMS).getAsNumber(PdfName.P);
                            if (num == null) {
                                if (!checkLock(dict)) {
                                    return false;
                                }
                                continue;
                            }
                            if (num.intValue() == 1) {
                                return false;
//                                throw new DocumentException("Error:LOCKED_NO_CHANGES_ALLOWED!");
                            } else if (num.intValue() == 2) {
                                continue;
//                                throw new DocumentException("Error:LOCKED_FILLING_FORM");
                            } else if (num.intValue() == 3) {
                                continue;
//                                throw new DocumentException("Error:LOCKED_FILLING_FORM_AND_ANNOTATION");
                            }
                        }
                    }
                }
            }

            boolean lock = checkLock(dict);
            if (!lock) {
                return false;
            }
        }
        return true;
    }

    //Update 20230503 by GIATK
    // Make the TextFinder become an array =>Multiple check    
    protected void initPositionOfTextFinderArray(PdfReader reader) throws Exception {
        try {
            totalNumOfPages = reader.getNumberOfPages();
//            if (pageAndPosition == null) {
            for (int i = 0; i < textFinderArray.size(); i++) {
                SignaturePosition signature = new SignaturePosition(0, iRec);

                TextPlusXY textPlusXY = null;
                StringCoordinatesExtraction stringCoordinatesExtraction = new StringCoordinatesExtraction(reader);
                if (textFinderArray.get(i).page != -1) {
                    //tim tren 1 trang xac dinh
                    if (textFinderArray.get(i).page == 0) {
                        signature.setPage(totalNumOfPages); // last
                    } else {
                        signature.setPage(textFinderArray.get(i).page);
                        if (signature.getPage() > totalNumOfPages) {
                            throw new Exception("Page " + signature.getPage() + " not found");
                        }
                    }
                    textPlusXY = stringCoordinatesExtraction.getCoordinate(textFinderArray.get(i).text, signature.getPage());
                    if (textPlusXY == null) {
                        throw new Exception("TEXT [" + textFinderArray.get(i).text + "] not found");
                    }
                } else {
                    //tim tat ca cac trang
                    //update 03/08/2021
                    List<TextPlusXY> textPlusXYs = stringCoordinatesExtraction.getCoordinate(textFinderArray.get(i).text);
                    if (textPlusXYs.isEmpty()) {
                        throw new Exception("TEXT [" + textFinderArray.get(i).text + "] not found");
                    }
                    textPlusXY = textPlusXYs.get(0);
                    signature.setPage(textPlusXY.getPage());

                    //update 2023/04/05
                    if (textFinderArray.get(i).placeAll) {
                        try {
                            for (int j = 1; j < textPlusXYs.size(); j++) {
                                TextPlusXY otherTextPlusXY = textPlusXYs.get(j);
                                int detectedLowerX = (int) Math.ceil(otherTextPlusXY.getX());
                                int detectedLowerY = (int) Math.ceil(otherTextPlusXY.getY());
                                detectedLowerX += textFinderArray.get(i).offsetX;
                                detectedLowerY += textFinderArray.get(i).offsetY;
                                int calculatedUpperX = (int) (detectedLowerX + textFinderArray.get(i).width);
                                int calculatedUpperY = (int) (detectedLowerY + textFinderArray.get(i).height);
                                sigPosList.add(new SignaturePosition(otherTextPlusXY.getPage(), new Rectangle(detectedLowerX, detectedLowerY, calculatedUpperX, calculatedUpperY)));
                            }
                        } catch (Exception ex) {
                            throw new Exception("Error while detect text!", ex);
                        }
                    }
                }

                if (textPlusXY == null) {
                    throw new NullPointerException("The text : '" + textFinderArray.get(i).text + "' not found ");
                }

                int detectedLowerX = (int) Math.ceil(textPlusXY.getX());
                int detectedLowerY = (int) Math.ceil(textPlusXY.getY());

                detectedLowerX += textFinderArray.get(i).offsetX;
                detectedLowerY += textFinderArray.get(i).offsetY;

                int calculatedUpperX = (int) (detectedLowerX + textFinderArray.get(i).width);
                int calculatedUpperY = (int) (detectedLowerY + textFinderArray.get(i).height);
                sigPosList.add(new SignaturePosition(signature.getPage(), new Rectangle(detectedLowerX, detectedLowerY, calculatedUpperX, calculatedUpperY)));
            }

            if (!sigPosList.isEmpty()) {
                List<SignaturePosition> newSigPosList = new ArrayList<>();
                for (SignaturePosition signaturePosition : sigPosList) {
                    if (signaturePosition.getPage() > totalNumOfPages) {
                        throw new Exception("Page " + signaturePosition.getPage() + " not found");
                    }
                    if (signaturePosition.getPage() == 0) {
                        newSigPosList.add(new SignaturePosition(totalNumOfPages, signaturePosition.getRectangle()));
                    } else {
                        newSigPosList.add(new SignaturePosition(signaturePosition.getPage(), signaturePosition.getRectangle()));
                    }
                }
                sigPosList = newSigPosList;
            }

//            } else {
//                Map<Integer, String> pageAndPositionAsTreeMap = new TreeMap<>(pageAndPosition);
//
//                boolean checkCountVariable = false;
//                int countVariable = 0;
//                if (totalNumOfPages < pageAndPositionAsTreeMap.size()) {
//                    checkCountVariable = true;
//                }
//
//                for (Integer pageNo : pageAndPositionAsTreeMap.keySet()) {
//                    System.out.println("Total:"+totalNumOfPages);
//                    System.out.println("PageNo:"+pageNo);
//                    if (pageNo == 0 || pageNo > totalNumOfPages) {
//                        throw new Exception("Invalid pageNo: " + pageNo);
//                    }
//
//                    String[] vPosition = pageAndPositionAsTreeMap.get(pageNo).replace("\n", "").replace("\t", "").replace(" ", "").split(",");
//
//                    if (vPosition.length != 2) {
//                        throw new Exception("Invalid position parameter");
//                    }
//
//                    float[] fPosition = new float[2];
//                    fPosition[0] = Float.parseFloat(vPosition[0]);
//                    fPosition[1] = Float.parseFloat(vPosition[1]);
//
//                    int detectedLowerX = (int) Math.ceil(fPosition[0]);
//                    int detectedLowerY = (int) Math.ceil(fPosition[1]);
//                    //detectedLowerX += textFinder.offsetX; //no offset
//                    //detectedLowerY += textFinder.offsetY; //no offset
//                    int calculatedUpperX = (int) (detectedLowerX + boxSize[0]);
//                    int calculatedUpperY = (int) (detectedLowerY + boxSize[1]);
//                    sigPosList.add(new SignaturePosition(pageNo, new Rectangle(detectedLowerX, detectedLowerY, calculatedUpperX, calculatedUpperY)));
//                    countVariable++;
//                    if (checkCountVariable) {
//                        if (countVariable == totalNumOfPages) {
//                            break;
//                        }
//                    }
//                }
//                signingPageInt = sigPosList.get(0).getPage();
//                position = sigPosList.get(0).getRectangle();
//                //remove first element
//                sigPosList.remove(0);
//            }
        } catch (Exception ex) {
//            ex.printStackTrace();
            throw new Exception("Can't init signature position", ex);
        }
    }

    public void addMoreSignaturePosition(String page, String offset, String boxSize, String text) throws Exception {
        if (page == null) {
            throw new Exception("page is null");
        }
        if (offset == null) {
            throw new Exception("offset is null");
        }
        if (boxSize == null) {
            throw new Exception("box size is null");
        }
        if (text == null) {
            throw new Exception("finding text is null");
        }
        int pageNum;
        try {
            switch (page.toUpperCase()) {
                case "FIRST":
                    pageNum = 1;
                    break;
                case "LAST":
                    pageNum = 0;
                    break;
                default:
                    pageNum = Integer.parseInt(page);
                    if (pageNum < 1) {
                        throw new Exception("Invalid page number");
                    }
                    break;
            }
        } catch (Exception ex) {
            throw new Exception("Invalid page number :", ex);
        }

        int[] iGrid = new int[2];
        int[] iSize = new int[2];
        try {
            String[] sGrid = offset.replace("\n", "").replace("\t", "").replace(" ", "").split(",");
            String[] sSize = boxSize.replace("\n", "").replace("\t", "").replace(" ", "").split(",");
            for (int i = 0; i < 2; i++) {
                iGrid[i] = Integer.parseInt(sGrid[i]);
                iSize[i] = Integer.parseInt(sSize[i]);
                if ((iSize[i] < 0)) {
                    throw new Exception("Invalid position parameter");
                }
            }
            this.textFinderArray.add(new TextFinder(pageNum, iGrid[0], iGrid[1], iSize[0], iSize[1], text, false));
        } catch (Exception ex) {
            throw new Exception("Invalid position parameter", ex);
        }
    }

    public void clearSignaturePositions() {
        this.sigPosList.clear();
        this.textFinderArray.clear();
    }   

   

    public void setSigningAlgorithm(Algorithm algorithm) {
        this.signingAlgorithm = algorithm;
    }
}
