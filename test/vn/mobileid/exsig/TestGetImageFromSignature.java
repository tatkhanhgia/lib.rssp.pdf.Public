/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.mobileid.exsig;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PRIndirectReference;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfIndirectReference;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStream;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author Admin
 */
public class TestGetImageFromSignature {

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        String file = "C:\\Users\\Admin\\Downloads\\sample.pdf";
        byte[] src = Files.readAllBytes(new File(file).toPath());
        PdfReader reader = new PdfReader(src);
        AcroFields acro = reader.getAcroFields();
        List<String> signame = acro.getSignatureNames();
        System.out.println("\n\t Verify Signature");
        for (String name : signame) {
            AcroFields.Item item = acro.getFieldItem(name);
            PdfDictionary dict = item.getValue(0);
            PdfDictionary AP = dict.getAsDict(PdfName.AP);

            PdfStream n2 = AP.getAsStream(PdfName.N)
                    .getAsDict(PdfName.RESOURCES)
                    .getAsDict(PdfName.XOBJECT)
                    .getAsStream(PdfName.FRM)
                    .getAsDict(PdfName.RESOURCES)
                    .getAsDict(PdfName.XOBJECT)
                    .getAsStream(PdfName.N2);
            PdfDictionary n23 = AP.getAsStream(PdfName.N)
                    .getAsDict(PdfName.RESOURCES)
                    .getAsDict(PdfName.XOBJECT)
                    .getAsStream(PdfName.FRM)
                    .getAsDict(PdfName.RESOURCES)
                    .getAsDict(PdfName.XOBJECT)
                    .getAsStream(PdfName.N0)
                    .getAsDict(PdfName.RESOURCES)
                    .getAsDict(PdfName.XOBJECT);
            for (PdfName names : n23.getKeys()) {
                PdfStream image = n23.getAsStream(names);
                PdfImageObject data = new PdfImageObject((PRStream)image);
                byte[] hello = data.getImageAsBytes();
                try ( FileOutputStream fos = new FileOutputStream("C:\\Users\\Admin\\Downloads\\"+names+".png")) {
                    fos.write(hello);                    
                    fos.close();
                }                
            }
            System.out.println("as");
        }
    }
}
