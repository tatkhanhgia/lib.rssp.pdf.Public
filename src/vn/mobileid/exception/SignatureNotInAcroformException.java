/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.mobileid.exception;

/**
 *
 * @author GiaTK
 */
public class SignatureNotInAcroformException extends Exception{

    public SignatureNotInAcroformException() {
    }

    public SignatureNotInAcroformException(String string) {
        super(string);
    }

    public SignatureNotInAcroformException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public SignatureNotInAcroformException(Throwable thrwbl) {
        super(thrwbl);
    }

    public SignatureNotInAcroformException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }
    
}
