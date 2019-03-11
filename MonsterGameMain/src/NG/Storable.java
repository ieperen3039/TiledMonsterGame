package NG;

import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * An Object of this type can be written to file using a {@link DataOutput} and read from that file using a {@link
 * DataInput}. This object must have a public constructor that accepts a {@link DataInput} object as only parameter.
 * Implementing classes may not be inner classes (until a workaround has been found)
 * @author Geert van Ieperen. Created on 28-9-2018.
 */
public interface Storable {
    /**
     * writes all the state data of this object to the given output. This object can be recovered by calling the
     * appropriate constructor that specifies to be the inverse of this method.
     * @param out any stream that reliably transfers data
     * @throws IOException if an I/O error occurs while writing data to the stream.
     * @implNote The data must be written in exactly the same order as the specified constructor reads.
     * @see #write(DataOutput, Storable)
     * @see #read(DataInput, Class)
     */
    void writeToDataStream(DataOutput out) throws IOException;

    /**
     * Writes the given object to the given data stream. The object must have a constructor that accepts exactly a
     * {@link DataInput} as only parameter. The object can be restored with a call to {@link #read(DataInput, Class)}
     * @param out    the data stream where to put the data to
     * @param object the object to store
     * @throws IOException if an I/O error occurs while writing data to the stream.
     */
    static void write(DataOutput out, Storable object) throws IOException {
        if (object instanceof Enum) {
            writeEnum(out, (Enum) object);
            return;
        }

        writeClass(out, object.getClass());
        object.writeToDataStream(out);
    }

    /**
     * reads an object from the input stream, constructs it using a constructor that takes a {@link DataInput} object as
     * only argument.
     * @param in       a stream where this object has been written to.
     * @param expected the class that is expected to be read from the stream.
     * @throws IOException        if something goes wrong with reading the data from the input stream.
     * @throws ClassCastException if the read class instance can't be cast to T
     * @implNote An object written to a stream using {@link #writeToDataStream(DataOutput)} should be {@link
     * Object#equals(Object) equal} to the object returned by this method.
     * @see #write(DataOutput, Storable)
     */
    static <T> T read(DataInput in, Class<T> expected) throws IOException, ClassNotFoundException {
        if (expected.isEnum()) {
            //noinspection unchecked
            return (T) readEnum(in, (Class<? extends Enum>) expected);
        }

        Class<? extends T> foundClass = null;

        // collect classes of arguments
        try {
            foundClass = readClass(in, expected);

            // find and call constructor
            Constructor<? extends T> constructor = foundClass.getDeclaredConstructor(DataInput.class);
            return constructor.newInstance(in);

        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IOException("Could not access the constructor of type " + foundClass, ex);

        } catch (InvocationTargetException ex) {
            String exception = ex.getCause().getClass().getSimpleName();
            throw new IOException("Initializer caused an " + exception, ex.getCause());

        } catch (NoSuchMethodException ex) {
            throw new IOException(foundClass + " has no constructor that accepts as argument a DataInput class", ex);

        }
    }

    static void writeEnum(DataOutput out, Enum object) throws IOException {
        out.writeUTF(object.toString());
    }

    static <T extends Enum> T readEnum(DataInput in, Class<T> expected) throws IOException {
        String enumName = in.readUTF();
        T[] constants = expected.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException(expected + " is not an Enum class");
        return Toolbox.findClosest(enumName, constants);
    }

    /**
     * writes a class object to file by writing package and name as a string.
     * @see #readClass(DataInput, Class)
     */
    static void writeClass(DataOutput out, Class<?> theClass) throws IOException {
        String name = theClass.getName();
        out.writeUTF(name);
    }

    /**
     * reads a class object from the data stream
     * @param in       the data stream
     * @param expected a superclass of the expected read class. This is used to cast the return value to the desired
     *                 class.
     * @param <T>      The type of the expected class
     * @return a subclass of the expected class, as it was written to the stream.
     * @throws IOException            if the next data on the stream does not represent an UTF string
     * @throws IOException            if an exception occurs while reading the stream
     * @throws ClassNotFoundException if the found class can not be cast to T
     */
    static <T> Class<? extends T> readClass(DataInput in, Class<T> expected)
            throws IOException, ClassNotFoundException {
        String className = in.readUTF();
        Class<?> foundClass = Class.forName(className);

        if (expected.isAssignableFrom(foundClass)) {
            //noinspection unchecked
            return (Class<? extends T>) foundClass;

        } else {
            throw new ClassCastException("Found " + foundClass + " which does not implement or override " + expected);
        }

    }

    static void writeVector3f(DataOutput out, Vector3fc vec) throws IOException {
        out.writeFloat(vec.x());
        out.writeFloat(vec.y());
        out.writeFloat(vec.z());
    }

    static Vector3f readVector3f(DataInput in) throws IOException {
        return new Vector3f(
                in.readFloat(),
                in.readFloat(),
                in.readFloat()
        );
    }

    static void writeVector4f(DataOutput out, Vector4fc vec) throws IOException {
        out.writeFloat(vec.x());
        out.writeFloat(vec.y());
        out.writeFloat(vec.z());
        out.writeFloat(vec.w());
    }

