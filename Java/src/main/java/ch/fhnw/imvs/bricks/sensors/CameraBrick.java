// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package main.java.ch.fhnw.imvs.bricks.sensors;

import main.java.ch.fhnw.imvs.bricks.core.Proxy;
import main.java.ch.fhnw.imvs.bricks.core.Brick;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CameraBrick extends Brick {
    protected CameraBrick(Proxy proxy, String brickID) throws IOException {
        super(proxy, brickID);
    }

    Path testImage = Path.of("Java/resources/testimage.jpg");
    private BufferedImage currentImage = ImageIO.read(new File("Java/resources/testimage.jpg"));
    public Image getImage() {
        return currentImage;
    }

    @Override
    protected byte[] getTargetPayload(boolean mock) throws IOException {
        byte[] payload = null;
        if (mock) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (ImageOutputStream output = ImageIO.createImageOutputStream(outputStream)) {
                ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("JPEG").next();

                ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                jpgWriteParam.setCompressionQuality(0.6f);

                jpgWriter.setOutput(output);

                jpgWriter.write(null, new IIOImage(currentImage, null, null), jpgWriteParam);

                jpgWriter.dispose();
            }
            ByteBuffer buf = ByteBuffer.allocate((int) Files.size(testImage));
            byte[] imageBytes = outputStream.toByteArray();
            buf.put(imageBytes);
            payload = buf.array();
        }
        return payload;
    }

    @Override
    protected void setCurrentPayload(byte[] payload) throws IOException {
        ByteArrayInputStream inputStream= new ByteArrayInputStream(payload);
        currentImage = ImageIO.read(inputStream);
    }

    public static CameraBrick connect(Proxy proxy, String brickID) throws IOException {
        CameraBrick brick = new CameraBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
