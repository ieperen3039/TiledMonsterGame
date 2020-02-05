package NG.GUIMenu.Components;

import NG.GUIMenu.Frames.SFrameLookAndFeel;
import NG.Rendering.Textures.Texture;
import org.joml.Vector2ic;

/**
 * a panel that displays a section of a texture
 */
public class STexturedPanel extends SComponent {
    private final Texture texture;

    private int x;
    private int y;
    private int width;
    private int height;

    public STexturedPanel(Texture texture, int width, int height, int x, int y) {
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        setGrowthPolicy(false, false);
    }

    public STexturedPanel(Texture texture, boolean growthPolicy) {
        this.texture = texture;
        width = texture.getWidth();
        height = texture.getHeight();
        x = 0;
        y = 0;

        setGrowthPolicy(growthPolicy, growthPolicy);
    }

    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;
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
        // TODO pull from home
    }
}
