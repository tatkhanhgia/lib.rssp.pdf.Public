/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.exsig;

import vn.mobileid.font.FontPath;

/**
 *
 * @author minhg
 */
public enum DefaultFont {
    Arial("D-Arial.ttf"),
    Times("D-Times.ttf"),
    Verdana("D-Verdana.ttf"),
    Tahoma("D-Tahoma.ttf"),
    Georgia("D-Georgia.ttf"),
    Sig_Scripture("S-Scripture.ttf");
    
    private final String value;
    
    private DefaultFont(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
   
    public String getPath() {
        return new FontPath().getClass().getResource(value).toString();
    }
}
