package NG.Rendering.MatrixStack;

import NG.ActionHandling.ClickShader;
import NG.Camera.Camera;
import NG.Entities.Entity;
import NG.Rendering.Shaders.ShaderProgram;
import org.joml.Matrix4f;

/**
 * @author Geert van Ieperen created on 30-1-2019.
 */
public class ClickShaderGL extends AbstractSGL {
    private final ClickShader shader;
    private final Matrix4f viewProjectionMatrix;

    public ClickShaderGL(
            ClickShader shader, int windowWidth, int windowHeight, Camera viewpoint, boolean isometric
    ) {
        this.shader = shader;
        viewProjectionMatrix = SGL.getViewProjection(windowWidth, windowHeight, viewpoint, isometric);

    }

    @Override
    public void render(Mesh object, Entity sourceEntity) {
        shader.setEntity(sourceEntity);
        shader.setProjectionMatrix(viewProjectionMatrix);
        shader.setModelMatrix(getModelMatrix());
        object.render(LOCK);
        shader.unsetEntity();
    }

    public ShaderProgram getShader() {
        return shader;
    }
}
