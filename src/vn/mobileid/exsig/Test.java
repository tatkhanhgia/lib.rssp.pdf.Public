/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.mobileid.exsig;

import com.itextpdf.text.pdf.PRIndirectReference;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReaderV4;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.List;
import java.util.ListIterator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author GiaTK
 */
public class Test {

    public static void main(String[] args) throws IOException, Exception {
        byte[] file = Files.readAllBytes(Paths.get("C:\\Users\\Admin\\Downloads\\Check\\decoded-file-15.pdf"));
        System.out.println("Is Valid:"+PdfProfile.verify(file, null));
//        PdfProfileCMS profile = new PdfProfileCMS(Algorithm.SHA256);        }
//        byte[] file = Files.readAllBytes(Paths.get("C:\\Users\\Admin\\Downloads\\Check\\decoded-file-7.pdf"));
//        String password = null;
//        PdfReaderV4.unethicalreading = true;
//        PdfReaderV4 reader;
//        if (password == null) {
//            reader = new PdfReaderV4(file);
//        } else {
//            reader = new PdfReaderV4(file, password.getBytes());
//        }
//        Security.addProvider(new BouncyCastleProvider());
//        PdfDictionary page = reader.getPageN(2);
//        PdfArray annots = page.getAsArray(PdfName.ANNOTS);
//        ListIterator<PdfObject> lists = annots.listIterator();
//        while (lists.hasNext()) {
//            PRIndirectReference reference = (PRIndirectReference) lists.next();
//            PdfDictionary dict = (PdfDictionary) PdfReaderV4.getPdfObject(reference);
//            try {
//                if (dict.getAsName(PdfName.FT).equals(PdfName.SIG)) {
//                    System.out.println("Name:"+dict.getAsString(PdfName.T));
//                    System.out.println("Hello");
//                }
//            } catch (Exception ex) {
//            }
//            String tempp = "a";
//        }
//        String temp = "";
    }
}
