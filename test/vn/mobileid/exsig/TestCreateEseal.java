///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package vn.mobileid.exsig;
//
//import static com.itextpdf.text.Annotation.TEXT;
//import com.itextpdf.text.Document;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.Paragraph;
//import com.itextpdf.text.Phrase;
//import com.itextpdf.text.Rectangle;
//import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfDocument;
//import com.itextpdf.text.pdf.PdfPCell;
//import com.itextpdf.text.pdf.PdfPCellEvent;
//import com.itextpdf.text.pdf.PdfPRow;
//import com.itextpdf.text.pdf.PdfPTable;
//import com.itextpdf.text.pdf.PdfPTableEvent;
//import com.itextpdf.text.pdf.PdfPTableEventAfterSplit;
//import com.itextpdf.text.pdf.PdfWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
///**
// *
// * @author Admin
// */
//public class TestCreateEseal {
//
//    public static final String DEST = "file/Border2.pdf";
//
//    class DottedCells implements PdfPTableEvent {
//
//        public void tableLayout(PdfPTable table, float[][] widths,
//                float[] heights, int headerRows, int rowStart,
//                PdfContentByte[] canvases) {
//            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
//            canvas.setLineDash(3f, 3f);
//            float llx = widths[0][0];
//            float urx = widths[0][widths.length];
//            for (int i = 0; i < heights.length; i++) {
//                canvas.moveTo(llx, heights[i]);
//                canvas.lineTo(urx, heights[i]);
//            }
//            for (int i = 0; i < widths.length; i++) {
//                for (int j = 0; j < widths[i].length; j++) {
//                    canvas.moveTo(widths[i][j], heights[i]);
//                    canvas.lineTo(widths[i][j], heights[i + 1]);
//                }
//            }
//            canvas.stroke();
//        }
//    }
//
//    class DottedCell implements PdfPCellEvent {
//
//        public void cellLayout(PdfPCell cell, Rectangle position,
//                PdfContentByte[] canvases) {
//            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
//            canvas.setLineDash(3f, 3f);
//            canvas.rectangle(position.getLeft(), position.getBottom(),
//                    position.getWidth(), position.getHeight());
//            canvas.stroke();
//        }
//    }
//
//    class SpecialRoundedCell implements PdfPCellEvent {
//
//        public void cellLayout(PdfPCell cell, Rectangle position,
//                PdfContentByte[] canvases) {
//            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
//            float llx = position.getLeft() + 2;
//            float lly = position.getBottom() + 2;
//            float urx = position.getRight() - 2;
//            float ury = position.getTop() - 2;
//            float r = 4;
//            float b = 0.4477f;
//            canvas.moveTo(llx, lly);
//            canvas.lineTo(urx, lly);
//            canvas.lineTo(urx, ury - r);
//            canvas.curveTo(urx, ury - r * b, urx - r * b, ury, urx - r, ury);
//            canvas.lineTo(llx + r, ury);
//            canvas.curveTo(llx + r * b, ury, llx, ury - r * b, llx, ury - r);
//            canvas.lineTo(llx, lly);
//            canvas.stroke();
//        }
//    }
//
//    class DottedHeader implements PdfPTableEvent {
//
//        public void tableLayout(PdfPTable table, float[][] widths,
//                float[] heights, int headerRows, int rowStart,
//                PdfContentByte[] canvases) {
//            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
//            canvas.setLineDash(3f, 3f);
//            float x1 = widths[0][0];
//            float x2 = widths[0][widths.length];
//            canvas.moveTo(x1, heights[0]);
//            canvas.lineTo(x2, heights[0]);
//            canvas.moveTo(x1, heights[headerRows]);
//            canvas.lineTo(x2, heights[headerRows]);
//            canvas.stroke();
//        }
//    }
//
//    class DottedCell2 implements PdfPCellEvent {
//
//        public void cellLayout(PdfPCell cell, Rectangle position,
//                PdfContentByte[] canvases) {
//            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
//            canvas.setLineDash(3f, 3f);
//            canvas.moveTo(position.getLeft(), position.getTop());
//            canvas.lineTo(position.getRight(), position.getTop());
//            canvas.moveTo(position.getLeft(), position.getBottom());
//            canvas.lineTo(position.getRight(), position.getBottom());
//            canvas.stroke();
//        }
//    }
//
//    class BorderEvent2 implements PdfPTableEventAfterSplit {
//
//        protected boolean bottom = true;
//        protected boolean top = true;
//
//        public void splitTable(PdfPTable table) {
//            bottom = false;
//        }
//
//        public void afterSplitTable(PdfPTable table, PdfPRow startRow, int startIdx) {
//            top = false;
//        }
//
//        public void tableLayout(PdfPTable table, float[][] width, float[] height,
//                int headerRows, int rowStart, PdfContentByte[] canvas) {
//            float widths[] = width[0];
//            float y1 = height[0];
//            float y2 = height[height.length - 1];
//            float x1 = widths[0];
//            float x2 = widths[widths.length - 1];
//            PdfContentByte cb = canvas[PdfPTable.LINECANVAS];
//            cb.moveTo(x1, y1);
//            cb.lineTo(x1, y2);
//            cb.moveTo(x2, y1);
//            cb.lineTo(x2, y2);
//            if (top) {
//                cb.moveTo(x1, y1);
//                cb.lineTo(x2, y1);
//            }
//            if (bottom) {
//                cb.moveTo(x1, y2);
//                cb.lineTo(x2, y2);
//            }
//            cb.stroke();
//            cb.resetRGBColorStroke();
//            bottom = true;
//            top = true;
//        }
//    }
//
//    class BorderEvent implements PdfPTableEventAfterSplit {
//
//        protected int rowCount;
//        protected boolean bottom = true;
//        protected boolean top = true;
//
//        public void setRowCount(int rowCount) {
//            this.rowCount = rowCount;
//        }
//
//        public void splitTable(PdfPTable table) {
//            if (table.getRows().size() != rowCount) {
//                bottom = false;
//            }
//        }
//
//        public void afterSplitTable(PdfPTable table, PdfPRow startRow, int startIdx) {
//            if (table.getRows().size() != rowCount) {
//                // if the table gains a row, a row was split
//                rowCount = table.getRows().size();
//                top = false;
//            }
//        }
//
//        public void tableLayout(PdfPTable table, float[][] width, float[] height,
//                int headerRows, int rowStart, PdfContentByte[] canvas) {
//            float widths[] = width[0];
//            float y1 = height[0];
//            float y2 = height[height.length - 1];
//            PdfContentByte cb = canvas[PdfPTable.LINECANVAS];
//            for (int i = 0; i < widths.length; i++) {
//                cb.moveTo(widths[i], y1);
//                cb.lineTo(widths[i], y2);
//            }
//            float x1 = widths[0];
//            float x2 = widths[widths.length - 1];
//            for (int i = top ? 0 : 1; i < (bottom ? height.length : height.length - 1); i++) {
//                cb.moveTo(x1, height[i]);
//                cb.lineTo(x2, height[i]);
//            }
//            cb.stroke();
//            cb.resetRGBColorStroke();
//            bottom = true;
//            top = true;
//        }
//    }
//
//    public static void main(String[] args) throws IOException, DocumentException {
//        File file = new File(DEST);
//        file.getParentFile().mkdirs();
//        new TestCreateEseal().createBorder2Pdf(DEST);
//    }
//
//    public void createDottedPdf(String dest) throws IOException, DocumentException {
//        TestCreateEseal app = new TestCreateEseal();
//        Document document = new Document();
//        PdfWriter.getInstance(document, new FileOutputStream(dest));
//        document.open();
//        document.add(new Paragraph("Table event"));
//        PdfPTable table = new PdfPTable(3);
//        table.setTableEvent(app.new DottedCells());
//        table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
//        table.addCell("A1");
//        table.addCell("A2");
//        table.addCell("A3");
//        table.addCell("B1");
//        table.addCell("B2");
//        table.addCell("B3");
//        table.addCell("C1");
//        table.addCell("C2");
//        table.addCell("C3");
//        document.add(table);
//        document.add(new Paragraph("Cell event"));
//        table = new PdfPTable(1);
//        PdfPCell cell = new PdfPCell(new Phrase("Test"));
//        cell.setCellEvent(app.new DottedCell());
//        cell.setBorder(PdfPCell.NO_BORDER);
//        table.addCell(cell);
//        document.add(table);
//        document.close();
//    }
//
//    public void createRoundPdf(String dest) throws IOException, DocumentException {
//        Document document = new Document();
//        PdfWriter.getInstance(document, new FileOutputStream(dest));
//        document.open();
//        PdfPTable table = new PdfPTable(3);
//        PdfPCell cell = getCell("These cells have rounded borders at the top.");
//        table.addCell(cell);
//        cell = getCell("These cells aren't rounded at the bottom.");
//        table.addCell(cell);
//        cell = getCell("A custom cell event was used to achieve this.");
//        table.addCell(cell);
//        document.add(table);
//        document.close();
//    }
//
//    public void createDottedHeaderPdf(String dest) throws IOException, DocumentException {
//        Document document = new Document();
//        PdfWriter.getInstance(document, new FileOutputStream(dest));
//        document.open();
//        document.add(new Paragraph("Table event"));
//        PdfPTable table = new PdfPTable(3);
//        table.setTableEvent(new DottedHeader());
//        table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
//        table.addCell("A1");
//        table.addCell("A2");
//        table.addCell("A3");
//        table.setHeaderRows(1);
//        table.addCell("B1");
//        table.addCell("B2");
//        table.addCell("B3");
//        table.addCell("C1");
//        table.addCell("C2");
//        table.addCell("C3");
//        document.add(table);
//        document.add(new Paragraph("Cell event"));
//        table = new PdfPTable(3);
//        table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
//        table.getDefaultCell().setCellEvent(new DottedCell());
//        table.addCell("A1");
//        table.addCell("A2");
//        table.addCell("A3");
//        table.getDefaultCell().setCellEvent(null);
//        table.addCell("B1");
//        table.addCell("B2");
//        table.addCell("B3");
//        table.addCell("C1");
//        table.addCell("C2");
//        table.addCell("C3");
//        document.add(table);
//        document.close();
//    }
//
//    public void createBorderPdf(String dest) throws IOException, DocumentException {
//        Document document = new Document();
//        PdfWriter.getInstance(document, new FileOutputStream(dest));
//        document.open();
//        PdfPTable table = new PdfPTable(2);
//        table.setTotalWidth(500);
//        table.setLockedWidth(true);
//        BorderEvent event = new BorderEvent();
//        table.setTableEvent(event);
//        table.setWidthPercentage(100);
//        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
//        table.setSplitLate(false);
//        PdfPCell cell = new PdfPCell(new Phrase(TEXT));
//        cell.setBorder(Rectangle.NO_BORDER);
//        for (int i = 0; i < 60;) {
//            table.addCell("Cell " + (++i));
//            table.addCell(cell);
//        }
//        event.setRowCount(table.getRows().size());
//        document.add(table);
//        document.close();
//    }
//
//    public void createBorder2Pdf(String dest) throws IOException, DocumentException {
//        Document document = new Document();
//        PdfWriter.getInstance(document, new FileOutputStream(dest));
//        document.open();
//        PdfPTable table = new PdfPTable(2);
//        table.setTotalWidth(500);
//        table.setLockedWidth(true);
//        BorderEvent2 event = new BorderEvent2();
//        table.setTableEvent(event);
//        table.setWidthPercentage(100);
//        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
//        table.setSplitLate(false);
//        PdfPCell cell = new PdfPCell(new Phrase(TEXT));
//        cell.setBorder(Rectangle.NO_BORDER);
//        for (int i = 0; i < 60; ) {
//            table.addCell("Cell " + (++i));
//            table.addCell(cell);
//        }
//        document.add(table);
//        document.close();
//    }
//    
//    public PdfPCell getCell(String content) {
//        PdfPCell cell = new PdfPCell(new Phrase(content));
//        cell.setCellEvent(new SpecialRoundedCell());
//        cell.setPadding(5);
//        cell.setBorder(PdfPCell.NO_BORDER);
//        return cell;
//    }
//}
