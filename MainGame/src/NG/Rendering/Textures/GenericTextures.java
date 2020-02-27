package NG.Rendering.Textures;

/**
 * @author Geert van Ieperen created on 1-2-2019.
 */
public enum GenericTextures implements Texture {
    CHECKER("..", "check.png"),
    ;

    private Texture tex;

    GenericTextures(String... path) {
        tex = Texture.createResource(path).get();
    }

    @Override
    public void bind(int sampler) {
        tex.bind(sampler);
    }

    @Override
    public void cleanup() {
        tex.cleanup();
    }

    @Override
    public int getWidth() {
        return tex.getWidth();
    }

    @Override
    public int getHeight() {
        return tex.getHeight();
    }

    @Override
    public int getID() {
        return tex.getID();
    }
}
