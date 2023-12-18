/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.mobileid.exsig;

import com.itextpdf.text.Element;

/**
 *
 * @author GiaTK
 */
public enum TextProfile {
    TEXT_BOTTOM_LEFT(Element.ALIGN_BOTTOM, Element.ALIGN_LEFT),
    TEXT_BOTTOM_RIGHT(Element.ALIGN_BOTTOM, Element.ALIGN_CENTER),
    TEXT_BOTTOM_CENTER(Element.ALIGN_BOTTOM, Element.ALIGN_RIGHT),
    TEXT_MIDDLE_LEFT(Element.ALIGN_MIDDLE, Element.ALIGN_LEFT),
    TEXT_MIDDLE_CENTER(Element.ALIGN_MIDDLE, Element.ALIGN_CENTER),
    TEXT_MIDDLE_RIGHT(Element.ALIGN_MIDDLE, Element.ALIGN_RIGHT),
    TEXT_TOP_LEFT(Element.ALIGN_TOP, Element.ALIGN_LEFT),
    TEXT_TOP_CENTER(Element.ALIGN_TOP, Element.ALIGN_CENTER),
    TEXT_TOP_RIGHT(Element.ALIGN_TOP, Element.ALIGN_RIGHT);
    
    protected final int vertical;
    protected final int horizontal;

    private TextProfile(int vertical, int horizontal) {
        this.vertical = vertical;
        this.horizontal = horizontal;
    }
}
