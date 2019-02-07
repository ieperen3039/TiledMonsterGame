package NG.Engine;

import NG.DataStructures.Storable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Geert van Ieperen. Created on 19-9-2018.
 */
public class Version implements Comparable<Version>, Storable {
    private final int major;
    private final int minor;

    public Version(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public static Version getFromInputStream(DataInput in) throws IOException {
        if (in.readChar() != 'v') {
            throw new IOException("Wrong data, expected a version number");
        }

        return new Version(in.readInt(), in.readInt());
    }

    public int major() {
        return major;
    }

    @Override
    public int compareTo(Version o) {
        return (major == o.major) ? Integer.compare(minor, o.minor) : Integer.compare(major, o.major);
    }

    public boolean isLessThan(int major, int minor) {
        return this.major == major ? this.minor < minor : this.major < major;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Version)) return false;
        Version other = (Version) obj;

        return major == other.major && minor == other.minor;
    }

    /**
     * checks whether this version is less than the given major and minor version, throwing an VersionMisMatch exception
     * if this is the case
     * @param major the major version required
     * @param minor the optional minor version, 0 if it doesn't matter
     * @throws MisMatchException if {@link this#isLessThan(int, int)}
     */
    public void requireAtLeast(int major, int minor) throws MisMatchException {
        if (this.isLessThan(major, minor)) {
            throw new MisMatchException(new Version(major, minor), this);
        }
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }

    @Override
    public void writeToFile(DataOutput out) throws IOException {
        out.writeChar('v');
        out.writeInt(major);
        out.writeInt(minor);
    }

    @Override
    public void readFromFile(DataInput in) throws IOException {
        if (in.readChar() != 'v') {
            throw new IOException("Wrong data, expected a version number");
        }

        in.skipBytes(2 * Integer.BYTES);
    }

    public static class MisMatchException extends Exception {
        public MisMatchException(Version required, Version current) {
            super("Version mismatch: required " + required + ", got " + current);
        }
    }
}
