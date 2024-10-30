package org.betterbeeng;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JPanel;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.betterbeeng.entity.Archer;
import org.betterbeeng.entity.Entity;
import org.betterbeeng.entity.Mage;
import org.betterbeeng.entity.Warrior;

public class GameDisplay extends JPanel {
    private List<Entity> teamAEntities;
    private List<Entity> teamBEntities;
    private BufferedImage warriorTeamAImage;
    private BufferedImage warriorTeamBImage;
    private BufferedImage mageTeamAImage;
    private BufferedImage mageTeamBImage;
    private BufferedImage archerTeamAImage;
    private BufferedImage archerTeamBImage;

    public GameDisplay(List<Entity> teamAEntities, List<Entity> teamBEntities) {
        this.teamAEntities = teamAEntities;
        this.teamBEntities = teamBEntities;
        loadAssets();
    }

    private void loadAssets() {
        try {
            warriorTeamAImage = applyColorFilter(loadSvgAsBufferedImage("assets/warrior.svg"), Color.BLUE);
            warriorTeamBImage = applyColorFilter(loadSvgAsBufferedImage("assets/warrior.svg"), Color.RED);
            mageTeamAImage = applyColorFilter(loadSvgAsBufferedImage("assets/mage.svg"), Color.BLUE);
            mageTeamBImage = applyColorFilter(loadSvgAsBufferedImage("assets/mage.svg"), Color.RED);
            archerTeamAImage = applyColorFilter(loadSvgAsBufferedImage("assets/archer.svg"), Color.BLUE);
            archerTeamBImage = applyColorFilter(loadSvgAsBufferedImage("assets/archer.svg"), Color.RED);
        } catch (IOException | TranscoderException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage loadSvgAsBufferedImage(String filePath) throws IOException, TranscoderException {
        TranscoderInput input = new TranscoderInput(new File(filePath).toURI().toString());
        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
        transcoder.transcode(input, null);
        return transcoder.getBufferedImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawEntities(g, teamAEntities);
        drawEntities(g, teamBEntities);
    }

    private void drawEntities(Graphics g, List<Entity> entities) {
        for (Entity entity : entities) {
            BufferedImage image = getEntityImage(entity);
            if (image != null) {
                g.drawImage(image, entity.getX() * 20, entity.getY() * 20, 20, 20, this);
            }
        }
    }

    private BufferedImage getEntityImage(Entity entity) {
        if (entity instanceof Warrior) {
            return entity.getTeamId() == 1 ? warriorTeamAImage : warriorTeamBImage;
        } else if (entity instanceof Mage) {
            return entity.getTeamId() == 1 ? mageTeamAImage : mageTeamBImage;
        } else if (entity instanceof Archer) {
            return entity.getTeamId() == 1 ? archerTeamAImage : archerTeamBImage;
        }
        return null;
    }

    private BufferedImage applyColorFilter(BufferedImage image, Color color) {
        BufferedImage coloredImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb & 0xFF000000) != 0x00000000) { // Check if pixel is not transparent
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int newR = (color.getRed() * r) / 255;
                    int newG = (color.getGreen() * g) / 255;
                    int newB = (color.getBlue() * b) / 255;
                    int newArgb = (argb & 0xFF000000) | (newR << 16) | (newG << 8) | newB;
                    coloredImage.setRGB(x, y, newArgb);
                }
            }
        }
        return coloredImage;
    }

    public void updateEntities(List<Entity> teamAEntities, List<Entity> teamBEntities) {
        this.teamAEntities = teamAEntities;
        this.teamBEntities = teamBEntities;
        repaint();
    }

    private static class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage bufferedImage;

        @Override
        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage img, TranscoderOutput output) {
            this.bufferedImage = img;
        }

        public BufferedImage getBufferedImage() {
            return bufferedImage;
        }
    }
}