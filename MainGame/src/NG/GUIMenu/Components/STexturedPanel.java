package NG.GUIMenu.Components;

import NG.GUIMenu.Rendering.SFrameLookAndFeel;
import NG.Rendering.Textures.Texture;
import org.joml.Vector2ic;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * a panel that displays a section of a texture
 */
public class STexturedPanel extends SComponent {
    private final Texture texture;

    private int minWidth;
    private int minHeight;
    private int nvgTextID = -1;

    /**
     * create a textured panel with the given minimum size. The texture is stretched to match the given dimensions.
     * @param texture the texture to show
     * @param width   minimum width in pixels
     * @param height  minimum height in pixels
     */
    public STexturedPanel(Texture texture, int width, int height) {
        this.texture = texture;
        this.minWidth = width;
        this.minHeight = height;
        setGrowthPolicy(false, false);
    }

    @Override
    public int minWidth() {
        return minWidth;
    }

    @Override
    public int minHeight() {
        return minHeight;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        if (nvgTextID == -1) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(texture.getWidth() * texture.getHeight() * 4);
            glBindTexture(GL_TEXTURE_2D, texture.getID());
            glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

            nvgTextID = design.getPainter().createImageFromBuffer(buffer, texture.getWidth(), texture.getHeight());
        }

        int width = getWidth();
        int height = getHeight();
        int xHBorder = (width - minWidth) / 2;
        int yHBorder = (height - minHeight) / 2;
        design.getPainter()
                .drawImage(nvgTextID, screenPosition.x() + xHBorder, screenPosition.y() + yHBorder, minWidth, minHeight, 0, 1);
    }
}
