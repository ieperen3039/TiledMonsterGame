package NG.DataStructures;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * An Object of this type can be written to file using a {@link DataOutput} and read from that file using a {@link
 * DataInput}. This object must have a public constructor that accepts a {@link DataInput} object as only parameter.
 * @author Geert van Ieperen. Created on 28-9-2018.
 */
public interface Storable {
    /**
     * writes all the state data of this object to the given output. This object can be recovered by calling this
     * object's constructor that accepts a {@link DataInput} as its only field.
     * @param out any stream that reliably transfers data
     * @throws IOException if an I/O error occurs while writing data to the stream.
     * @implNote The data must be written in exactly the same order as the specified constructor reads.
     */
    void writeToFile(DataOutput out) throws IOException;

    /**
     * Writes the given object to the given data stream. The object can be safely restored with a call to {@link
     * #readFromFile(DataInput, Class)}
     * @param object the object to store
     * @param out    the data stream where to put the data to
     * @throws IOException if an I/O error occurs while writing data to the stream.
     */
    static void writeToFile(Storable object, DataOutput out) throws IOException {
        String name = object.getClass().getName();
        out.writeChars(name);
        out.writeChar('\n');
        object.writeToFile(out);
    }

    /**
     * reads an object from the input stream, constructs it using a constructor that takes a {@link DataInput} object as
     * only argument.
     * @param in a stream where this object has been written to.
     * @throws IOException if something goes wrong with reading the data from the input stream.
     * @implNote An object written to a stream using {@link #writeToFile(DataOutput)} should {@link
     * Object#equals(Object)} an object constructed with a no-arg constructor, where this method is called on the other
     * end of that same stream.
     */
    static <T> T readFromFile(DataInput in, Class<T> expected) throws IOException, ClassNotFoundException {
        String className = in.readLine();
        Class<?> foundClass = Class.forName(className);

        if (expected.isAssignableFrom(foundClass)) {
            try {
                Constructor<T> constructor = expected.getDeclaredConstructor(in.getClass());
                return constructor.newInstance(in);

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                throw new IOException("Could not instantiate a constructor of type " + foundClass, ex);

            } catch (NoSuchMethodException e) {
                throw new IOException("Class " + foundClass + " has no constructor that accepts DataInput as argument");
            }
        } else {
            throw new ClassCastException("Found class " + foundClass + " which does not implement or override " + expected);
        }
    }
}
