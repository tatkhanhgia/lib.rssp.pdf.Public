/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.itextpdf.text.pdf.security;

/**
 *
 * @author GiaTK
 */
public class NotEnoughSpaceException extends Exception{

    public NotEnoughSpaceException() {
    }

    public NotEnoughSpaceException(String string) {
        super(string);
    }

    public NotEnoughSpaceException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public NotEnoughSpaceException(Throwable thrwbl) {
        super(thrwbl);
    }

    public NotEnoughSpaceException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }
    
}
