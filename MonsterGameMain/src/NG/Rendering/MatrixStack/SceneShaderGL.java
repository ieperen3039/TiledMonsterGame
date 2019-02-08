package NG.Rendering.MatrixStack;

import NG.Camera.Camera;
import NG.Entities.Entity;
import NG.Rendering.Shaders.SceneShader;
import NG.Rendering.Shaders.ShaderProgram;
import org.joml.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Geert van Ieperen created on 16-11-2017.
 */
public class SceneShaderGL extends AbstractSGL {
    private final Matrix4f viewProjectionMatrix;
    private Matrix3f normalMatrix = new Matrix3f();

    private SceneShader shader;

    /**
     * @param shader       the shader to use for rendering
     * @param windowWidth  the width of the viewport in pixels
     * @param windowHeight the height of the viewport in pixels
     * @param viewpoint    the camera that defines eye position, focus and up vector
     * @param isometric    when true, no perspective transformation is used. This results in a retro tycoon style
     */
    public SceneShaderGL(SceneShader shader, int windowWidth, int windowHeight, Camera viewpoint, boolean isometric) {
        super();
        this.shader = shader;
        viewProjectionMatrix = SGL.getViewProjection(windowWidth, windowHeight, viewpoint, isometric);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, windowWidth, windowHeight);
        glEnable(GL_LINE_SMOOTH);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    @Override
    public void render(Mesh mesh, Entity sourceEntity) {
        Matrix4f modelMatrix = getModelMatrix();
        modelMatrix.normal(normalMatrix);

        shader.setProjectionMatrix(viewProjectionMatrix);
        shader.setModelMatrix(modelMatrix);
        shader.setNormalMatrix(normalMatrix);

        mesh.render(LOCK);
    }

    public ShaderProgram getShader() {
        return shader;
    }

    public Vector2f getPositionOnScreen(Vector3fc vertex) {
        Vector4f pos = new Vector4f(vertex, 1.0f);
        getProjection().transformProject(pos);
        if (pos.z() > 1) {
            return null;
        } else {
            return new Vector2f(pos.x(), pos.y());
        }
    }

    /** @return the view-projection matrix */
    public Matrix4fc getProjection() {
        return viewProjectionMatrix;
    }

    @Override
    public String toString() {
        return "ShaderUniformGL {\n" +
                "modelMatrix=" + getModelMatrix() +
                ", viewProjectionMatrix=" + viewProjectionMatrix +
                ", normalMatrix=" + normalMatrix +
                ", shader=" + shader.getClass() +
                "\n}";
    }
}
