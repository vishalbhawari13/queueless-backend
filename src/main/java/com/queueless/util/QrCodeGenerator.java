package com.queueless.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;

public class QrCodeGenerator {

    public static BufferedImage generateQr(String text, int size)
            throws Exception {

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix =
                writer.encode(text, BarcodeFormat.QR_CODE, size, size);

        BufferedImage image =
                new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                image.setRGB(
                        x,
                        y,
                        matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF
                );
            }
        }
        return image;
    }
}
