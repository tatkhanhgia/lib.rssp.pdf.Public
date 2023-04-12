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
public enum ImageProfile {
    
    IMAGE_LEFT(Element.ALIGN_MIDDLE, Element.ALIGN_LEFT, Element.ALIGN_LEFT, 4),
    IMAGE_RIGHT(Element.ALIGN_MIDDLE, Element.ALIGN_LEFT, Element.ALIGN_LEFT, 4),
    IMAGE_BOTTOM(Element.ALIGN_MIDDLE, Element.ALIGN_LEFT, Element.ALIGN_LEFT, 4),
    IMAGE_BOTTOM_TEXT_BOTTOM(Element.ALIGN_BOTTOM, Element.ALIGN_LEFT, Element.ALIGN_LEFT, 4),
    IMAGE_TOP(Element.ALIGN_MIDDLE, Element.ALIGN_LEFT, Element.ALIGN_LEFT, 4),
    IMAGE_TOP_TEXT_TOP(Element.ALIGN_TOP, Element.ALIGN_LEFT, Element.ALIGN_LEFT, 4),
    IMAGE_CENTER(Element.ALIGN_MIDDLE, Element.ALIGN_LEFT, Element.ALIGN_LEFT, 6);

    protected final int vertical;
    protected final int horizontal;
    protected final int textAlign;
    protected final int border;
    
    private ImageProfile(int vertical ,int horizontal, int textAlign, int border) {
        this.vertical = vertical;
        this.horizontal = horizontal;
        this.textAlign = textAlign;
        this.border = border;
    }
}
