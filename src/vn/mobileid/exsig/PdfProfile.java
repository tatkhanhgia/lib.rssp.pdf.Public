/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.exsig;

import com.itextpdf.text.pdf.SignaturePosition;
import vn.mobileid.util.Utils;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
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
import com.itextpdf.text.pdf.AcroFieldsV4;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ByteBuffer;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PRIndirectReference;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfReaderV4;
import com.itextpdf.text.pdf.PdfSignatureAppearanceMI;
import com.itextpdf.text.pdf.PdfStamperMI;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalBlankSignatureContainer;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.LtvTimestampMI;
import com.itextpdf.text.pdf.security.LtvVerificationMI;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignatureMI;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.PdfPKCS7V4;
import com.itextpdf.text.pdf.security.SignaturePermissions;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.xml.bind.DatatypeConverter;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.mobileid.exception.SignatureNotInAcroformException;

/**
 *
 * @author Minhgalc
 */
public class PdfProfile extends Profile implements Serializable {

    private transient final Logger log = LoggerFactory.getLogger(PdfProfile.class);
    protected transient boolean certified;
    protected transient String reason = "";
    protected transient String location = "";
    protected transient float fontSizeMin = 9;

    protected transient ImageProfile imageProfile;
    protected transient ImageAlgin imageAlgin;
    protected transient Image background;
    protected transient Image image;
    protected transient boolean dsImage;

    protected transient TextProfile textProfile;

    protected transient boolean writeAll;
    protected transient String fontName;
    protected transient float fontSize;
    protected transient String header;
    protected transient String signatureFont;
    protected transient String titleFont;
    protected transient String textContent;
    protected transient float paddingLeft = 0;
    protected transient float paddingRight = 0;
    protected transient float lineSpacing = 0;

    protected transient Rectangle position;
    protected transient Rectangle iRec;
    protected transient int totalNumOfPages;
    protected transient int signingPageInt;
    protected transient PdfPTable sigTable;
    protected transient TextAlignment textAlignment = TextAlignment.ALIGN_LEFT;
    protected transient BaseColor textColor = BaseColor.BLACK;
    protected transient float[] checkMarkPosition;
    protected transient float[] checkTextPosition;
    protected transient boolean checkMark = false;
    protected transient boolean checkText = false;
    protected transient boolean autoScale = false;

    protected transient TextFinder textFinder;
    protected transient List<SignaturePosition> sigPosList = new ArrayList<>();

    protected transient List<Image> layer0Icons = new ArrayList<>();

    protected transient java.awt.Color defaultBackground = null;

    protected transient java.awt.Color defaultBorder = null;

    transient private BaseFont baseFont;

    final private static transient String HTML_FONT_STYLE_BOLD_BEGIN = "<b>";
    final private static transient String HTML_FONT_STYLE_BOLD_END = "</b>";
    final private static transient String HTML_FONT_STYLE_ITALIC_BEGIN = "<i>";
    final private static transient String HTML_FONT_STYLE_ITALIC_END = "</i>";
    final private static transient String HTML_FONT_STYLE_BOLD_ITALIC_BEGIN = "<bi>";
    final private static transient String HTML_FONT_STYLE_BOLD_ITALIC_END = "</bi>";
    final private static transient String HTML_FONT_STYLE_UNDERLINE_BEGIN = "<u>";
    final private static transient String HTML_FONT_STYLE_UNDERLINE_END = "</u>";
    final private static transient String HTML_BREAK = "<br/>";

    final private static transient String PREFIX_PERSONAL_CODE = "CMND:";
    final private static transient String PREFIX_PERSONAL_PASSPORT_CODE = "HC:";
    final private static transient String PREFIX_CITIZEN_CODE = "CCCD:";
    final private static transient String PREFIX_ENTERPRISE_TAX_CODE = "MST:";
    final private static transient String PREFIX_ENTERPRISE_BUDGET_CODE = "MNS:";

    protected transient Map<Integer, String> pageAndPosition;
    protected transient float[] boxSize;

    public PdfProfile() {
    }

    public PdfProfile(PdfForm form, Algorithm algorithm) {
        super(form, algorithm);
        this.textContent = "KÝ BỞI: {signby}\nLÝ DO: {reason}\nNƠI KÝ: {location}\nNGÀY KÝ: {date}";
        this.fontName = DefaultFont.Times.getPath();
        this.fontSize = 13;
        this.header = "Mobile-ID Remote Signing service by:";
    }

