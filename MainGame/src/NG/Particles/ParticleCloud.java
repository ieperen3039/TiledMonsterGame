package NG.Particles;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.MeshLoading.Mesh;
import NG.Tools.Toolbox;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * a group of particles, each rendered around the same time
 * @author Geert van Ieperen created on 16-5-2018.
 */
public class ParticleCloud implements Mesh, Externalizable {
    private static final float PARTICLECLOUD_MIN_TIME = 0.5f;
    private static final int PARTICLECLOUD_SPLIT_SIZE = 1000;
    private int vaoId = -1;
    private int posMidVboID;
    private int moveVboID;
    private int colorVboID;
    private int timeVboID;
    private int randVboID;

    private boolean isLoaded = false;
    private List<Particle> bulk = new ArrayList<>();
    private float minStartTime = Float.POSITIVE_INFINITY;
    private float minEndTime = Float.POSITIVE_INFINITY;
    private float maxEndTime = Float.NEGATIVE_INFINITY;
    private int nrOfParticles = 0;

    /**
     * @param position   position of the middle of the particle
     * @param color      color of this particle
     * @param movement   movement of this particle in one second
     * @param startTime
     * @param timeToLive
     */
    public void addParticle(
            Vector3fc position, Vector3fc movement, Color4f color, float startTime, float timeToLive
    ) {
        addParticle(new Particle(position, movement, color, startTime, timeToLive));
    }

    private void addParticle(Particle p) {
        bulk.add(p);
        minStartTime = Math.min(p.startTime, minStartTime);
        minEndTime = Math.min(p.endTime, minEndTime);
        maxEndTime = Math.max(p.endTime, maxEndTime);
    }

    public void writeToGL() {
        nrOfParticles = bulk.size();

        FloatBuffer positionBuffer = MemoryUtil.memAllocFloat(3 * nrOfParticles);
        FloatBuffer moveBuffer = MemoryUtil.memAllocFloat(3 * nrOfParticles);
        FloatBuffer colorBuffer = MemoryUtil.memAllocFloat(4 * nrOfParticles);
        FloatBuffer timeBuffer = MemoryUtil.memAllocFloat(2 * nrOfParticles);
        IntBuffer randomBuffer = MemoryUtil.memAllocInt(nrOfParticles);

        final int[] randInts = Toolbox.random.ints(nrOfParticles).toArray();
        randomBuffer.put(randInts);
        randomBuffer.rewind();

        for (int i = 0; i < bulk.size(); i++) {
            Particle p = bulk.get(i);

            p.position.get(i * 3, positionBuffer);
            p.movement.get(i * 3, moveBuffer);

            p.color.put(colorBuffer);
            timeBuffer.put(p.startTime);
            timeBuffer.put(p.endTime);
        }

        // position and movement do not require flipping
        colorBuffer.flip();
        timeBuffer.flip();

        try {
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            posMidVboID = loadToGL(positionBuffer, 0, 3); // Position of triangle middle VBO
            moveVboID = loadToGL(moveBuffer, 1, 3); // Movement VBO
            colorVboID = loadToGL(colorBuffer, 2, 4); // Color VBO
            timeVboID = loadToGL(timeBuffer, 3, 2); // beginTime-endTime VBO

            randVboID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, randVboID);
            glBufferData(GL_ARRAY_BUFFER, randomBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(4, 1, GL_INT, false, 0, 0);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
            isLoaded = true;

        } finally {
            MemoryUtil.memFree(positionBuffer);
            MemoryUtil.memFree(moveBuffer);
            MemoryUtil.memFree(colorBuffer);
            MemoryUtil.memFree(timeBuffer);
            MemoryUtil.memFree(randomBuffer);
        }

        Toolbox.checkGLError(toString());
    }

    private static int loadToGL(FloatBuffer buffer, int index, int itemSize) {
        buffer.rewind();
        int vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glVertexAttribPointer(index, itemSize, GL_FLOAT, false, 0, 0);
        return vboID;
    }

