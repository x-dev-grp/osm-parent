package com.xdev.xdevbase.qr.model;


public class QrCodeInfo   {
    private final String publicCode;
    private final String qrImageBase64;  // new field

    public String getPublicCode() {
        return publicCode;
    }

    public QrCodeInfo(String publicCode, String qrImageBase64) {
        this.publicCode = publicCode;
        this.qrImageBase64 = qrImageBase64;
    }


    public String getQrImageBase64() {
        return qrImageBase64;
    }
}
