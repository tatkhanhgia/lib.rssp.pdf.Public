/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.exsig;

import com.itextpdf.text.Element;


/**
 *
 * @author Minhgalc
 */
public enum TextAlignment {
    
    ALIGN_LEFT(Element.ALIGN_LEFT),
    ALIGN_RIGHT(Element.ALIGN_RIGHT),
    ALIGN_CENTER(Element.ALIGN_CENTER);

    protected final int value;
    
    private TextAlignment(int textAlign) {
        this.value = textAlign;
    }
}
