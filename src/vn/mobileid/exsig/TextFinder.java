/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.exsig;

/**
 *
 * @author minhg
 */
class TextFinder {
    final int page;
    final int offsetX;
    final int offsetY;
    final int width;
    final int height;
    final String text;
    //update 03/08/2021
    final boolean placeAll;
    
    public TextFinder(int page, int offsetX, int offsetY, int width, int height, String text, boolean placeAll) {
        this.page = page;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
        this.text = text;
        this.placeAll = placeAll;
    }

   
 
}
