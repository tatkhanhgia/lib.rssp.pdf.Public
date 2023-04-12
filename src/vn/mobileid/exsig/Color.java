/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.exsig;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;


/**
 *
 * @author Minhgalc
 */
public enum Color {
    

    WHITE(com.itextpdf.text.BaseColor.WHITE, java.awt.Color.WHITE),
    LIGHT_GRAY(com.itextpdf.text.BaseColor.LIGHT_GRAY, java.awt.Color.LIGHT_GRAY),
    GRAY(com.itextpdf.text.BaseColor.GRAY, java.awt.Color.GRAY),
    DARK_GRAY(com.itextpdf.text.BaseColor.DARK_GRAY, java.awt.Color.DARK_GRAY),
    BLACK(com.itextpdf.text.BaseColor.BLACK, java.awt.Color.BLACK),
    RED(com.itextpdf.text.BaseColor.RED, java.awt.Color.RED),
    PINK(com.itextpdf.text.BaseColor.PINK, java.awt.Color.PINK),
    ORANGE(com.itextpdf.text.BaseColor.ORANGE, java.awt.Color.ORANGE),
    YELLOW(com.itextpdf.text.BaseColor.YELLOW, java.awt.Color.YELLOW),
    GREEN(com.itextpdf.text.BaseColor.GREEN, java.awt.Color.GREEN),
    MAGENTA(com.itextpdf.text.BaseColor.MAGENTA, java.awt.Color.MAGENTA),
    CYAN(com.itextpdf.text.BaseColor.CYAN, java.awt.Color.CYAN),
    BLUE(com.itextpdf.text.BaseColor.BLUE, java.awt.Color.BLUE),
    ;
    private com.itextpdf.text.BaseColor color;
    private java.awt.Color awtColor;

    private Color(BaseColor color, java.awt.Color awtColor) {
        this.color = color;
        this.awtColor = awtColor;
    }
    public com.itextpdf.text.BaseColor getColor() {
        return color;
    }

    public java.awt.Color getAwtColor() {
        return awtColor;
    }
    
}
