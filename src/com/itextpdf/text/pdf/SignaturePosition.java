/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itextpdf.text.pdf;

import com.itextpdf.text.Rectangle;

/**
 *
 * @author minhg
 */
public class SignaturePosition {
    private int page;
    private Rectangle rectangle;

    public SignaturePosition(int page, Rectangle rectangle) {
        this.page = page;
        this.rectangle = rectangle;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
    
    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }
    
    

}
