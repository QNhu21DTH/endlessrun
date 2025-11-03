/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game;

import java.awt.Image;
import javax.swing.*;
import java.awt.image.BufferedImage;

public class Sprite {
    private BufferedImage sheet;
    private int frameCount, frameWidth, frameHeight;
    private int currentFrame = 0;
    private long lastFrameTime = 0;

    public Sprite(String path, int frames, int w, int h) {
        this.frameCount = frames;
        this.frameWidth = w;
        this.frameHeight = h;
        sheet = new BufferedImage(frames * w, h, BufferedImage.TYPE_INT_ARGB);
        Image img = new ImageIcon(path).getImage();
        sheet.getGraphics().drawImage(img, 0, 0, null);
    }

    public void update() {
        if (System.currentTimeMillis() - lastFrameTime > 100) {
            currentFrame = (currentFrame + 1) % frameCount;
            lastFrameTime = System.currentTimeMillis();
        }
    }

    public Image getImage() {
        return sheet.getSubimage(currentFrame * frameWidth, 0, frameWidth, frameHeight);
    }
}

