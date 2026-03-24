package com.xdev.xdevbase.qr.model;


public class QrCodeRequest {
    private String text;        // texte à encoder
    private int width = 300;    // largeur par défaut
    private int height = 300;   // hauteur par défaut

    // Constructeurs, getters et setters
    public QrCodeRequest() {}


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }
}