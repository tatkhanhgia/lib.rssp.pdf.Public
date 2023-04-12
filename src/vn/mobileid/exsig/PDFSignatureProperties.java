/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.exsig;

import com.itextpdf.text.BaseColor;
import java.util.Calendar;
import java.util.Map;

/**
 *
 * @author ADMIN
 */
public class PDFSignatureProperties {

    protected byte[] pdfData;
    protected String pdfPassword;
    protected String textContent;
    protected String reason;
    protected String signerCertificate;
    protected String timeFormat = "yyyy-MM-dd'T'HH:mm:ss";
    protected long timeMillis = Calendar.getInstance().getTimeInMillis();
    //setVisibleSignature
    protected String page;
    protected String offset;
    protected String boxSize;
    protected String text;
    protected boolean placeAll;
    protected Map<Integer, String> pageAndPosition;
    protected String position;
    protected int visibleSignatureType;
    //setImage
    protected byte[] image;
    protected ImageProfile imageProfile;
    protected ImageAlgin imageAlign;
    protected int imageType;
    //addLayer0Icon
    protected byte[] layer0Icon;
    protected String layer0IconPosition;
    //set Font
    protected DefaultFont font;
    protected String fontName;
    protected float fontSize;
    protected float lineSpacing;
    protected TextAlignment textAlignment;
    protected Color textColor;

    protected byte[] fontData;
    protected String encoding;
    protected boolean embedded;

    protected int fontType;

    protected byte[] getPdfData() {
        return pdfData;
    }

    public void setPdfData(byte[] pdfData) {
        this.pdfData = pdfData;
    }

    protected String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    protected String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    protected String getSignerCertificate() {
        return signerCertificate;
    }

    public void setSignerCertificate(String signerCertificate) {
        this.signerCertificate = signerCertificate;
    }

    protected String getPdfPassword() {
        return pdfPassword;
    }

    public void setPdfPassword(String pdfPassword) {
        this.pdfPassword = pdfPassword;
    }

    public void setSigningTime(long timeMillis, String format) {
        this.timeMillis = timeMillis;
        this.timeFormat = format;
    }

    public void setSigningTime(Calendar calendar, String format) {
        this.timeMillis = calendar.getTimeInMillis();
        this.timeFormat = format;
    }

    protected String getTimeFormat() {
        return timeFormat;
    }

    protected long getTimeMillis() {
        return timeMillis;
    }

    public void setVisibleSignature(String page, String offset, String boxSize, String text) {
        this.page = page;
        this.offset = offset;
        this.boxSize = boxSize;
        this.text = text;
        visibleSignatureType = 1;
    }

    public void setVisibleSignature(String offset, String boxSize, String text, boolean placeAll) {
        this.offset = offset;
        this.boxSize = boxSize;
        this.text = text;
        this.placeAll = placeAll;
        visibleSignatureType = 2;
    }

    public void setVisibleSignature(String boxSize, Map<Integer, String> pageAndPosition) {
        this.boxSize = boxSize;
        this.pageAndPosition = pageAndPosition;
        visibleSignatureType = 3;
    }

    public void setVisibleSignature(String offset, String boxSize, String text) {
        setVisibleSignature(offset, boxSize, text, false);
        visibleSignatureType = 4;
    }

    public void setVisibleSignature(String page, String position) {
        this.page = page;
        this.position = position;
        visibleSignatureType = 5;
    }

    protected int getVisibleSignatureType() {
        return visibleSignatureType;
    }

    protected String getPage() {
        return page;
    }

    protected String getOffset() {
        return offset;
    }

    protected String getBoxSize() {
        return boxSize;
    }

    protected String getText() {
        return text;
    }

    protected boolean isPlaceAll() {
        return placeAll;
    }

    protected Map<Integer, String> getPageAndPosition() {
        return pageAndPosition;
    }

    protected String getPosition() {
        return position;
    }

    protected byte[] getImage() {
        return image;
    }

    protected ImageProfile getImageProfile() {
        return imageProfile;
    }

    protected ImageAlgin getImageAlign() {
        return imageAlign;
    }

    public void setImage(byte[] image, ImageProfile imageProfile) {
        this.image = image;
        this.imageProfile = imageProfile;
        this.imageType = 1;
    }

    public void setImage(byte[] image, ImageProfile imageProfile, ImageAlgin imageAlign) {
        this.image = image;
        this.imageProfile = imageProfile;
        this.imageAlign = imageAlign;
        this.imageType = 2;
    }

    protected int getImageType() {
        return imageType;
    }

    protected byte[] getLayer0Icon() {
        return layer0Icon;
    }

    protected String getLayer0IconPosition() {
        return layer0IconPosition;
    }

    public void addLayer0Icon(byte[] data, String position) {
        this.layer0Icon = data;
        this.layer0IconPosition = position;
    }

    public void setFont(DefaultFont font, float fontSize, float lineSpacing, TextAlignment alignment, Color textColor) {
        this.font = font;
        this.fontSize = fontSize;
        this.lineSpacing = lineSpacing;
        this.textAlignment = alignment;
        this.textColor = textColor;
        fontType = 1;
    }

    public void setFont(String font, float fontSize, float lineSpacing, TextAlignment alignment, Color textColor) {
        this.fontName = font;
        this.fontSize = fontSize;
        this.lineSpacing = lineSpacing;
        this.textAlignment = alignment;
        this.textColor = textColor;
        fontType = 2;
    }

    public void setFont(byte[] fontData, String encoding, boolean embedded, float fontSize, float lineSpacing, TextAlignment alignment, Color textColor) {
        this.fontData = fontData;
        this.embedded = embedded;
        this.encoding = encoding;
        this.fontSize = fontSize;
        this.lineSpacing = lineSpacing;
        this.textAlignment = alignment;
        this.textColor = textColor;
        fontType = 3;
    }

    protected void setFont(DefaultFont font, float fontSize) {
        setFont(font, fontSize, lineSpacing, null, Color.BLACK);
    }

    protected String getFontName() {
        return fontName;
    }

    protected float getFontSize() {
        return fontSize;
    }

    protected float getLineSpacing() {
        return lineSpacing;
    }

    protected TextAlignment getTextAlignment() {
        return textAlignment;
    }

    protected Color getTextColor() {
        return textColor;
    }

    protected byte[] getFontData() {
        return fontData;
    }

    protected String getEncoding() {
        return encoding;
    }

    protected boolean isEmbedded() {
        return embedded;
    }

    protected int getFontType() {
        return fontType;
    }

    protected DefaultFont getFont() {
        return font;
    }

}