    /** an estimate of the number of visible particles */
    public int estParticlesAt(float currentTime) {
        int count = 0;
        for (Particle p : bulk) {
            if (p.startTime > currentTime && p.endTime < currentTime) {
                count++;
            }
        }
        return count;
    }

    public Stream<ParticleCloud> granulate() {
        float despawnPeriod = maxEndTime - minEndTime;

        if ((despawnPeriod > PARTICLECLOUD_MIN_TIME) && (bulk.size() > PARTICLECLOUD_SPLIT_SIZE)) {
            float mid = minEndTime + (despawnPeriod / 2);

            ParticleCloud newCloud = splitOff(mid);
            return Stream.concat(this.granulate(), newCloud.granulate());
        }

        return Stream.of(this);
    }

    /**
     * @param maxEndTime the new maximum end time of these particles
     * @return a cloud of particles that end later than the given end time
     */
    public ParticleCloud splitOff(float maxEndTime) {
        ParticleCloud newCloud = new ParticleCloud();
        ArrayList<Particle> newBulk = new ArrayList<>();

        for (Particle p : bulk) {
            if (p.endTime > maxEndTime) {
                newCloud.addParticle(p);

            } else {
                newBulk.add(p);
            }
        }

        this.maxEndTime = maxEndTime;
        this.bulk = newBulk;

        return newCloud;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(bulk);
        out.writeFloat(minStartTime);
        out.writeFloat(minEndTime);
        out.writeFloat(maxEndTime);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        //noinspection unchecked
        bulk = (List<Particle>) in.readObject();
        minStartTime = in.readFloat();
        minEndTime = in.readFloat();
        maxEndTime = in.readFloat();
        isLoaded = false;
    }

    /**
     * renders all particles. The particle-shader must be linked first, and writeToGl must be called
     */
    @Override
    public void render(SGL.Painter lock) {
        if (!isLoaded) writeToGL();

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0); // Position of triangle middle VBO
        glEnableVertexAttribArray(1); // Movement VBO
        glEnableVertexAttribArray(2); // Color VBO
        glEnableVertexAttribArray(3); // Time VBO
        glEnableVertexAttribArray(4); // Rand VBO

        glDrawArrays(GL_POINTS, 0, nrOfParticles);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
        glBindVertexArray(0);
    }

    public boolean hasFaded(float currentTime) {
        return currentTime > maxEndTime;
    }

    /**
     * @return true iff this cloud should be disposed
     */
    public boolean disposeIfFaded(float currentTime) {
        if (vaoId == 0) return true;

        if (hasFaded(currentTime)) {
            dispose();
            return true;
        }
        return false;
    }

    public void dispose() {
        bulk.clear();
        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(new int[]{posMidVboID, moveVboID, colorVboID, timeVboID, randVboID});

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
        vaoId = 0;
        Toolbox.checkGLError(toString());
    }

    /**
     * merges the other queued particles into this particle Note! this only works before {@link #writeToGL()} is called
     * @param other another particle cloud. The other will not be modified
     * @throws NullPointerException if this or the other has called {@link #writeToGL()} previously
     */
    public void addAll(ParticleCloud other) {
        this.bulk.addAll(other.bulk);
        this.minEndTime = Math.min(this.minEndTime, other.minEndTime);
        this.maxEndTime = Math.max(this.maxEndTime, other.maxEndTime);
        this.minStartTime = Math.min(this.minStartTime, other.minStartTime);
    }

    /**
     * record class for particles
     */
    private static class Particle implements Serializable {
        public final Vector3fc position;
        public final Vector3fc movement;
        public final Color4f color;
        public final float startTime;
        public final float endTime;

        public Particle(Vector3fc position, Vector3fc movement, Color4f color, float startTime, float timeToLive) {
            assert timeToLive > 0;
            this.position = position;
            this.movement = movement;
            this.color = color;
            this.startTime = startTime;
            this.endTime = startTime + timeToLive;
        }
    }
}
