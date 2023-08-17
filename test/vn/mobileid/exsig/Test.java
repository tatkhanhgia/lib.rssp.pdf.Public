///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package vn.mobileid.exsig;
//
//import com.itextpdf.text.Document;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.Element;
//import com.itextpdf.text.Font;
//import com.itextpdf.text.PageSize;
//import com.itextpdf.text.Phrase;
//import com.itextpdf.text.Rectangle;
//import com.itextpdf.text.pdf.BaseFont;
//import com.itextpdf.text.pdf.ColumnText;
//import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
///**
// *
// * @author gia
// */
//public class Test {
//    public static void main(String[] args) throws DocumentException, IOException
//    {
//       testShowTextAlignedVsSimpleColumnTopAlignment();
//    }
//    
//    public static void testShowTextAlignedVsSimpleColumnTopAlignment() throws DocumentException, IOException
//{
//    Document document = new Document(PageSize.A4);
//    String RESULT_FOLDER = "C:\\Users\\gia\\Desktop\\haha.pdf";
//
//    PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(RESULT_FOLDER)));
//    document.open();
//
//    Font fontQouteItems = new Font(BaseFont.createFont(), 12);
//    PdfContentByte canvas = writer.getDirectContent();
//
//    // Item Number
//    ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Phrase("36222-0", fontQouteItems), 60, 450, 0);
//
//    // Estimated Qty
//    ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Phrase("47", fontQouteItems), 143, 450, 0);
//
//    // Item Description
//    ColumnText ct = new ColumnText(canvas); // Uses a simple column box to provide proper text wrapping
//    ct.setSimpleColumn(new Rectangle(193, 070, 200, 200));
//    ct.setText(new Phrase("In-Situ : Poly Cable - 100'\nPoly vented rugged black gable 100ft\nThis is an additional description. It can wrap an extra line if it needs to so this text is long.", fontQouteItems));
//    System.out.println(ct.go(true) == ColumnText.NO_MORE_TEXT);
//    ct = new ColumnText(canvas); // Uses a simple column box to provide proper text wrapping
//    ct.setSimpleColumn(new Rectangle(193, 070, 200, 200));
//    ct.setText(new Phrase("In-Situ : Poly Cable - 100'\nPoly vented rugged black gable 100ft\nThis is an additional description. It can wrap an extra line if it needs to so this text is long.", fontQouteItems));
//    ct.go();
//    document.close();
//}
//}
