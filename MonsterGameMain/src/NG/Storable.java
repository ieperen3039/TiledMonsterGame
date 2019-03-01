package NG;

import NG.Tools.Toolbox;
import org.joml.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
     * @see #writeToFile(DataOutput, Storable)
     * @see #readFromFile(DataInput, Class)
     */
    void writeToFile(DataOutput out) throws IOException;

    /**
     * Writes the given object to the given data stream. The object must have a constructor that accepts exactly a
     * {@link DataInput} as only parameter. The object can be restored with a call to {@link #readFromFile(DataInput,
     * Class)}
     * @param out    the data stream where to put the data to
     * @param object the object to store
     * @throws IOException if an I/O error occurs while writing data to the stream.
     */
    static void writeToFile(DataOutput out, Storable object) throws IOException {
        if (object instanceof Enum) {
            Enum asEnum = (Enum) object;
            out.writeUTF(asEnum.toString());
            return;
        }

        writeClass(out, object.getClass());
        object.writeToFile(out);
    }

    /**
     * reads an object from the input stream, constructs it using a constructor that takes a {@link DataInput} object as
     * only argument.
     * @param in       a stream where this object has been written to.
     * @param expected the class that is expected to be read from the stream.
     * @throws IOException if something goes wrong with reading the data from the input stream.
     * @throws ClassCastException if the read class instance can't be cast to T
     * @implNote An object written to a stream using {@link #writeToFile(DataOutput)} should be {@link
     * Object#equals(Object) equal} to the object returned by this method.
     * @see #writeToFile(DataOutput, Storable)
     */
    static <T> T readFromFile(DataInput in, Class<T> expected) throws IOException, ClassNotFoundException {
        if (expected.isEnum()) {
            String enumName = in.readUTF();
            return Toolbox.findClosest(enumName, expected.getEnumConstants());
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

    static void writeCollection(DataOutput out, Collection<Storable> box) throws IOException {
        out.writeInt(box.size());
        for (Storable s : box) {
            Storable.writeToFile(out, s);
        }
    }

    /**
     * reads a collection from the stream, and stores it in a {@link List}
     * @param in       the data input stream
     * @param expected the class of the elements
     * @param <T>      the actual type of the elements
     * @return an {@link ArrayList} with the values read from the input stream
     * @throws IOException            if an exception occurs while reading the stream
     * @throws ClassNotFoundException if any of the elements can not be cast to T
     */
    static <T> List<T> readCollection(DataInput in, Class<T> expected) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        ArrayList<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            T entity = Storable.readFromFile(in, expected);
            list.add(entity);
        }
        return list;
    }
}
