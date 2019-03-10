package NG.Rendering.MeshLoading;

import NG.DataStructures.Generic.Color4f;
import NG.Tools.Toolbox;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * <h1> Introduction </h1>
 * Class to load 3D models (meshes) that have been stored in the Polygon File Format (.ply). The advantage of PLY over
 * for example OBJ is that PLY by default also contains vertex color information. For the Dungeons and Drawings game,
 * this makes it easier to maintain a low poly style. After either finding or creating a low-poly model in Blender, the
 * model can be colored using Blenders vertex-paint mode. All the vertex data (texture coordinates, color data,
 * position, etc.) can then be exported to a .ply file.
 *
 * <h1> The file format </h1>
 * The .ply format is a relatively simple, text-based (ASCII) format. There is also a binary version, but this is for
 * now not supported. The file is organised in two parts.
 *
 * <ol>
 * <li> A header that specifies the elements of the mesh and their types </li>
 * <li> The body which contains a list of said elements (e.g. vertices and faces) </li>
 * </ol>
 *
 * <h2> The header </h2>
 * <ol>
 * <li>The first line starts with {@code ply}</li>
 * <li>The second line indicates the version. For us, this should always be {@code format ascii 1.0}</li>
 * <li>In the header, a comment can be added by starting the line with {@code comment}</li>
 * <li>An element is introduced with the {@code element <name> <amount>} line. <amount> is the amount of elements
 * there are of this type and <name> is, as can be expected, the name of this type of element (e.g. 'vertex').</li>
 * <li>An property of an element is introduced with the {@code property <type> <name>} line. Where the <type> is the
 * type of the variable (either char, uchar, short, ushort, int, uint, float, or double) and <name> is the name of the
 * property. The {@code list <amount>} keyword can be used to indicate a list of <amount> values.</li>
 * <li>The header is ended with the {@code end_header} keyword</li>
 * </ol>
 *
 * <h2> The body </h2>
 * The body then lists the elements in the order they are introduced in the header. Each element has its own line which
 * consists of the values for all properties separated by a space.
 *
 * <h1> Limitations </h1>
 * For now, the file format we can read is constrained to the following: Object vertex = (float[3] position, float[3]
 * normal, uchar[3] rgb_color) Object face = (list<int> vertext_indices)
 * @author Cas Wognum
 * @author Geert van Ieperen
 */
public class PLYFile implements MeshFile {
    private final String name;
    private final List<Vector3fc> vertices;
    private final List<Vector3fc> normals;
    private final List<Color4f> colors;
    private final List<Mesh.Face> faces;
    private int nrOfVertices = -1;
    private int nrOfFaces = -1;

    /**
     * Starts the parsing of a new .ply file. Opens the file as a list of strings where each String corresponds to a
     * line in the .ply file. Each line is then processed one by one and at the end the data is restructured to be
     * usable by the Mesh class.
     * @param path the path to the .ply file to parse
     * @throws IOException if file not found
     * @throws IOException if file format not supported
     */
    public PLYFile(Vector3fc offSet, Vector3fc scale, Path path, String name) throws IOException {
        this.name = name;
        vertices = new ArrayList<>();
        normals = new ArrayList<>();
        colors = new ArrayList<>();
        faces = new ArrayList<>();

        // Open the file as a list of strings
        List<String> lines = Files.readAllLines(path);

        // Check if the file format is correct
        int endHeader = lines.indexOf("end_header");
        if (endHeader == -1) {
            throw new IOException("PLYLoader.loadMesh() failed: Unsupported file format. " +
                    "'end_header' keyword is missing");
        }

        List<String> header = new ArrayList<>(lines.subList(0, endHeader)); // exclude "end_header"
        List<String> body = new ArrayList<>(lines.subList(endHeader + 1, lines.size()));

        // Check the header and query the amount of vertices and faces
        parseFileFormat(header);

        // Parse all vertices
        for (int i = 0; i < nrOfVertices; i++) {
            String[] tokens = Toolbox.WHITESPACE_PATTERN.split(body.get(i));

            // Position vector data
            vertices.add(
                    new Vector3f(
                            Float.parseFloat(tokens[0]),
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])
                    )
                            .mul(scale)
                            .add(offSet)
            );

            // Normal vector data
            normals.add(new Vector3f(
                    Float.parseFloat(tokens[3]),
                    Float.parseFloat(tokens[4]),
                    Float.parseFloat(tokens[5])
            ));

            // Color data
            colors.add(Color4f.rgb(
                    Integer.parseInt(tokens[6]),
                    Integer.parseInt(tokens[7]),
                    Integer.parseInt(tokens[8])
            ));
        }

        // Parse all faces
        for (int i = nrOfVertices; i < nrOfVertices + nrOfFaces; i++) {
            String[] s = Toolbox.WHITESPACE_PATTERN.split(body.get(i));
            faces.add(Mesh.Face.parsePLY(s));
        }
    }

    /**
     * Checks the file format to see if it is a format that is currently supported. It checks if the specifications in
     * the header coincide with the specifications we expect. Check is far from perfect, but helps in debugging.
     * Limitations; Correctness of properties is not checked and the amount of properties per element neither is
     * checked.
     * @param header The lines that make up the header
     */
    private void parseFileFormat(List<String> header) throws IOException {
        int numberOfProperties = 0;

        for (String line : header) {
            String[] tokens = Toolbox.WHITESPACE_PATTERN.split(line);

            switch (tokens[0]) {
                case "ply":
                case "comment":
                    break;
                case "format":
                    if (!tokens[1].equals("ascii")) {
                        throw new IOException("Not the ASCII format, but " + tokens[1]);
                    }
                    break;
                case "element":
                    if (tokens[1].equals("vertex")) {
                        assert nrOfVertices == -1;
                        nrOfVertices = Integer.parseInt(tokens[2]);

                    } else if (tokens[1].equals("face")) {
                        assert nrOfFaces == -1;
                        nrOfFaces = Integer.parseInt(tokens[2]);

                    } else {
                        throw new IOException("Unsupported element " + tokens[1]);
                    }
                    break;
                case "property":
                    numberOfProperties++;
                    break;
                default:
                    // TODO allow ignored elements
                    throw new IOException("Unsupported keyword " + tokens[0]);
            }
        }

        // TODO allow variable properties
        if (numberOfProperties != 10) {
            throw new IOException("Wrong number of properties " + numberOfProperties);
        }
    }


    @Override
    public List<Vector2fc> getTextureCoords() {
        return Collections.emptyList();
    }

    @Override
    public List<Vector3fc> getVertices() {
        return vertices;
    }

    @Override
    public List<Vector3fc> getNormals() {
        return normals;
    }

    @Override
    public List<Color4f> getColors() {
        return colors;
    }

    @Override
    public List<Mesh.Face> getFaces() {
        return faces;
    }

    @Override
    public String toString() {
        return "PLY file " + name;
    }
}
