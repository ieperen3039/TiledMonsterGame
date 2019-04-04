package NG.Particles;

import NG.DataStructures.Generic.Color4f;
import NG.Rendering.MatrixStack.MatrixStack;
import NG.Rendering.Shapes.Primitives.Plane;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility-class for particles
 * @author Geert van Ieperen created on 12-1-2018.
 */
public final class Particles {
    public static final float FIRE_LINGER_TIME = 10f;
    public static final int EXPLOSION_BASE_DENSITY = 1000;
    private static final float PARTICLE_MODIFIER = 1f;
    private static final float FIRE_PARTICLE_SIZE = 1f;

    /**
     * creates an explosion of particles from the given position, mixing the given color with white
     * @param position  source position where all particles come from
     * @param direction movement of the average position of the cloud
     * @param color2    second color extreme. Each particle has a color where each color primitive is individually
     *                  randomized.
     * @param power     the speed of the fastest particle relative to the middle of the cloud
     * @return a new explosion, not written to the GPU yet.
     */
    public static ParticleCloud explosion(Vector3fc position, Vector3fc direction, Color4f color2, float power) {
        return explosion(
                position, direction, Color4f.WHITE, color2,
                (int) (EXPLOSION_BASE_DENSITY * PARTICLE_MODIFIER),
                FIRE_LINGER_TIME, FIRE_PARTICLE_SIZE
        );
    }

    /**
     * creates an explosion of particles from the given position, using a blend of the two colors
     * @param position     source position where all particles come from
     * @param direction    movement of the average position of the cloud
     * @param color1       first color extreme
     * @param color2       second color extreme. Each particle has a color between color1 and color2
     * @param density      the number of particles generated
     * @param lingerTime   the maximal lifetime of the particles. Actual duration is exponentially distributed.
     * @param particleSize roughly the actual size of the particle
     * @return a new explosion, not written to the GPU yet.
     */
    public static ParticleCloud explosion(
            Vector3fc position, Vector3fc direction, Color4f color1, Color4f color2,
            int density, float lingerTime, float particleSize
    ) {
        ParticleCloud result = new ParticleCloud();

        for (int i = 0; i < (density); i++) {
            Vector3f movement = Vectors.randomOrb();
            movement.add(direction);

            float rand = Toolbox.random.nextFloat();
            Color4f interColor = color1.interpolateTo(color2, rand);

            result.addParticle(position, movement, interColor, lingerTime);
        }

        return result;
    }

    private static ParticleCloud getParticles(
            Collection<Vector3f[]> splittedTriangles, Vector3fc launchDir, float jitter,
            float deprecationTime, float speed, Color4f particleColor
    ) {
        ParticleCloud particles = new ParticleCloud();
        for (Vector3f[] p : splittedTriangles) {
            Vector3f movement = new Vector3f();
            Vector3f random = Vectors.randomOrb();

            float randFloat = Toolbox.random.nextFloat();

            movement = random.mul(jitter * speed * (1 - randFloat), movement);
            movement.add(launchDir);

            Vector3f avg = p[0].add(p[1]).add(p[2]).div(3f);

            particles.addParticle(
                    avg, movement, particleColor, deprecationTime
            );
        }
        return particles;
    }

    /**
     * splits the given triangles in smaller triangles
     * @param splits number of iterations. the number of resulting triangles grows exponentially
     * @return triangles in the same definition as the input triangles
     */
    private static Collection<Vector3fc[]> triangulate(Collection<Vector3fc[]> triangles, int splits) {
        for (int i = 0; i < splits; i++) {
            triangles = triangles.stream()
                    .flatMap(p -> splitTriangle(p[0], p[1], p[2]))
                    .collect(Collectors.toList());
        }
        return triangles;
    }

    /**
     * breaks the object up in triangles
     * @param ms translation matrix to world-space
     * @return collection of these triangles in world-space
     */
    private static Collection<Vector3fc[]> asTriangles(Plane targetPlane, MatrixStack ms) {
        Collection<Vector3fc[]> triangles = new ArrayList<>();
        Iterator<Vector3fc> border = targetPlane.getBorder().iterator();

        // split into triangles and add those
        Vector3fc A, B, C;
        try {
            A = ms.getPosition(border.next());
            B = ms.getPosition(border.next());
            C = ms.getPosition(border.next());
        } catch (NoSuchElementException ex) {
            // a plane without at least two edges can not be split
            throw new IllegalArgumentException("Plane with less than three vertices can not be split", ex);
        }

        triangles.add(new Vector3fc[]{A, B, C});

        while (border.hasNext()) {
            A = B;
            B = C;
            C = ms.getPosition(border.next());
            triangles.add(new Vector3fc[]{A, B, C});
        }
        return triangles;
    }

    /**
     * creates four particles splitting the triangle between the given coordinates like the triforce (Zelda)
     * @return Collection of four Particles
     */
    private static Stream<Vector3f[]> splitTriangle(Vector3fc A, Vector3fc B, Vector3fc C) {
        Stream.Builder<Vector3f[]> particles = Stream.builder();

        final Vector3fc AtoB = new Vector3f(A).lerp(B, 0.5f);
        final Vector3fc AtoC = new Vector3f(A).lerp(C, 0.5f);
        final Vector3fc BtoC = new Vector3f(B).lerp(C, 0.5f);

        particles.add(new Vector3f[]{
                new Vector3f(A), new Vector3f(AtoB), new Vector3f(AtoC)
        });

        particles.add(new Vector3f[]{
                new Vector3f(B), new Vector3f(BtoC), new Vector3f(AtoB)
        });

        particles.add(new Vector3f[]{
                new Vector3f(C), new Vector3f(BtoC), new Vector3f(AtoC)
        });

        particles.add(new Vector3f[]{
                new Vector3f(AtoB), new Vector3f(AtoC), new Vector3f(BtoC)
        });

        return particles.build();
    }
}