    static Vector4f readVector4f(DataInput in) throws IOException {
        return new Vector4f(
                in.readFloat(),
                in.readFloat(),
                in.readFloat(),
                in.readFloat()
        );
    }

    static void writeQuaternionf(DataOutput out, Quaternionfc vec) throws IOException {
        out.writeFloat(vec.x());
        out.writeFloat(vec.y());
        out.writeFloat(vec.z());
        out.writeFloat(vec.w());
    }

    static Quaternionf readQuaternionf(DataInput in) throws IOException {
        return new Quaternionf(
                in.readFloat(),
                in.readFloat(),
                in.readFloat(),
                in.readFloat()
        );
    }

    static void writeCollection(DataOutput out, Collection<? extends Storable> box) throws IOException {
        out.writeInt(box.size());
        for (Storable s : box) {
            Storable.write(out, s);
        }
    }

    /**
     * reads a collection from the stream, and stores it in an {@link ArrayList}
     * @param in       the data input stream
     * @param expected the class of the elements
     * @param <T>      the actual type of the elements
     * @return an {@link ArrayList} with the values read from the input stream
     * @throws IOException            if an exception occurs while reading the stream
     * @throws ClassNotFoundException if any element of the map has an unknown or not-loaded class
     * @throws ClassCastException     if any of the elements can not be cast to T
     * @see #writeCollection(DataOutput, Collection)
     */
    static <T> List<T> readCollection(DataInput in, Class<T> expected) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        ArrayList<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            T entity = Storable.read(in, expected);
            list.add(entity);
        }
        return list;
    }

    static void writeMatrix4f(DataOutput out, Matrix4fc mat) throws IOException {
        byte[] bytes = new byte[Float.BYTES * 16];

        float[] array = new float[16];
        ByteBuffer.wrap(bytes).asFloatBuffer().put(mat.get(array));

        out.write(bytes);
    }

    static Matrix4f readMatrix4f(DataInput in) throws IOException {
        byte[] bytes = new byte[Float.BYTES * 16];
        in.readFully(bytes);

        float[] array = new float[16];
        ByteBuffer.wrap(bytes).asFloatBuffer().get(array);

        Matrix4f mat = new Matrix4f();
        mat.set(array).determineProperties();

        return mat;
    }

    static <K extends Storable, V extends Storable> void writeMap(DataOutput out, Map<K, V> map) throws IOException {
        out.writeInt(map.size());
        Set<K> keySet = map.keySet();

        for (K key : keySet) {
            Storable.write(out, key);
            Storable.write(out, map.get(key));
        }
    }

    /**
     * reads a map from the stream and stores it in a {@link HashMap}. If the keyClass is an {@link Enum}, an {@link
     * EnumMap} is returned instead.
     * @param in         the data input stream
     * @param keyClass   the class of the keys of the map
     * @param valueClass the class of the values in the map
     * @param <K>        the type of the keys
     * @param <V>        the type of the values
     * @return a {@link HashMap} or {@link EnumMap} with the values read from the stream
     * @throws IOException            if an exception occurs while reading the stream
     * @throws ClassNotFoundException if any element of the map has an unknown or not-loaded class
     * @throws ClassCastException     if any of the keys can not be cast to K, or any of the values can not be cast to
     *                                V
     * @see #writeMap(DataOutput, Map)
     */
    static <K, V> Map<K, V> readMap(DataInput in, Class<K> keyClass, Class<V> valueClass)
            throws IOException, ClassNotFoundException {

        int size = in.readInt();
        Map<K, V> map = new HashMap<>(size);


        for (int i = 0; i < size; i++) {
            K key = Storable.read(in, keyClass);
            V value = Storable.read(in, valueClass);
            map.put(key, value);
        }

        if (keyClass.isEnum()) {
            //noinspection unchecked
            Map<? extends Enum, V> enumMap = (Map<? extends Enum, V>) map;
            //noinspection unchecked
            return new EnumMap<>(enumMap);

        } else {
            return map;
        }
    }

    /**
     * Writes this object to a file, which can be recovered with either {@link #readFromFile(File, Class)} or {@link
     * #read(DataInput, Class)}.
     * @param file the file to write to, existing or not.
     */
    default void writeToFile(File file) {
        try (OutputStream fileStream = new FileOutputStream(file)) {
            DataOutput out = new DataOutputStream(fileStream);
            Logger.DEBUG.print("Writing " + file, this);
            write(out, this);

        } catch (IOException ex) {
            Logger.ERROR.print(ex);
        }
    }

    /**
     * reads an object from file, returning null if any exception occurs.
     * @param file     the file to read
     * @param expected the class that is expected to be in the file
     * @param <T>      the type of the returned object
     * @return the object from the file, cast to T
     * @throws IOException if anything goes wrong while reading the file
     * @see #readFromFileRequired(File, Class)
     */
    static <T> T readFromFile(File file, Class<T> expected) throws IOException, ClassNotFoundException {
        try (InputStream fileStream = new FileInputStream(file)) {
            DataInput in = new DataInputStream(fileStream);
            return read(in, expected);
        }
    }

    /** the version of {@link #readFromFile(File, Class)} that throws an Runtime exception when an error occurs */
    static <T> T readFromFileRequired(File file, Class<T> expected) {
        try {
            return readFromFile(file, expected);

        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
