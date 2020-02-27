package NG.Rendering.Textures;

import NG.Resources.FileResource;
import NG.Resources.Resource;
import NG.Tools.Directory;

/**
 * @author Geert van Ieperen created on 1-2-2019.
 */
public interface Texture {
    /**
     * activate this texture to be applied on the next model
     * @param sampler the texture slot to bind to
     */
    void bind(int sampler);

    /** destroy the resources claimed by the texture */
    void cleanup();

    int getWidth();

    int getHeight();

    int getID();

    static Resource<Texture> createResource(String... path) {
        return FileResource.get(FileTexture::new, Directory.images, path);
    }
}
