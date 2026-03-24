package com.xdev.xdevbase.qr.controller;



import com.google.zxing.WriterException;
import com.xdev.xdevbase.qr.model.QrCodeRequest;
import com.xdev.xdevbase.qr.service.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/qrcode")
public class QrCodeController {

    @Autowired
    private com.xdev.xdevbase.qr.service.QrCodeService qrCodeService;

    @PostMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCode(@RequestBody QrCodeRequest request)
            throws WriterException, IOException {

        BufferedImage qrImage = qrCodeService.generateQrCode(
                request.getText(),
                request.getWidth(),
                request.getHeight()
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
    }
}