    public void setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
    }

    public void setBackground(Color color) {
        defaultBackground = color.getAwtColor();
    }

    public void setBorder(Color color) {
        defaultBorder = color.getAwtColor();
    }

    public void setSigningTime(long timeMillis, String format) {
        this.timeMillis = timeMillis;
        this.timeFormat = format;
    }

    public void setSigningTime(Calendar calendar, String format) {
        this.timeMillis = calendar.getTimeInMillis();
        this.timeFormat = format;
    }

    public void setSignInitial(boolean signingInitial) {
        this.writeAll = signingInitial;
    }

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

    //update 03/08/2021
    public void setVisibleSignature(String page, String offset, String boxSize, String text) throws Exception {
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
            //update 03/08/2021
            this.textFinder = new TextFinder(pageNum, iGrid[0], iGrid[1], iSize[0], iSize[1], text, false);
        } catch (Exception ex) {
            throw new Exception("Invalid position parameter", ex);
        }
    }

    //update 03/08/2021
    public void setVisibleSignature(String offset, String boxSize, String text, boolean placeAll) throws Exception {
        if (offset == null) {
            throw new Exception("offset is null");
        }
        if (boxSize == null) {
            throw new Exception("box size is null");
        }
        if (text == null) {
            throw new Exception("finding text is null");
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
            //update 03/08/2021
            this.textFinder = new TextFinder(-1, iGrid[0], iGrid[1], iSize[0], iSize[1], text, placeAll);
        } catch (Exception ex) {
            throw new Exception("Invalid position parameter", ex);
        }
    }

    //update 20220521 - nghiep vu TCB, dung toa do
    public void setVisibleSignature(String boxSize, Map<Integer, String> pageAndPosition) throws Exception {
        if (boxSize == null) {
            throw new Exception("box size is null");
        }
        if (pageAndPosition == null) {
            throw new Exception("pageAndPosition is null");
        }
        if (pageAndPosition.isEmpty()) {
            throw new Exception("pageAndPosition is empty");
        }

        this.boxSize = new float[2];

        try {
            String[] sSize = boxSize.replace("\n", "").replace("\t", "").replace(" ", "").split(",");
            for (int i = 0; i < 2; i++) {
                this.boxSize[i] = Float.parseFloat(sSize[i]);
                if ((this.boxSize[i] < 0)) {
                    throw new Exception("Invalid position parameter");
                }
            }

            this.pageAndPosition = pageAndPosition;

        } catch (Exception ex) {
            throw new Exception("Invalid position parameter", ex);
        }
    }

    public void setVisibleSignature(String offset, String boxSize, String text) throws Exception {
        setVisibleSignature(offset, boxSize, text, false);
    }

    public void setVisibleSignature(String page, String position) throws Exception {
        if (page == null) {
            throw new Exception("page is null");
        }
        if (position == null) {
            throw new Exception("position is null");
        }
        try {
            switch (page.toUpperCase()) {
                case "FIRST":
                    signingPageInt = 1;
                    break;
                case "LAST":
                    signingPageInt = 0;
                    break;
                default:
                    signingPageInt = Integer.parseInt(page);
                    if (signingPageInt < 1) {
                        throw new Exception("Invalid page number");
                    }
                    break;
            }
        } catch (Exception ex) {
            throw new Exception("Invalid page number " + page, ex);
        }

        int[] iGrid = new int[4];
        try {
            String[] sGrid = position.replace("\n", "").replace("\t", "").replace(" ", "").split(",");
            for (int i = 0; i < 4; i++) {
                iGrid[i] = Integer.parseInt(sGrid[i]);
            }
            this.position = new Rectangle(iGrid[0], iGrid[1], iGrid[2], iGrid[3]);
        } catch (Exception ex) {
            throw new Exception("Invalid position parameter", ex);
        }
    }

    public void setVisibleSignature(String page, String position, boolean isItextRectangle) throws Exception {
        System.out.println("Input position:" + position);
        if (page == null) {
            throw new Exception("page is null");
        }
        if (position == null) {
            throw new Exception("position is null");
        }
        try {
            switch (page.toUpperCase()) {
                case "FIRST":
                    signingPageInt = 1;
                    break;
                case "LAST":
                    signingPageInt = 0;
                    break;
                default:
                    signingPageInt = Integer.parseInt(page);
                    if (signingPageInt < 1) {
                        throw new Exception("Invalid page number");
                    }
                    break;
            }
        } catch (Exception ex) {
            throw new Exception("Invalid page number " + page, ex);
        }

        int[] iGrid = new int[4];
        try {
            String[] sGrid = position.replace("\n", "").replace("\t", "").replace(" ", "").split(",");
            for (int i = 0; i < 4; i++) {
                iGrid[i] = Integer.parseInt(sGrid[i]);
            }
            if (isItextRectangle) {
                this.position = new Rectangle(iGrid[0], iGrid[1], iGrid[2], iGrid[3]);
            } else {
                this.position = new Rectangle(iGrid[0], iGrid[1], iGrid[2] + iGrid[0], iGrid[3] + iGrid[1]);
            }
        } catch (Exception ex) {
            throw new Exception("Invalid position parameter", ex);
        }
    }

    public void addMoreSignaturePosition(String page, String position) throws Exception {
        if (page == null) {
            throw new Exception("page is null");
        }
        if (position == null) {
            throw new Exception("position is null");
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
            throw new Exception("Invalid page number : ", ex);
        }

        int[] iGrid = new int[4];
        try {
            String[] sGrid = position.replace("\n", "").replace("\t", "").replace(" ", "").split(",");
            for (int i = 0; i < 4; i++) {
                iGrid[i] = Integer.parseInt(sGrid[i]);
//                if ((iGrid[i] < 0)) {
//                    throw new Exception("Invalid position parameter");
//                }
            }
            this.sigPosList.add(new SignaturePosition(pageNum, new Rectangle(iGrid[0], iGrid[1], iGrid[2], iGrid[3])));
        } catch (Exception ex) {
            throw new Exception("Invalid position parameter", ex);
        }
    }

    public void setFont(DefaultFont font, float fontSize, float lineSpacing, TextAlignment alignment, Color textColor) throws Exception {
        this.fontName = font.getPath();
        this.fontSize = fontSize;
        this.lineSpacing = lineSpacing;
        this.textAlignment = alignment;
        this.textColor = textColor.getColor();
    }

    public void setFont(String font, float fontSize, float lineSpacing, TextAlignment alignment, Color textColor) throws Exception {
        this.fontName = font;
        this.fontSize = fontSize;
        this.lineSpacing = lineSpacing;
        this.textAlignment = alignment;
        this.textColor = textColor.getColor();
    }

    public void setFont(
            byte[] fontData,
            String encoding,
            boolean embedded,
            float fontSize,
            float lineSpacing,
            TextAlignment alignment,
            Color textColor
    ) {
        try {
            this.baseFont = BaseFont.createFont(
                    "myfont.ttf",
                    encoding,
                    embedded,
                    true,
                    fontData,
                    null);
        } catch (Exception e) {
            throw new RuntimeException("Cannot createFont based on provided fontData");
        }
        this.fontSize = fontSize;
        this.lineSpacing = lineSpacing;
        this.textAlignment = alignment;
        this.textColor = textColor.getColor();
    }

    public void setFont(DefaultFont font, float fontSize) throws Exception {
        setFont(font, fontSize, lineSpacing, null, Color.BLACK);
    }

    public void setCheckMark(boolean checkMark) throws Exception {
        this.checkMark = checkMark;
    }

    public void setCheckMark(boolean checkMark, String checkMarkPosition) throws Exception {
        this.checkMark = checkMark;
        setCheckMarkPosition(checkMarkPosition);
    }

    public void setCheckText(boolean checkMark) throws Exception {
        this.checkText = checkMark;
    }

    public void setCheckText(boolean checkMark, String checkTextPosition) throws Exception {
        this.checkText = checkMark;
        setCheckTextPosition(checkTextPosition);
    }

    protected void setCheckMarkPosition(String checkMarkPosition) throws Exception {
        float[] iGrid = new float[3];
        try {
            String[] sGrid = checkMarkPosition.replace("\n", "").replace("\t", "").replace(" ", "").split(",");
            for (int i = 0; i < 3; i++) {
                iGrid[i] = Integer.parseInt(sGrid[i]);
                if ((iGrid[i] < 0)) {
                    throw new Exception("Invalid grid unit " + iGrid[i]);
                }
            }
            this.checkMarkPosition = iGrid;
        } catch (Exception ex) {
            throw new Exception("Invalid position parameter", ex);
        }
    }

    protected void setCheckTextPosition(String checkTextPosition) throws Exception {
        float[] iGrid = new float[4];
        try {
            String[] sGrid = checkTextPosition.replace("\n", "").replace("\t", "").replace(" ", "").split(",");
            for (int i = 0; i < 4; i++) {
                iGrid[i] = Integer.parseInt(sGrid[i]);
                if ((iGrid[i] < 0)) {
                    throw new Exception("Invalid grid unit " + iGrid[i]);
                }
            }
            this.checkTextPosition = iGrid;
        } catch (Exception ex) {
            throw new Exception("Invalid position parameter", ex);
        }
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void addRootCertificate(boolean rootCertificate) {
        this.rootCertificate = rootCertificate;
    }

    public void setImage(byte[] image, ImageProfile imageProfile) throws Exception {
        try {
            this.image = Image.getInstance(image);
            this.imageProfile = imageProfile;
        } catch (BadElementException | IOException ex) {
            log.error("setImage", ex);
            throw new Exception("Can't load image " + ex.getMessage());
        }
    }

    public void setImage(byte[] image, ImageProfile imageProfile, ImageAlgin imageAlign) throws Exception {
        try {
            this.image = Image.getInstance(image);
            this.imageProfile = imageProfile;
            this.imageAlgin = imageAlign;
        } catch (BadElementException | IOException ex) {
            log.error("setImage", ex);
            throw new Exception("Can't load image " + ex.getMessage());
        }
    }

    public void setCertified(boolean certified) {
        this.certified = certified;
    }

    public void setTextContent(String textContent) {
        setTextContent(textContent, TextProfile.TEXT_BOTTOM_LEFT);
    }

    public void setTextContent(String textContent, TextProfile file) {
        this.textContent = textContent;
        this.textProfile = textProfile;
    }

    public void setTextContent(String textContent, float paddingLeft, float paddingRight) {
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        this.textContent = textContent;
    }

    public void setDSImage(String header, DefaultFont titleFont, DefaultFont signatureFont) throws Exception {
        this.dsImage = true;
        this.signatureFont = signatureFont.getValue();
        this.titleFont = titleFont.getValue();
        this.header = header;
    }

    public void setBackground(byte[] background) throws Exception {
        try {
            this.background = Image.getInstance(background);
        } catch (BadElementException | IOException ex) {
            throw new Exception("Can't load image ", ex);
        }
    }

    /*
    protected String getCertificateInfo(X509Certificate cert, String SubjectDN) throws InvalidNameException, UnsupportedEncodingException {
        try {
            String dn = cert.getSubjectX500Principal().getName();
            LdapName ln = new LdapName(dn);
            for (Rdn rdn : ln.getRdns()) {
                if (rdn.getType().equalsIgnoreCase(SubjectDN)) {
                    if (rdn.getValue() instanceof byte[]) {
                        return new String((byte[]) rdn.getValue(), "UTF-8");
                    }
                    return rdn.getValue().toString();
                }
            }
        } catch (Exception ex) {
        }
        return "NULL";
    }
     */
    protected String getCertificateInfo(X509Certificate cert, String oid) throws InvalidNameException, UnsupportedEncodingException {
        try {
            X500Name subject = new X500Name(cert.getSubjectDN().toString());
            RDN[] rdn = subject.getRDNs();
            for (int j = 0; j < rdn.length; j++) {
                AttributeTypeAndValue[] attributeTypeAndValue = rdn[j].getTypesAndValues();
                if (attributeTypeAndValue[0].getType().toString().equals(oid)) {
                    return attributeTypeAndValue[0].getValue().toString();
                }
            }
        } catch (Exception ex) {
        }
        return "NULL";
    }

    public float fitText(Font font, String text, Rectangle rect, float maxFontSize, int runDirection) {
        try {
//            System.out.println("Input font:"+maxFontSize);
            ColumnText ct = null;
            int status = 0;
            if (maxFontSize <= fontSizeMin) {
                return fontSizeMin;
            }
            if (maxFontSize <= 0) {
                int cr = 0;
                int lf = 0;
                char t[] = text.toCharArray();
                for (int k = 0; k < t.length; ++k) {
                    if (t[k] == '\n') {
                        ++lf;
                    } else if (t[k] == '\r') {
                        ++cr;
                    }
                }
                int minLines = Math.max(cr, lf) + 1;
                maxFontSize = Math.abs(rect.getHeight()) / minLines - 0.001f;
            }
            font.setSize(maxFontSize);
            Phrase ph = new Phrase(text, font);
            ct = new ColumnText(null);
            ct.setSimpleColumn(ph, rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop(), maxFontSize, Element.ALIGN_LEFT);
            ct.setLeading(0, 1.5f);
            ct.setRunDirection(runDirection);
            status = ct.go(true);
            int maxLine = text.split("\n").length;
//            System.out.println("Status1:" + (ct.getExtraParagraphSpace() >= 0));
//            System.out.println("Line:" + ct.getLinesWritten());
//            System.out.println("MaxLine:" + maxLine);
//            System.out.println("Spacing:" + (ct.getCurrentLeading() - maxFontSize));
//            System.out.println("Rect Height:" + rect.getHeight());
//            System.out.println("Status2:" + (((ct.getLinesWritten() * maxFontSize) + ((ct.getCurrentLeading() - maxFontSize) * (ct.getLinesWritten() - 1))) <= rect.getHeight()));
//            
            if ( //                    (status == ColumnText.NO_MORE_TEXT) &&
                    ct.getLinesWritten() >= maxLine
                    && ct.getExtraParagraphSpace() >= 0
                    && (((ct.getLinesWritten() * maxFontSize) + ((ct.getCurrentLeading() - maxFontSize) * (ct.getLinesWritten() - 1))) <= rect.getHeight())) {
                return maxFontSize;
            }
            if (this.autoScale) {
                if (ColumnText.hasMoreText(status)) { //no more column                                    
                    float precision = 0.85f;
                    float min = 0;
                    float max = maxFontSize;
                    float size = maxFontSize;
                    for (int k = 0; k < 50; ++k) { //just in case it doesn't converge
                        size = (min + max) * precision;
                        iRec = new Rectangle(
                                iRec.getLeft() + this.paddingLeft,
                                0,
                                iRec.getRight() + this.paddingRight + 50,
                                iRec.getTop() + 10);
                        this.textFinder.width = (int) iRec.getRight();
                        this.textFinder.height = (int) iRec.getTop();
                        this.position.setRight(this.position.getRight() + 50);
                        this.position.setTop(this.position.getTop() + 10);
                        switch (imageProfile) {
                            case IMAGE_LEFT:
                                imageProfileLeft();
                                break;
                            case IMAGE_RIGHT:
                                imageProfileRight();
                                break;
                            case IMAGE_BOTTOM:
                                imageProfileBottom();
                                break;
                            case IMAGE_BOTTOM_TEXT_BOTTOM:
                                imageProfileBottom();
                                break;
                            case IMAGE_TOP:
                                imageProfileTop();
                                break;
                            case IMAGE_TOP_TEXT_TOP:
                                imageProfileTop();
                                break;
                            case IMAGE_CENTER:
                                imageProfileCenter();
                                break;
                            default:
                                iRec = new Rectangle(
                                        (position.getWidth() / 2 - (position.getWidth() * 0.9f) / 2) + this.paddingLeft,
                                        position.getHeight() / 2 - (position.getHeight() * 0.9f) / 2,
                                        (position.getWidth() / 2 + (position.getWidth() * 0.9f) / 2) - this.paddingRight,
                                        position.getHeight() / 2 + (position.getHeight() * 0.9f) / 2);
                        }
                        ct = new ColumnText(null);
                        font.setSize(size);
                        ct.setSimpleColumn(new Phrase(text, font), iRec.getLeft(), iRec.getBottom(), iRec.getRight(), iRec.getTop(), size, Element.ALIGN_LEFT);
                        ct.setRunDirection(runDirection);
                        status = ct.go(true);
                        if (status == ColumnText.NO_MORE_TEXT) {
                            return size;
                        } else {
                            max = size;
                        }
                    }
                    return size;
                }
            }

            float precision = 0.95f;
            float max = maxFontSize;
            float size = maxFontSize;
            for (int k = 0; k < 50; ++k) { //just in case it doesn't converge                
                size = max * precision;
//                System.out.println("FitText - Size:" + size);
                if (size <= fontSizeMin) {
                    return fontSizeMin;
                }
                ct = new ColumnText(null);
                font.setSize(size);
                Phrase q = new Phrase(text, font);
                ct.setSimpleColumn(q, rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop(), size, Element.ALIGN_LEFT);
                ct.setLeading(0, 1.5f);
                ct.setRunDirection(runDirection);
                status = ct.go(true);
//                System.out.println("Status1:" + (status == ColumnText.NO_MORE_TEXT));
//                System.out.println("Status2:" + (ct.getExtraParagraphSpace() >= 0));
//                System.out.println("MAXLINE:"+maxLine);
//                System.out.println("Line:" + ct.getLinesWritten());
//                System.out.println("Spacing:" + (ct.getCurrentLeading() - size));
//                System.out.println("Rect Height:" + rect.getHeight());
//                System.out.println("Status3:" + (((ct.getLinesWritten() * size) + ((ct.getCurrentLeading() - size) * (ct.getLinesWritten() - 1))) <= rect.getHeight()));
//                System.out.println("============");
                if ( //                        (status == ColumnText.NO_MORE_TEXT) &&
                        ct.getLinesWritten() >= maxLine
                        && ct.getExtraParagraphSpace() >= 0
                        && (((ct.getLinesWritten() * size) + ((ct.getCurrentLeading() - size) * (ct.getLinesWritten() - 1))) <= rect.getHeight())) {
                    return size;
//                if ((status & ColumnText.NO_MORE_TEXT) != 0) {                    
//                    if (max - min < size * precision) {
//                        return size;
//                    }
//                    min = size / 2 ;
                } else {
                    max = size;
                }
            }
            return size;
        } catch (DocumentException ex) {
            log.error("fitText", ex);
            throw new ExceptionConverter(ex);
        }
    }

    protected byte[] enrichLT(PdfReader reader) throws Exception {
        try {
            java.security.Security.addProvider(new BouncyCastleProvider());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfStamperMI stp = new PdfStamperMI(reader, baos, '\0', true);
            LtvVerificationMI v = stp.getLtvVerification();

            List<byte[]> ocspList = null;
            if (ocsp != null) {
                ocspList = new ArrayList<>();
                ocspList.add(ocsp);
            }

            List<byte[]> certList = new ArrayList<>();
            for (X509Certificate certificate : certificates) {
                certList.add(certificate.getEncoded());
            }

            v.addVerification(
                    signatureId,
                    ocspList,
                    crls,
                    certList
            );

            stp.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new Exception("Can't enrichLT", ex);
        }
    }

    protected byte[] enrichLTA(PdfReader reader) throws Exception {
        try {
            java.security.Security.addProvider(new BouncyCastleProvider());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfStamperMI stp = PdfStamperMI.createSignature(reader, baos, '\0', null, true);
//        PdfStamperMI stp = new PdfStamperMI(reader, baos, '\0', true);
            LtvVerificationMI v = stp.getLtvVerification();
//            AcroFields fields = stp.getAcroFields();
//            List<String> names = fields.getSignatureNames();
//            String sigName = names.get(names.size() - 1);
//            PdfPKCS7 pkcs7 = fields.verifySignature(sigName);

            List<byte[]> ocspList = null;
            if (ocsp != null) {
                ocspList = new ArrayList<>();
                ocspList.add(ocsp);
            }

            List<byte[]> certList = new ArrayList<>();
            for (X509Certificate certificate : certificates) {
                certList.add(certificate.getEncoded());
            }

//        if (pkcs7.isTsp()) {
//            v.addVerification(sigName, ocsp, crl,
//                    LtvVerification.CertificateOption.SIGNING_CERTIFICATE,
//                    LtvVerification.Level.OCSP_CRL,
//                    LtvVerification.CertificateInclusion.NO);
//        } else {
//            for (String name : names) {
//                v.addVerification(name, ocsp, crl,
//                        LtvVerification.CertificateOption.WHOLE_CHAIN,
//                        LtvVerification.Level.OCSP_CRL,
//                        LtvVerification.CertificateInclusion.NO);
//            }
//        }
            v.addVerification(
                    signatureId,
                    ocspList,
                    crls,
                    certList
            );

            TSAClient tsaClient = null;
            if (form.isTsa()) {
                tsaClient = new TSAClientBouncyCastle(tsaData[0], tsaData[1], tsaData[2], 8192, algorithm.getValue());
            }
            PdfSignatureAppearanceMI sap = stp.getSignatureAppearance();
            LtvTimestampMI.timestamp(sap, tsaClient, null);

            stp.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new Exception("Can't enrichLTA", ex);
        }
    }

    //update 03/08/2021
    protected void initPosition(PdfReader reader) throws Exception {
        try {
            totalNumOfPages = reader.getNumberOfPages();
            if (pageAndPosition == null) {
                if (textFinder == null) {
                    if (signingPageInt == 0) {
                        signingPageInt = totalNumOfPages;
                    }
                    if (signingPageInt > totalNumOfPages) {
                        throw new Exception("Page " + signingPageInt + " not found");
                    }
                } else {
                    TextPlusXY textPlusXY = null;
                    StringCoordinatesExtraction stringCoordinatesExtraction = new StringCoordinatesExtraction(reader);
                    if (textFinder.page != -1) {
                        //tim tren 1 trang xac dinh
                        if (textFinder.page == 0) {
                            signingPageInt = totalNumOfPages; // last
                        } else {
                            signingPageInt = textFinder.page;
                            if (signingPageInt > totalNumOfPages) {
                                throw new Exception("Page " + signingPageInt + " not found");
                            }
                        }
                        textPlusXY = stringCoordinatesExtraction.getCoordinate(textFinder.text, signingPageInt);
                        if (textPlusXY == null) {
                            throw new Exception("TEXT [" + textFinder.text + "] not found");
                        }
                    } else {
                        //tim tat ca cac trang
                        //update 03/08/2021
                        List<TextPlusXY> textPlusXYs = stringCoordinatesExtraction.getCoordinate(textFinder.text);
                        if (textPlusXYs.isEmpty()) {
                            throw new Exception("TEXT [" + textFinder.text + "] not found");
                        }
                        textPlusXY = textPlusXYs.get(0);
                        signingPageInt = textPlusXY.getPage();
                        //update 03/08/2021
                        if (textFinder.placeAll) {
                            try {
                                for (int i = 1; i < textPlusXYs.size(); i++) {
                                    TextPlusXY otherTextPlusXY = textPlusXYs.get(i);
                                    int detectedLowerX = (int) Math.ceil(otherTextPlusXY.getX());
                                    int detectedLowerY = (int) Math.ceil(otherTextPlusXY.getY());
                                    detectedLowerX += textFinder.offsetX;
                                    detectedLowerY += textFinder.offsetY;
                                    int calculatedUpperX = (int) (detectedLowerX + textFinder.width);
                                    int calculatedUpperY = (int) (detectedLowerY + textFinder.height);
                                    sigPosList.add(new SignaturePosition(otherTextPlusXY.getPage(), new Rectangle(detectedLowerX, detectedLowerY, calculatedUpperX, calculatedUpperY)));
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    if (textPlusXY == null) {
                        throw new NullPointerException("The text : '" + textFinder.text + "' not found ");
                    }

                    int detectedLowerX = (int) Math.ceil(textPlusXY.getX());
                    int detectedLowerY = (int) Math.ceil(textPlusXY.getY());

                    detectedLowerX += textFinder.offsetX;
                    detectedLowerY += textFinder.offsetY;

                    int calculatedUpperX = (int) (detectedLowerX + textFinder.width);
                    int calculatedUpperY = (int) (detectedLowerY + textFinder.height);
                    position = new Rectangle(detectedLowerX, detectedLowerY, calculatedUpperX, calculatedUpperY);
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
            } else {
                Map<Integer, String> pageAndPositionAsTreeMap = new TreeMap<>(pageAndPosition);

                boolean checkCountVariable = false;
                int countVariable = 0;
                if (totalNumOfPages < pageAndPositionAsTreeMap.size()) {
                    checkCountVariable = true;
                }

                for (Integer pageNo : pageAndPositionAsTreeMap.keySet()) {
                    if (pageNo == 0 || pageNo > totalNumOfPages) {
                        throw new Exception("Invalid pageNo: " + pageNo);
                    }

                    String[] vPosition = pageAndPositionAsTreeMap.get(pageNo).replace("\n", "").replace("\t", "").replace(" ", "").split(",");

                    if (vPosition.length != 2) {
                        throw new Exception("Invalid position parameter");
                    }

                    float[] fPosition = new float[2];
                    fPosition[0] = Float.parseFloat(vPosition[0]);
                    fPosition[1] = Float.parseFloat(vPosition[1]);

                    int detectedLowerX = (int) Math.ceil(fPosition[0]);
                    int detectedLowerY = (int) Math.ceil(fPosition[1]);
                    //detectedLowerX += textFinder.offsetX; //no offset
                    //detectedLowerY += textFinder.offsetY; //no offset
                    int calculatedUpperX = (int) (detectedLowerX + boxSize[0]);
                    int calculatedUpperY = (int) (detectedLowerY + boxSize[1]);
                    sigPosList.add(new SignaturePosition(pageNo, new Rectangle(detectedLowerX, detectedLowerY, calculatedUpperX, calculatedUpperY)));
                    countVariable++;
                    if (checkCountVariable) {
                        if (countVariable == totalNumOfPages) {
                            break;
                        }
                    }
                }
                signingPageInt = sigPosList.get(0).getPage();
                position = sigPosList.get(0).getRectangle();
                //remove first element
                sigPosList.remove(0);
            }
        } catch (Exception ex) {
//            ex.printStackTrace();
            throw new Exception("Can't init signature position", ex);
        }
    }

    protected void initContent(Date date, X509Certificate cert) throws Exception {
        try {
            SimpleDateFormat format = new SimpleDateFormat(timeFormat);
            textContent = textContent.replace("{location}", location);
            textContent = textContent.replace("{reason}", reason);
            textContent = textContent.replace("{date}", format.format(date));
            textContent = textContent.replace("{signby}", getCertificateInfo(cert, "2.5.4.3"));
            textContent = textContent.replace("{organize}", getCertificateInfo(cert, "2.5.4.10"));
            textContent = textContent.replace("{organizationunit}", getCertificateInfo(cert, "2.5.4.11"));
            textContent = textContent.replace("{email}", getCertificateInfo(cert, "1.2.840.113549.1.9.1"));
            textContent = textContent.replace("{phone}", getCertificateInfo(cert, "2.5.4.20"));

            textContent = textContent.replace("{title}", getCertificateInfo(cert, "2.5.4.12"));
            textContent = textContent.replace("{givenname}", getCertificateInfo(cert, "2.5.4.42"));
            textContent = textContent.replace("{serialnumber}", getSerialNumber(cert));
            textContent = textContent.replace("{personalid}", getPersonalID(cert));
            textContent = textContent.replace("{enterpriseid}", getEnterpriseID(cert));
        } catch (UnsupportedEncodingException | InvalidNameException ex) {
            throw new Exception("Can't prepare text content", ex);
        }
    }

    protected void imageProfileLeft() {
        image.scaleToFit((position.getRight() - position.getLeft()) / 2 - imageProfile.border * 2,
                position.getTop() - position.getBottom() - imageProfile.border * 2
        );
        image.setAbsolutePosition(0, 0);
        iRec = new Rectangle(
                image.getAbsoluteX() + image.getScaledWidth() + imageProfile.border + this.paddingLeft,
                image.getAbsoluteY(),
                position.getWidth() - this.paddingRight,
                position.getHeight());
    }

    protected void imageProfileRight() {
        image.scaleToFit((position.getRight() - position.getLeft()) / 2 - imageProfile.border * 2,
                position.getTop() - position.getBottom() - imageProfile.border * 2
        );
        image.setAbsolutePosition(position.getWidth() - image.getScaledWidth(), 0);
        iRec = new Rectangle(
                0 + this.paddingLeft,
                0,
                position.getWidth() - image.getScaledWidth() - imageProfile.border - this.paddingRight,
                position.getHeight());
    }

    protected void imageProfileBottom() {
        image.scaleToFit(position.getWidth() - imageProfile.border * 2,
                position.getHeight() / 2 - imageProfile.border * 2
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

    protected PdfPTable createImage(Font font) throws Exception {
        try {
            PdfPTable table = new PdfPTable(1);

            if (lineSpacing == 0) {
                lineSpacing = 1f;
            }

            if (background != null) {
                background.scaleAbsolute(position);
                background.setAbsolutePosition((position.getRight() - position.getLeft()) / 2 - background.getScaledWidth() / 2,
                        (position.getTop() - position.getBottom()) / 2 - background.getScaledHeight() / 2);
            }

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
                            imageProfileBottom();
                            break;
                        case IMAGE_BOTTOM_TEXT_BOTTOM:
                            imageProfileBottom();
                            break;
                        case IMAGE_TOP:
                            imageProfileTop();
                            break;
                        case IMAGE_TOP_TEXT_TOP:
                            imageProfileTop();
                            break;
                        case IMAGE_CENTER:
                            imageProfileCenter();
                            break;
                        default:
                            iRec = new Rectangle(
                                    (position.getWidth() / 2 - (position.getWidth() * 0.9f) / 2) + this.paddingLeft,
                                    position.getHeight() / 2 - (position.getHeight() * 0.9f) / 2,
                                    (position.getWidth() / 2 + (position.getWidth() * 0.9f) / 2) - this.paddingRight,
                                    position.getHeight() / 2 + (position.getHeight() * 0.9f) / 2);
                    }

                    table.setSpacingAfter(0);
                    table.setSpacingBefore(0);
                    table.setWidthPercentage(100);
                    table.setWidths(new int[]{1});

                    PdfPCell textCell = new PdfPCell();
                    float finalFontSize = -1;
                    while (finalFontSize <= 0) {
                        finalFontSize = fitText(font, textContent, iRec, font.getSize(), PdfWriter.RUN_DIRECTION_DEFAULT);
//                        finalFontSize = fitText(font, textContent, iRec, font.getCalculatSize(), PdfWriter.RUN_DIRECTION_DEFAULT);
//                        finalFontSize = fitText(font, textContent, iRec, 20, PdfWriter.RUN_DIRECTION_DEFAULT);
                    }
                    System.out.println("FinalSize:" + finalFontSize);
                    font.setSize(finalFontSize);
                    textCell.setBorder(Rectangle.NO_BORDER);
                    textCell.setNoWrap(false);
                    textCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                    textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    textCell.setFixedHeight(iRec.getTop() - iRec.getBottom());

                    //Update 09/03/2021
                    Paragraph par = new Paragraph();
                    if (!containsHTMLChars(textContent)) {
                        par.add(new Phrase(new Chunk(textContent, font)));
                    } else {
                        int index = 0;
                        List<String> array = new ArrayList<>();
                        String temporaryTextContent = textContent;
                        while (index != -1) {
                            index = temporaryTextContent.indexOf(HTML_BREAK);
                            if (index == -1) {
                                array.add(temporaryTextContent);
                                break;
                            }
                            String s = temporaryTextContent.substring(0, index);
                            temporaryTextContent = temporaryTextContent.substring(index + HTML_BREAK.length());
                            array.add(s);
                        }
                        if (array.size() == 1) {
                            processParagraph(par, array.get(0));
                        } else {
                            for (int i = 0; i < array.size(); i++) {
                                processParagraph(par, array.get(i));
                                par.add("\n");
                            }
                        }
                    }
                    if (textAlignment == null) {
                        par.setAlignment(imageProfile.textAlign);
                    } else {
                        par.setAlignment(textAlignment.value);
                    }

//                    par.setLeading(0.1f, lineSpacing);
                    if (lineSpacing > 0) {
                        par.setMultipliedLeading(lineSpacing);
                    }
                    textCell.addElement(par);
                    table.addCell(textCell);

                } else {
                    iRec = new Rectangle(
                            0 + this.paddingLeft,
                            0,
                            position.getWidth() + this.paddingRight,
                            position.getHeight());
                    table.setSpacingAfter(0);
                    table.setSpacingBefore(0);
                    table.setWidthPercentage(100);
                    table.setWidths(new int[]{1});

                    PdfPCell textCell = new PdfPCell();
                    float finalFontSize = 0;
                    while (finalFontSize < 1) {
                        finalFontSize = fitText(font, textContent, iRec, font.getCalculatedSize(), PdfWriter.RUN_DIRECTION_DEFAULT);
                    }
                    System.out.println("FinalFontSize:" + finalFontSize);
                    font.setSize(finalFontSize);
                    textCell.setBorder(Rectangle.NO_BORDER);
                    textCell.setNoWrap(false);
                    textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    textCell.setFixedHeight(iRec.getTop() - iRec.getBottom());

                    Paragraph par = new Paragraph();
                    if (!containsHTMLChars(textContent)) {
                        par.add(new Phrase(new Chunk(textContent, font)));
                    } else {
                        int index = 0;
                        List<String> array = new ArrayList<>();
                        String temporaryTextContent = textContent;
                        while (index != -1) {
                            index = temporaryTextContent.indexOf(HTML_BREAK);
                            if (index == -1) {
                                array.add(temporaryTextContent);
                                break;
                            }
                            String s = temporaryTextContent.substring(0, index);
                            temporaryTextContent = temporaryTextContent.substring(index + HTML_BREAK.length());
                            array.add(s);
                        }
                        if (array.size() == 1) {
                            processParagraph(par, array.get(0));
                        } else {
                            for (int i = 0; i < array.size(); i++) {
                                processParagraph(par, array.get(i));
                                par.add("\n");
                            }
                        }
                    }
                    if (textAlignment == null) {
                        par.setAlignment(Element.ALIGN_LEFT);
                    } else {
                        par.setAlignment(textAlignment.value);
                    }
                    par.setLeading(0.1f, lineSpacing);
                    textCell.addElement(par);
                    table.addCell(textCell);
                }
            }
            return table;
        } catch (DocumentException | FontFormatException | IOException | InvalidNameException ex) {
            throw new Exception("Can't generate signature text box", ex);
        }
    }

    protected PdfPTable createImage_V2(Font font) throws Exception {
        try {
            PdfPTable table = new PdfPTable(1);

            if (lineSpacing == 0) {
                lineSpacing = 1f;
            }

            if (background != null) {
                background.scaleAbsolute(position);
                background.setAbsolutePosition((position.getRight() - position.getLeft()) / 2 - background.getScaledWidth() / 2,
                        (position.getTop() - position.getBottom()) / 2 - background.getScaledHeight() / 2);
            }

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
                            imageProfileBottom();
                            break;
                        case IMAGE_BOTTOM_TEXT_BOTTOM:
                            imageProfileBottom();
                            break;
                        case IMAGE_TOP:
                            imageProfileTop();
                            break;
                        case IMAGE_TOP_TEXT_TOP:
                            imageProfileTop();
                            break;
                        case IMAGE_CENTER:
                            imageProfileCenter();
                            break;
                        default:
                            iRec = new Rectangle(
                                    (position.getWidth() / 2 - (position.getWidth() * 0.9f) / 2) + this.paddingLeft,
                                    position.getHeight() / 2 - (position.getHeight() * 0.9f) / 2,
                                    (position.getWidth() / 2 + (position.getWidth() * 0.9f) / 2) - this.paddingRight,
                                    position.getHeight() / 2 + (position.getHeight() * 0.9f) / 2);
                    }

                    table.setSpacingAfter(0);
                    table.setSpacingBefore(0);
                    table.setWidthPercentage(100);
                    table.setWidths(new int[]{1});

                    PdfPCell textCell = new PdfPCell();
                    float finalFontSize = -1;
                    while (finalFontSize <= 0) {
                        finalFontSize = fitText(font, textContent, iRec, font.getSize(), PdfWriter.RUN_DIRECTION_DEFAULT);
                    }
                    System.out.println("FinalSize:" + finalFontSize);
                    font.setSize(finalFontSize);
                    textCell.setBorder(Rectangle.NO_BORDER);
                    textCell.setNoWrap(false);
                    textCell.setVerticalAlignment(textProfile.vertical);
                    textCell.setHorizontalAlignment(textProfile.horizontal);
                    textCell.setFixedHeight(iRec.getTop() - iRec.getBottom());

                    //Update 09/03/2021
                    Paragraph par = new Paragraph();
                    if (!containsHTMLChars(textContent)) {
                        par.add(new Phrase(new Chunk(textContent, font)));
                    } else {
                        int index = 0;
                        List<String> array = new ArrayList<>();
                        String temporaryTextContent = textContent;
                        while (index != -1) {
                            index = temporaryTextContent.indexOf(HTML_BREAK);
                            if (index == -1) {
                                array.add(temporaryTextContent);
                                break;
                            }
                            String s = temporaryTextContent.substring(0, index);
                            temporaryTextContent = temporaryTextContent.substring(index + HTML_BREAK.length());
                            array.add(s);
                        }
                        if (array.size() == 1) {
                            processParagraph(par, array.get(0));
                        } else {
                            for (int i = 0; i < array.size(); i++) {
                                processParagraph(par, array.get(i));
                                par.add("\n");
                            }
                        }
                    }
                    if (textAlignment == null) {
                        par.setAlignment(imageProfile.textAlign);
                    } else {
                        par.setAlignment(textAlignment.value);
                    }

//                    par.setLeading(0.1f, lineSpacing);
                    if (lineSpacing > 0) {
                        par.setMultipliedLeading(lineSpacing);
                    }
                    textCell.addElement(par);
                    table.addCell(textCell);

                } else {
                    iRec = new Rectangle(
                            0 + this.paddingLeft,
                            0,
                            position.getWidth() + this.paddingRight,
                            position.getHeight());
                    table.setSpacingAfter(0);
                    table.setSpacingBefore(0);
                    table.setWidthPercentage(100);
                    table.setWidths(new int[]{1});

                    PdfPCell textCell = new PdfPCell();
                    float finalFontSize = 0;
                    while (finalFontSize < 1) {
                        finalFontSize = fitText(font, textContent, iRec, font.getCalculatedSize(), PdfWriter.RUN_DIRECTION_DEFAULT);
                    }
                    System.out.println("FinalFontSize:" + finalFontSize);
                    font.setSize(finalFontSize);
                    textCell.setBorder(Rectangle.NO_BORDER);
                    textCell.setNoWrap(false);
                    textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    textCell.setFixedHeight(iRec.getTop() - iRec.getBottom());

                    Paragraph par = new Paragraph();
                    if (!containsHTMLChars(textContent)) {
                        par.add(new Phrase(new Chunk(textContent, font)));
                    } else {
                        int index = 0;
                        List<String> array = new ArrayList<>();
                        String temporaryTextContent = textContent;
                        while (index != -1) {
                            index = temporaryTextContent.indexOf(HTML_BREAK);
                            if (index == -1) {
                                array.add(temporaryTextContent);
                                break;
                            }
                            String s = temporaryTextContent.substring(0, index);
                            temporaryTextContent = temporaryTextContent.substring(index + HTML_BREAK.length());
                            array.add(s);
                        }
                        if (array.size() == 1) {
                            processParagraph(par, array.get(0));
                        } else {
                            for (int i = 0; i < array.size(); i++) {
                                processParagraph(par, array.get(i));
                                par.add("\n");
                            }
                        }
                    }
                    if (textAlignment == null) {
                        par.setAlignment(Element.ALIGN_LEFT);
                    } else {
                        par.setAlignment(textAlignment.value);
                    }
                    par.setLeading(0.1f, lineSpacing);
                    textCell.addElement(par);
                    table.addCell(textCell);
                }
            }
            return table;
        } catch (DocumentException | FontFormatException | IOException | InvalidNameException ex) {
            throw new Exception("Can't generate signature text box", ex);
        }
    }

    @Override
    List<byte[]> appendSignautre(List<String> signatureList) throws Exception {
        X509Certificate[] cert = this.certificates.toArray(new X509Certificate[certificates.size()]);
        List<byte[]> result = new ArrayList<>();
        TSAClient tsaClient = null;
        if (form.isTsa()) {
            tsaClient = new TSAClientBouncyCastle(tsaData[0], tsaData[1], tsaData[2], 8192, algorithm.getValue());
        }

        for (int i = 0; i < tempDataList.size(); i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BouncyCastleDigest digest = new BouncyCastleDigest();
            PdfPKCS7 sgn = new PdfPKCS7(null, cert, algorithm.getValue(), null, digest, false);
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
                    MakeSignature.CryptoStandard.CADES);
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
                if (!form.isLTA()) {
                    completeData = enrichLT(reader);
                } else {
                    completeData = enrichLTA(reader);
                }
                reader.close();
            }
            result.add(completeData);

        }
        return result;
    }

    @Override
    void generateHash(List<byte[]> dataToBeSign) throws Exception {

        if (form.isRevocation()) {
            if (certificates.size() >= 2 && crls.isEmpty()) {
                OcspClient ocspClient = new OcspClientBouncyCastle();
                ocsp = ocspClient.getEncoded(certificates.get(0), certificates.get(1), null);
                if (ocsp != null) {
                    ltvSize = ltvSize + ocsp.length;
                }
            }
        }

        if (rootCertificate && !form.isRevocation()) {
            certificates = Utils.getCertPath(certificates.get(0));
        }

        Calendar signingTime = Calendar.getInstance();
        signingTime.setTimeInMillis(timeMillis);
        Date date = signingTime.getTime();

        signatureId = "sig-"
                + Calendar.getInstance().getTimeInMillis()
                + "-"
                + getCertificateInfo(certificates.get(0), "CN");

        Font font = null;

        if (position != null || textFinder != null || pageAndPosition != null) {
            try {
                if (this.baseFont == null && fontName != null) {
                    baseFont = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                }
                font = new Font(baseFont, fontSize, Font.NORMAL, textColor);
                initContent(date, certificates.get(0));
            } catch (DocumentException | IOException ex) {
                throw new Exception("Can't load content", ex);
            }
        }

        for (int i = 0; i < dataToBeSign.size(); i++) {
            sigPosList.clear(); // FIX MULTIPLE FILE, DIFFERENT PAGES NUMBER
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
            if (position != null || textFinder != null || pageAndPosition != null) {

                initPosition(reader);
                sigTable = createImage(font);

                try {
                    appearance.setVisibleSignature(position, signingPageInt, signatureId);
                } catch (Exception ex) {
                    signatureId = "sig-" + Calendar.getInstance().getTimeInMillis();
                    appearance.setVisibleSignature(position, signingPageInt, signatureId);
                }
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
                //Update 09/03/2021 fix ImageProfile.CENTER
                if (image != null) {
                    n2.addImage(image);
                }

                if (!dsImage) {
                    ColumnText ct = new ColumnText(n2);
                    ct.setSimpleColumn(iRec);
                    ct.setExtraParagraphSpace(0);
                    ct.setLeading(0);
                    ct.addElement(sigTable);
                    ct.go();
                }

                PdfTemplate n0 = appearance.getLayer(0);

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

                if (background != null) {
                    n0.addImage(background);
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

            ExternalSignatureContainer external = new ExternalBlankSignatureContainer(PdfName.ADOBE_PPKLITE, PdfName.ETSI_CADES_DETACHED);
            MakeSignatureMI.signExternalContainer(appearance, external, 10240 + ltvSize + tsaSize);
            tempDataList.add(baos.toByteArray());
        }

        X509Certificate[] cert = this.certificates.toArray(new X509Certificate[certificates.size()]);
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
            PdfPKCS7 sgn = new PdfPKCS7(null, cert, algorithm.getValue(), null, digest, false);
            byte[] hash = DigestAlgorithms.digest(rg, digest.getMessageDigest(algorithm.getValue()));
            otherList.add(hash);
            byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, ocsp, crls, MakeSignature.CryptoStandard.CADES);
            byte[] hashData = DigestAlgorithms.digest(new ByteArrayInputStream(sh), digest.getMessageDigest(algorithm.getValue()));
            hashList.add(new String(Base64.encode(hashData)));
        }
    }

    public byte[] createTemporalFile(SigningMethodAsync signingMethod, List<byte[]> dataToBeSign, List<String> passwordList) throws Exception {
        this.passwordList = passwordList;
        return createTemporalFile(signingMethod, dataToBeSign);
    }

    public List<byte[]> sign(SigningMethodSync signingMethod, List<byte[]> dataToBeSign, List<String> passwordList) throws Exception {
        this.passwordList = passwordList;
        return sign(signingMethod, dataToBeSign);
    }

    public static List<VerifyResult> verify(byte[] data, String password, boolean certificateStatusEnabled) throws Exception {
        PdfVerify pdfVerify = new PdfVerify();
        return pdfVerify.verifySignature(data, password, certificateStatusEnabled);
    }

    public static List<VerifyResult> verify(byte[] data, boolean certificateStatusEnabled) throws Exception {
        PdfVerify pdfVerify = new PdfVerify();
        return pdfVerify.verifySignature(data, null, certificateStatusEnabled);
    }

    public static boolean verify(byte[] data, String password) throws IOException, GeneralSecurityException {
        try {
            PdfReaderV4.unethicalreading = true;
            PdfReaderV4 reader;
            if (password == null) {
                reader = new PdfReaderV4(data);
            } else {
                reader = new PdfReaderV4(data, password.getBytes());
            }
            BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);
            AcroFieldsV4 acroFields = reader.getAcroFields_v4();
            List<String> signatureNames = acroFields.getSignatureNames();
            if (signatureNames != null || !signatureNames.isEmpty()) {
                for (String name : signatureNames) {
                    PdfPKCS7 pkcs = acroFields.verifySignature(name, provider.getName());
                    if (!pkcs.verify()) {
                        return false;
                    }
                }
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    protected boolean containsHTMLChars(String content) {
        if (content.contains(HTML_BREAK)
                || content.contains(HTML_FONT_STYLE_BOLD_BEGIN)
                || content.contains(HTML_FONT_STYLE_BOLD_END)
                || content.contains(HTML_FONT_STYLE_ITALIC_BEGIN)
                || content.contains(HTML_FONT_STYLE_ITALIC_END)
                || content.contains(HTML_FONT_STYLE_BOLD_ITALIC_BEGIN)
                || content.contains(HTML_FONT_STYLE_BOLD_ITALIC_END)
                || content.contains(HTML_FONT_STYLE_UNDERLINE_BEGIN)
                || content.contains(HTML_FONT_STYLE_UNDERLINE_END)) {
            return true;
        }
        return false;
    }

    protected void processParagraph(Paragraph par, String textContent) {
        if (this.baseFont == null
                && fontName != null) {
            try {
                baseFont = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception e) {
                throw new RuntimeException("Cannot createFont " + fontName);
            }
        }
        if (textContent.indexOf(HTML_FONT_STYLE_BOLD_BEGIN) != -1
                && textContent.indexOf(HTML_FONT_STYLE_BOLD_END) != -1) {
            int i = textContent.indexOf(HTML_FONT_STYLE_BOLD_BEGIN);
            String head = textContent.substring(0, i);
            int j = textContent.indexOf(HTML_FONT_STYLE_BOLD_END);
            String tail = textContent.substring(j + HTML_FONT_STYLE_BOLD_END.length());
            String body = textContent.substring(i + HTML_FONT_STYLE_BOLD_BEGIN.length(), j);
            par.add(new Phrase(new Chunk(head, new Font(baseFont, fontSize, Font.NORMAL, textColor))));
            par.add(new Phrase(new Chunk(body, new Font(baseFont, fontSize, Font.BOLD, textColor))));
            par.add(new Phrase(new Chunk(tail, new Font(baseFont, fontSize, Font.NORMAL, textColor))));
        } else if (textContent.indexOf(HTML_FONT_STYLE_ITALIC_BEGIN) != -1
                && textContent.indexOf(HTML_FONT_STYLE_ITALIC_END) != -1) {
            int i = textContent.indexOf(HTML_FONT_STYLE_ITALIC_BEGIN);
            String head = textContent.substring(0, i);
            int j = textContent.indexOf(HTML_FONT_STYLE_ITALIC_END);
            String tail = textContent.substring(j + HTML_FONT_STYLE_ITALIC_END.length());
            String body = textContent.substring(i + HTML_FONT_STYLE_ITALIC_BEGIN.length(), j);
            par.add(new Phrase(new Chunk(head, new Font(baseFont, fontSize, Font.NORMAL, textColor))));
            par.add(new Phrase(new Chunk(body, new Font(baseFont, fontSize, Font.ITALIC, textColor))));
            par.add(new Phrase(new Chunk(tail, new Font(baseFont, fontSize, Font.NORMAL, textColor))));
        } else if (textContent.indexOf(HTML_FONT_STYLE_BOLD_ITALIC_BEGIN) != -1
                && textContent.indexOf(HTML_FONT_STYLE_BOLD_ITALIC_END) != -1) {
            int i = textContent.indexOf(HTML_FONT_STYLE_BOLD_ITALIC_BEGIN);
            String head = textContent.substring(0, i);
            int j = textContent.indexOf(HTML_FONT_STYLE_BOLD_ITALIC_END);
            String tail = textContent.substring(j + HTML_FONT_STYLE_BOLD_ITALIC_END.length());
            String body = textContent.substring(i + HTML_FONT_STYLE_BOLD_ITALIC_BEGIN.length(), j);
            par.add(new Phrase(new Chunk(head, new Font(baseFont, fontSize, Font.NORMAL, textColor))));
            par.add(new Phrase(new Chunk(body, new Font(baseFont, fontSize, Font.BOLDITALIC, textColor))));
            par.add(new Phrase(new Chunk(tail, new Font(baseFont, fontSize, Font.NORMAL, textColor))));
        } else if (textContent.indexOf(HTML_FONT_STYLE_UNDERLINE_BEGIN) != -1
                && textContent.indexOf(HTML_FONT_STYLE_UNDERLINE_END) != -1) {
            int i = textContent.indexOf(HTML_FONT_STYLE_UNDERLINE_BEGIN);
            String head = textContent.substring(0, i);
            int j = textContent.indexOf(HTML_FONT_STYLE_UNDERLINE_END);
            String tail = textContent.substring(j + HTML_FONT_STYLE_UNDERLINE_END.length());
            String body = textContent.substring(i + HTML_FONT_STYLE_UNDERLINE_BEGIN.length(), j);
            par.add(new Phrase(new Chunk(head, new Font(baseFont, fontSize, Font.NORMAL, textColor))));
            par.add(new Phrase(new Chunk(body, new Font(baseFont, fontSize, Font.UNDERLINE, textColor))));
            par.add(new Phrase(new Chunk(tail, new Font(baseFont, fontSize, Font.NORMAL, textColor))));
        } else {
            par.add(new Phrase(new Chunk(textContent, new Font(baseFont, fontSize, Font.NORMAL, textColor))));
        }
    }

    private String getPersonalID(X509Certificate cert) {
        String result = "NULL";
        if (cert != null) {
            X500Name subject = new X500Name(cert.getSubjectDN().toString());
            RDN[] rdn = subject.getRDNs();
            for (int j = 0; j < rdn.length; j++) {
                AttributeTypeAndValue[] attributeTypeAndValue = rdn[j].getTypesAndValues();
                String value = attributeTypeAndValue[0].getValue().toString();
                if (value.contains(PREFIX_PERSONAL_CODE)) {
                    result = value.substring(PREFIX_PERSONAL_CODE.length());
                    break;
                }
                if (value.contains(PREFIX_PERSONAL_PASSPORT_CODE)) {
                    result = value.substring(PREFIX_PERSONAL_PASSPORT_CODE.length());
                    break;
                }
                if (value.contains(PREFIX_CITIZEN_CODE)) {
                    result = value.substring(PREFIX_CITIZEN_CODE.length());
                    break;
                }
            }
        }
        return result;
    }

    private String getEnterpriseID(X509Certificate cert) {
        String result = "NULL";
        if (cert != null) {
            X500Name subject = new X500Name(cert.getSubjectDN().toString());
            RDN[] rdn = subject.getRDNs();
            for (int j = 0; j < rdn.length; j++) {
                AttributeTypeAndValue[] attributeTypeAndValue = rdn[j].getTypesAndValues();
                String value = attributeTypeAndValue[0].getValue().toString();
                if (value.contains(PREFIX_ENTERPRISE_TAX_CODE)) {
                    result = value.substring(PREFIX_ENTERPRISE_TAX_CODE.length());
                    break;
                }
                if (value.contains(PREFIX_ENTERPRISE_BUDGET_CODE)) {
                    result = value.substring(PREFIX_ENTERPRISE_BUDGET_CODE.length());
                    break;
                }
            }
        }
        return result;
    }

    private String getSerialNumber(X509Certificate cert) {
        if (cert != null) {
            return DatatypeConverter.printHexBinary(cert.getSerialNumber().toByteArray()).toUpperCase();
        }
        return "NULL";
    }

    public BaseFont getBaseFont() {
        return this.baseFont;
    }

    public boolean checkPermission(List<byte[]> data) throws Exception {
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
            Map<String, AcroFields.Item> maps = acro.getFields();
            for (String key : maps.keySet()) {
                PdfDictionary v = acro.getSignatureDictionary(key);

                AcroFields.Item item = maps.get(key);
                PdfDictionary dict = item.getValue(0);

                if (v.contains(PdfName.REFERENCE)) {
                    PdfArray ref = v.getAsArray(PdfName.REFERENCE);
                    for (int j = 0; j < ref.size(); j++) {
                        PdfName method = ref.getAsDict(i).getAsName(PdfName.TRANSFORMMETHOD);
                        if (method == null) {
                            continue;
                        }
                        if (method.compareTo(PdfName.DOCMDP) == 0) {
                            PdfNumber num = ref.getAsDict(i).getAsDict(PdfName.TRANSFORMPARAMS).getAsNumber(PdfName.P);
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
                            PdfNumber num = ref.getAsDict(i).getAsDict(PdfName.TRANSFORMPARAMS).getAsNumber(PdfName.P);
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
    }

    /**
     * Using to check Lock field in PDFDictionary input.
     *
     * @param field PdfDictionary of which data wants to be check
     * @return true if PdfDict don't contain otherwise false.
     */
    private static boolean checkLock(PdfDictionary field) throws DocumentException {
        if (field == null) {
            throw new DocumentException("Field check is null!");
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
        return false;
    }

    public void setFontSizeMin(float fontSize) {
        this.fontSizeMin = fontSize;
    }
}

class PdfVerify {

    public List<VerifyResult> verifySignature(byte[] signedData, String password, boolean revocationEnabled) throws Exception {

        PdfReaderV4.unethicalreading = true;
        PdfReaderV4 reader;
        if (password == null) {
            reader = new PdfReaderV4(signedData);
        } else {
            reader = new PdfReaderV4(signedData, password.getBytes());
        }
        Security.addProvider(new BouncyCastleProvider());
        AcroFieldsV4 acroFields = reader.getAcroFields_v4();
        List<String> signatureNames = acroFields.getSignatureNames();

        //Update 2023-12-18 by GIATK
        List<String> signatureNames_ = getSignatureInPage(reader);
        if (signatureNames_ != null) {
            for (String name : signatureNames_) {
                if (!signatureNames.contains(name)) {
                    throw new SignatureNotInAcroformException("Signature not exist in Acroform!");
                }
            }
        }

        try {
            List<VerifyResult> verifyResults = new ArrayList<>();

            boolean isTsp = false;
            if (!signatureNames.isEmpty()) {
                for (int i = 0; i < signatureNames.size(); i++) {
                    Date dateTime = null;
                    String date = null;
                    String form = null;
                    String id = signatureNames.get(i);
                    String signingMethod = null;
                    X509Certificate x509 = null;
                    boolean valid = true;
                    try {
                        form = "CMS";
                        PdfPKCS7V4 pkcs7 = acroFields.verifySignature_v4(signatureNames.get(i), "BC");
//                        List<AcroFields_v4.FieldPosition> pos = acroFields.getFieldPositions(name);
                        if (!pkcs7.verify()) {
                            valid = false;
                        }

                        dateTime = pkcs7.getSignDate().getTime();
                        signingMethod = pkcs7.getHashAlgorithm();
                        x509 = pkcs7.getSigningCertificate();
                        try {
                            if (pkcs7.isCades) {
                                form = "PAdES-B";
                            } else {
                                throw new Exception("Complete");
                            }

                            if (pkcs7.isTsp()) {
                                isTsp = true;
                            }

//                        //get crl
//                        {      
//                            int i = 0;
//                            for (CRL crl : pkcs7.getCRLs()) {
//                                i++;
//                                byte[] crlByte= ((X509CRL) crl).getEncoded();
//                                try(FileOutputStream fos = new FileOutputStream("C:/Users/minhg/Downloads/"+name+"_"+i+".crl")){
//                                    fos.write(crlByte);
//                                }
//                            }
//                        }
//                        
//                        {      
//                            int i = 0;
//                            for (Certificate certificate : pkcs7.getCertificates()) {
//                                 i++;
//                                byte[] crlByte= ((X509Certificate) certificate).getEncoded();
//                                try(FileOutputStream fos = new FileOutputStream("C:/Users/minhg/Downloads/"+name+"_"+i+".crt")){
//                                    fos.write(crlByte);
//                                }
//                            }
//                        }
//                        int i = 0;
                            List<X509Certificate> x509Certs = new ArrayList<>();
                            List<X509CRL> x509Crls = new ArrayList<>();

                            if (pkcs7.verifyTimestampImprint()) {
                                form = "PAdES-T";
                                TimeStampToken token = pkcs7.getTimeStampToken();
                                dateTime = token.getTimeStampInfo().getGenTime();
                            }

                            boolean isLT = false;
                            if (pkcs7.isRevocationValid()) {
                                isLT = true;
                            }
                            if (pkcs7.getCertificates() != null && pkcs7.getCRLs() != null) {

                                for (Certificate certificate : pkcs7.getCertificates()) {
                                    x509Certs.add((X509Certificate) certificate);
                                }
                                for (CRL crl : pkcs7.getCRLs()) {
                                    x509Crls.add((X509CRL) crl);
                                }
                                x509Certs = Utils.getCertPath(x509, true, dateTime, x509Certs, x509Crls);
                                if (x509Certs != null && !x509Certs.isEmpty()) {
                                    isLT = true;
                                }
                            }
                            if (isLT) {
                                form = "PAdES-LT";
                            }

                        } catch (Exception ex) {

                        }

                        if (revocationEnabled && !"PAdES-LT".equals(form)) {
                            if (!pkcs7.isRevocationValid()) {
                                valid = false;
                                if (!Utils.getCertificatePath(x509).isEmpty()) {
                                    valid = true;
                                }
                            }
                        }

                        PdfDictionary sigDict = acroFields.getSignatureDictionary(signatureNames.get(i));
                        SignaturePermissions perms = new SignaturePermissions(sigDict, null);
                        if (perms.isCertification()) {
                            if (i != signatureNames.size() - 1) {
                                valid = false;
                            }
                        }
                    } catch (Exception ex) {
                    }

                    if (dateTime != null) {
                        date = Utils.timeMilsToString(dateTime.getTime());
                    }
                    verifyResults.add(new VerifyResult(id, form, x509, signingMethod, date, valid));
                }
            }
            if (isTsp) {
                for (int i = 0; i < verifyResults.size(); i++) {
                    VerifyResult verifyResult = verifyResults.get(i);
                    if (verifyResult.getSigningForm().equals("PAdES-LT")) {
                        verifyResult.setSigningForm("PAdES-LTA");
                        verifyResults.set(i, verifyResult);
                    }
                }
            }
            return verifyResults;
        } catch (Exception ex) {
            ex.printStackTrace();
//            throw new Exception("Can't init signature position", ex);
        }
        return null;
    }

    private static List<String> getSignatureInPage(PdfReader reader) {
        try {
            List<String> signatureNames = new ArrayList<>();
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                PdfDictionary page = reader.getPageN(i);
                PdfArray annots = page.getAsArray(PdfName.ANNOTS);
                ListIterator<PdfObject> lists = annots.listIterator();
                while (lists.hasNext()) {
                    PRIndirectReference reference = (PRIndirectReference) lists.next();
                    PdfDictionary dict = (PdfDictionary) PdfReaderV4.getPdfObject(reference);
                    try {
                        if (dict.getAsName(PdfName.FT).equals(PdfName.SIG)) {
                            signatureNames.add(dict.getAsString(PdfName.T).toUnicodeString());
                        }
                    } catch (Exception ex) {
                    }
                }
            }
            return signatureNames;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
