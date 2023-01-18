// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package main.java.ch.fhnw.imvs.bricks.actuators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import main.java.ch.fhnw.imvs.bricks.core.Brick;
import main.java.ch.fhnw.imvs.bricks.core.Proxy;

public final class LcdDisplayBrick extends Brick {
    private LcdDisplayBrick(Proxy proxy, String brickID) throws IOException {
        super(proxy, brickID);
    }
    private BufferedImage targetImage;
    private int imgWidth;
    private int imgHeight;

    public void setImage(Image path) throws IOException {
        targetImage = (BufferedImage) path;
        super.sync();
    }

    public void setSize(int width, int height) throws IOException {
        imgWidth = width;
        imgHeight = height;
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) throws IOException {
        BufferedImage resizedImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_BGR);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, imgWidth, imgHeight);
        graphics.drawImage(targetImage, 0, 0, imgWidth, imgHeight, null);
        graphics.dispose();
        int imWidth = resizedImage.getWidth();
        int imHeight = resizedImage.getHeight();
        int imBands = resizedImage.getRaster().getNumBands(); // typically 3 or 4, depending on RGB or ARGB


        int[] imArr = new int[imWidth * imHeight * imBands];
        resizedImage.getRaster().getPixels(0, 0, imWidth, imHeight, imArr);
        int index = 0;
        int[] imgBits = new int[imArr.length/3];
        for(int i = 0; i < imgBits.length;i++){
            while (index < imArr.length-2){
                int r = imArr[index];
                int g = imArr[index+1];
                int b = imArr[index+2];
                index += 3;
                if(r == 255 && g == 255 &&  b == 255){
                    imgBits[i] = 1;
                    break;
                } else if(r == 0 && g == 0 &&  b == 0){
                    imgBits[i] = 0;
                    break;
                } else {
                    double y = 0.2126*r + 0.7152*g + 0.0722*b;
                    double c = y < 128 ? 0 : 1;
                    if(c == 1){
                        imgBits[i] = 1;
                    } else {
                        imgBits[i] = 0;
                    }
                    break;
                }
            }
        }
        byte[] imgBytes = new byte[imgBits.length/8];
        int ind = 0;
        for(int i = 0; i < imgBytes.length;i++){
            while (ind < imgBits.length-7){
                String s0 = String.valueOf(imgBits[ind]);
                String s1 = String.valueOf(imgBits[ind + 1]);
                String s2 = String.valueOf(imgBits[ind + 2]);
                String s3 = String.valueOf(imgBits[ind + 3]);
                String s4 = String.valueOf(imgBits[ind + 4]);
                String s5 = String.valueOf(imgBits[ind + 5]);
                String s6 = String.valueOf(imgBits[ind + 6]);
                String s7 = String.valueOf(imgBits[ind + 7]);
                ind += 8;
                int num = Integer.parseInt( s0 + s1 + s2 + s3 + s4 + s5 + s6 + s7,2);
                imgBytes[i] = (byte) num;
                break;
            }
        }
        System.out.println(imgBytes.length);
        ByteBuffer buf = ByteBuffer.allocate(imgBytes.length);
        buf.order(ByteOrder.BIG_ENDIAN); // network byte order
        buf.put(imgBytes);
        return buf.array();
    }

    @Override
    protected void setCurrentPayload(byte[] payload) {

    }

    public static LcdDisplayBrick connect(Proxy proxy, String brickID) throws IOException {
        LcdDisplayBrick brick = new LcdDisplayBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
