package NG.Rendering.Shaders;

import NG.Engine.Game;

/**
 * @author Geert van Ieperen created on 7-1-2018.
 */
public interface ShaderProgram {

    /** shaders and meshes must use these shader locations */
    int VERTEX_LOCATION = 0;
    int NORMAL_LOCATION = 1;
    int COLOR_LOCATION = 2;
    int TEXTURE_LOCATION = 3;

    /**
     * Bind the renderer to the current rendering state
     */
    void bind();

    /**
     * Unbind the renderer from the current rendering state
     */
    void unbind();

    /**
     * Cleanup the renderer to a state of disposal
     */
    void cleanup();

    /**
     * initialize the uniforms for this shader. Must be called before rendering.
     * @param game the source of information
     */
    void initialize(Game game);
}
