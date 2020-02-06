package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.Rendering.Textures.Texture;
import org.joml.Vector2ic;

/**
 * a panel that displays a section of a texture
 */
public class STexturedPanel extends SComponent {
    private final Texture texture;

    private int width;
    private int height;
    private int nvgTextID = -1;

    /**
     * create a textured panel with the given minimum size. The texture is stretched to match the given dimensions.
     * @param texture the texture to show
     * @param width   minimum width in pixels
     * @param height  minimum height in pixels
     */
    public STexturedPanel(Texture texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
        setGrowthPolicy(false, false);
    }

    /**
     * create a textured panel with the minimum resolution of the original texture
     * @param texture the texture to show
     * @param growthPolicy the growth policy. When false, the texture will not grow past the original resolution of the texture.
     */
    public STexturedPanel(Texture texture, boolean growthPolicy) {
        this.texture = texture;
        width = texture.getWidth();
        height = texture.getHeight();

        setGrowthPolicy(growthPolicy, growthPolicy);
    }

    @Override
    public int minWidth() {
        return width;
    }

    @Override
    public int minHeight() {
        return height;
    }

    @Override
    public void draw(SFrameLookAndFeel design, Vector2ic screenPosition) {
        if (nvgTextID == -1) {
            nvgTextID = design.getPainter()
                    .createImageFromTexture(texture.getID(), texture.getWidth(), texture.getHeight());
        }

        design.getPainter().drawImage(nvgTextID, screenPosition.x(), screenPosition.y(), width, height);
    }
}
