/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itextpdf.text.pdf;

import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.io.RASInputStream;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.security.PdfPKCS7_v4;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author USER
 */
public class AcroFields_v4 extends AcroFields {

    public AcroFields_v4(PdfReader_v4 reader, PdfWriter writer) {
        super(reader, writer);
    }

    public PdfPKCS7_v4 verifySignature_v4(String name, String provider) {
        PdfDictionary v = getSignatureDictionary(name);
        if (v == null) {
            return null;
        }
        try {
            PdfName sub = v.getAsName(PdfName.SUBFILTER);
            PdfString contents = v.getAsString(PdfName.CONTENTS);
            PdfPKCS7_v4 pk = null;
            if (sub.equals(PdfName.ADBE_X509_RSA_SHA1)) {
                PdfString cert = v.getAsString(PdfName.CERT);
                if (cert == null) {
                    cert = v.getAsArray(PdfName.CERT).getAsString(0);
                }
                pk = new PdfPKCS7_v4(contents.getOriginalBytes(), cert.getBytes(), provider);
            } else {
                pk = new PdfPKCS7_v4(contents.getOriginalBytes(), sub, provider);
            }
            updateByteRange(pk, v);
            PdfString str = v.getAsString(PdfName.M);
            if (str != null) {
                pk.setSignDate(PdfDate.decode(str.toString()));
            }
            PdfObject obj = PdfReader_v4.getPdfObject(v.get(PdfName.NAME));
            if (obj != null) {
                if (obj.isString()) {
                    pk.setSignName(((PdfString) obj).toUnicodeString());
                } else if (obj.isName()) {
                    pk.setSignName(PdfName.decodeName(obj.toString()));
                }
            }
            str = v.getAsString(PdfName.REASON);
            if (str != null) {
                pk.setReason(str.toUnicodeString());
            }
            str = v.getAsString(PdfName.LOCATION);
            if (str != null) {
                pk.setLocation(str.toUnicodeString());
            }
            return pk;
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private void updateByteRange(PdfPKCS7_v4 pkcs7, PdfDictionary v) {
        PdfArray b = v.getAsArray(PdfName.BYTERANGE);
        RandomAccessFileOrArray rf = reader.getSafeFile();
        InputStream rg = null;
        try {
            rg = new RASInputStream(new RandomAccessSourceFactory().createRanged(rf.createSourceView(), b.asLongArray()));
            byte buf[] = new byte[8192];
            int rd;
            while ((rd = rg.read(buf, 0, buf.length)) > 0) {
                pkcs7.update(buf, 0, rd);
            }
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        } finally {
            try {
                if (rg != null) {
                    rg.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(AcroFields_v4.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
