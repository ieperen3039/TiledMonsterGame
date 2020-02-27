package NG.Rendering.MeshLoading;

import NG.Animations.SkeletonBone;
import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Generic.PairList;
import NG.Rendering.Shapes.BasicShape;
import NG.Rendering.Shapes.Shape;
import NG.Resources.FileResource;
import NG.Resources.Resource;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Vectors;
import org.joml.Vector2fc;
import org.joml.Vector3fc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * @author Geert van Ieperen created on 28-2-2019.
 */
public class MeshFile {
    private final List<Vector2fc> textureCoords;
    private final List<Vector3fc> vertices;
    private final List<Vector3fc> normals;
    private final List<Mesh.Face> faces;
    private final List<Color4f> colors;
    private final List<PairList<SkeletonBone, Float>> influences;
    private final String name;

    public MeshFile(
            String name, List<Vector2fc> textureCoords, List<Vector3fc> vertices,
            List<Vector3fc> normals, List<Mesh.Face> faces, List<Color4f> colors,
            List<PairList<SkeletonBone, Float>> influences
    ) {
        this.name = name;
        this.textureCoords = textureCoords;
        this.vertices = vertices;
        this.normals = normals;
        this.faces = faces;
        this.colors = colors;
        this.influences = influences;
    }

    public boolean isTextured() {
        return !getTextureCoords().isEmpty();
    }

    public boolean isColored() {
        return !getColors().isEmpty();
    }

    public List<Vector2fc> getTextureCoords() {
        return textureCoords;
    }

    public List<Vector3fc> getVertices() {
        return vertices;
    }

    public List<Vector3fc> getNormals() {
        return normals;
    }

    public List<Color4f> getColors() {
        return colors;
    }

    public List<Mesh.Face> getFaces() {
        return faces;
    }

    private static MeshFile loadFile(Path file, Vector3fc scaling) throws IOException {
        String fileName = file.getFileName().toString();

        assert fileName.contains(".") : fileName;
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        switch (extension) {
            case ".obj":
                return FileLoaders.loadOBJ(scaling, file, fileName);
            case ".ply":
                return FileLoaders.loadPLY(scaling, file, fileName);
            default:
                throw new UnsupportedMeshFileException(fileName);
        }
    }

    public Shape getShape() {
        return new BasicShape(vertices, normals, faces);
    }

    public Mesh getMesh() {
        if (isTextured()) {
            return new TexturedMesh(this);
        } else {
            return new FlatMesh(getVertices(), getNormals(), getFaces());
        }
    }

    public static Resource<MeshFile> createResource(Directory meshes, String... path) {
        return createResource(Vectors.Scaling.UNIFORM, meshes, path);
    }

    public static Resource<MeshFile> createResource(Vector3fc scaling, Directory meshes, String... path) {
        return FileResource.get((p) -> loadFile(p, scaling), meshes, path);
    }

    /**
     * writes an object to the given filename
     * @param targetFile
     * @throws IOException if any problem occurs while creating the file
     */
    public void writeOBJFile(File targetFile) throws IOException {
        PrintWriter writer = new PrintWriter(targetFile, StandardCharsets.UTF_8);
        writeOBJFile(writer);
        writer.close();
        Logger.DEBUG.print("Successfully created obj file: " + targetFile);
    }

    /**
     * writes an object to the given print writer
     * @param writer
     */
    public void writeOBJFile(PrintWriter writer) {
        writer.println("# created using a simple obj writer by Geert van Ieperen");
        writer.println("# calling method: " + Logger.getCallingMethod(2));

        for (Vector3fc vec : vertices) {
            writer.println(String.format(Locale.US, "v %1.08f %1.08f %1.08f", vec.x(), vec.y(), vec.z()));
        }

        for (Vector3fc norm : normals) {
            writer.println(String.format(Locale.US, "vn %1.08f %1.08f %1.08f", norm.x(), norm.y(), norm.z()));
        }

        for (Vector2fc texCoord : textureCoords) {
            writer.println(String.format(Locale.US, "vt %1.08f %1.08f", texCoord.x(), texCoord.y()));
        }

        writer.println("usemtl None");
        writer.println("s off");
        writer.println("");

        if (isTextured()) {
            for (Mesh.Face face : faces) {
                assert face.tex != null;
                writer.print("f ");
                for (int i = 0; i < face.size(); i++) {
                    writer.print(" " + String.format("%d/%d/%d", face.vert[i] + 1, face.tex[i] + 1, face.norm[i] + 1));
                }
                writer.println();
            }
        } else {

            for (Mesh.Face face : faces) {
                writer.print("f ");
                for (int i = 0; i < face.size(); i++) {
                    writer.print(" " + String.format("%d//%d", face.vert[i] + 1, face.norm[i] + 1));
                }
                writer.println();
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public static class UnsupportedMeshFileException extends IOException {
        public UnsupportedMeshFileException(String fileName) {
            super(fileName);
        }
    }
}
