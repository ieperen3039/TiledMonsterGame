package NG.GameMap;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.Shaders.ShaderException;
import NG.Rendering.Textures.Texture;
import org.joml.Vector2ic;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glActiveTexture;

/**
 * a texture giving a top-down view of the map as a low-res texture
 */
public class MiniMapTexture implements Texture {
    private final GameMap target;
    private final int id;

    /**
     * The map is definite: it does not depend on the current game.
     * @param target the map to map
     */
    public MiniMapTexture(GameMap target) {
        this.target = target;

        this.id = glGenTextures();
    }

    public void init() throws ShaderException {
        Vector2ic size = target.getSize();
        int xSize = size.x();
        int ySize = size.y();

        // Load texture contents into a byte buffer
        int byteSize = 4;
        ByteBuffer buffer = ByteBuffer.allocateDirect(byteSize * xSize * ySize);

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                int height = target.getHeightAt(x, y);
                Color4f color = Color4f.rgb(0, height, 255 - height);
                color.put(buffer);
            }
        }

        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, id);

        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Upload the texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, xSize, ySize, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    }

    @Override
    public void bind(int sampler) {
        glActiveTexture(sampler);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    @Override
    public void cleanup() {
        glDeleteTextures(id);
    }

    @Override
    public int getWidth() {
        return target.getSize().x();
    }

    @Override
    public int getHeight() {
        return target.getSize().y();
    }
}
