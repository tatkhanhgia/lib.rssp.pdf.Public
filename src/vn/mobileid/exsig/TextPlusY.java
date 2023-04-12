/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.exsig;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author VUDP
 */
public class TextPlusY implements CharSequence {

    final List<String> texts = new ArrayList<>();
    final List<Float> yCoords = new ArrayList<>();
    final List<Float> xCoords = new ArrayList<>();
    private float xCoord;

    //
    // CharSequence implementation
    //
    @Override
    public int length() {
        int length = 0;
        for (String text : texts) {
            length += text.length();
        }
        return length;
    }

    @Override
    public char charAt(int index) {
        for (String text : texts) {
            if (index < text.length()) {
                return text.charAt(index);
            }
            index -= text.length();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        TextPlusY result = new TextPlusY();
        int length = end - start;
        for (int i = 0; i < yCoords.size(); i++) {
            String text = texts.get(i);
            if (start < text.length()) {
                float yCoord = yCoords.get(i);
                float xCoord = xCoords.get(i);
                if (start > 0) {
                    text = text.substring(start);
                    start = 0;
                }
                if (length > text.length()) {
                    result.add(text, yCoord, xCoord);
                } else {
                    result.add(text.substring(0, length), yCoord, xCoord);
                    break;
                }
            } else {
                start -= text.length();
            }
        }
        return result;
    }

    //
    // Object overrides
    //
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String text : texts) {
            builder.append(text);
        }
        return builder.toString();
    }

    public TextPlusY add(String text, float y, float x) {
        if (text != null) {
            texts.add(text);
            yCoords.add(y);
            xCoords.add(x);
        }
        return this;
    }

    public float yCoordAt(int index) {
        for (int i = 0; i < yCoords.size(); i++) {
            String text = texts.get(i);
            if (index < text.length()) {
                xCoord = xCoords.get(i);
                return yCoords.get(i);
            }
            index -= text.length();
        }
        throw new IndexOutOfBoundsException();
    }

    public float getxCoord() {
        return xCoord;
    }
}
