/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itextpdf.text.pdf;

import java.io.IOException;

/**
 *
 * @author USER
 */
public class PdfReader_v4 extends PdfReader{

    public PdfReader_v4(byte[] src) throws IOException {
        super(src);
    }
    
    public PdfReader_v4(byte[] src , byte[] password) throws IOException {
        super(src, password);
    }
    
    public AcroFields_v4 getAcroFields_v4() {
        return new AcroFields_v4(this, null);
    }
}
