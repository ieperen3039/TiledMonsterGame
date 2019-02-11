package NG;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

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
     * @see #readFromFile(DataInput, Class, Object[])
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
        writeToFile(out, object, DataInput.class);
    }

    /**
     * reads an object from the input stream, constructs it using a constructor that takes a {@link DataInput} object as
     * only argument.
     * @param in       a stream where this object has been written to.
     * @param expected the class that is expected to be read from the stream.
     * @throws IOException if something goes wrong with reading the data from the input stream.
     * @implNote An object written to a stream using {@link #writeToFile(DataOutput)} should {@link
     * Object#equals(Object)} an object constructed with a no-arg constructor, where this method is called on the other
     * end of that same stream.
     * @see #readFromFile(DataInput, Class, Object...)
     */
    static <T> T readFromFile(DataInput in, Class<T> expected) throws IOException, ClassNotFoundException {
        return readFromFile(in, expected, in);
    }

    /**
     * Writes the given object to the given data stream. The object must have a constructor that accepts exactly a
     * {@link DataInput} as only parameter. The object can be restored with a call to {@link #readFromFile(DataInput,
     * Class)}
     * @param out       the data stream where to put the data to
     * @param object    the object to store
     * @param arguments the classes of the arguments that must be used to restore the state of the object.
     * @throws IOException if an I/O error occurs while writing data to the stream.
     */
    static void writeToFile(DataOutput out, Storable object, Class<?>... arguments) throws IOException {
        writeClass(out, object.getClass());

        out.writeInt(arguments.length);
        for (Class argument_i : arguments) {
            writeClass(out, argument_i);
        }

        object.writeToFile(out);
    }

    /**
     * reads an object from the input stream, constructs it using a constructor that takes the given parameters as
     * arguments. The arguments must be ordered as how they are declared in the constructor.
     * @param in        a stream where this object has been written to.
     * @param expected  the class that is expected to be read from the stream.
     * @param arguments an array of which arguments the required class has. If the constructor's declaring class is an
     *                  inner class in a non-static context, the first argument to the constructor needs to be the
     *                  enclosing instance.
     * @throws IOException              if something goes wrong with reading the data from the input stream.
     * @throws ClassNotFoundException   if the class on the stream is not a loaded class
     * @throws ClassCastException       if the class on the stream is no subclass of the expected class
     * @throws IllegalArgumentException if the arguments of this method could not instantiate the given class.
     * @implNote An object written to a stream using {@link #writeToFile(DataOutput)} should {@link
     * Object#equals(Object)} an object constructed with a no-arg constructor, where this method is called on the other
     * end of that same stream.
     */
    static <T> T readFromFile(DataInput in, Class<T> expected, Object... arguments)
            throws IOException, ClassNotFoundException {

        Class<?> foundClass = readClass(in);
        if (expected.isAssignableFrom(foundClass)) {

            int nOfArguments = in.readInt();
            if (nOfArguments == arguments.length) {
                // collect classes of arguments
                Class<?>[] argClasses = new Class<?>[nOfArguments];
                try {
                    for (int i = 0; i < nOfArguments; i++) {
                        argClasses[i] = readClass(in);
                    }

                    // find and call constructor
                    Class<? extends T> foundImpl = foundClass.asSubclass(expected);
                    Constructor<? extends T> constructor = foundImpl.getDeclaredConstructor(argClasses);
                    return constructor.newInstance(arguments);

                } catch (InstantiationException | IllegalAccessException ex) {
                    throw new IOException("Could not access the constructor of type " + foundClass, ex);

                } catch (InvocationTargetException ex) {
                    String exception = ex.getCause().getClass().getSimpleName();
                    throw new IOException("Initializer caused an " + exception, ex.getCause());

                } catch (NoSuchMethodException ex) {
                    throw new IOException(foundClass + " has no constructor that accepts as arguments: " + Arrays.toString(argClasses), ex);

                } catch (ClassNotFoundException ex) {
                    throw new IllegalArgumentException("The class of one of the arguments is unknown");
                }
            } else {
                throw new IllegalArgumentException("Invalid number of arguments given: " + arguments.length + " where " + nOfArguments + " are required");
            }
        } else {
            throw new ClassCastException("Found " + foundClass + " which does not implement or override " + expected);
        }
    }

    static void writeClass(DataOutput out, Class<?> theClass) throws IOException {
        String name = theClass.getName();
        out.writeUTF(name);
    }

    static Class<?> readClass(DataInput in) throws IOException, ClassNotFoundException {
        String className = in.readUTF(); // works assuming that the class is loaded (and why should it not be)
        return Class.forName(className);
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
}
