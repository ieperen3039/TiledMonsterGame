package NG.Particles;

import NG.Core.Game;
import NG.Core.GameAspect;
import NG.Core.GameTimer;
import NG.Rendering.MatrixStack.SGL;
import NG.Tools.AutoLock;

import java.util.ArrayList;
import java.util.List;

/**
 * ordinary container for particles
 * @author Geert van Ieperen created on 3-4-2019.
 */
public class GameParticles implements GameAspect {
    private final List<ParticleCloud> particles;
    private final AutoLock newLock;
    private ParticleCloud newParticles = null;
    private Game game;

    public GameParticles() {
        this.particles = new ArrayList<>();

        newLock = new AutoLock.Instance();
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
    }

    public void add(ParticleCloud cloud) {
        try (AutoLock.Instance.Section ignored = newLock.open()) {
            if (newParticles == null) {
                newParticles = cloud;
            } else {
                newParticles.addAll(cloud);
            }
        }
    }

    public void draw(SGL gl) {
        float now = game.get(GameTimer.class).getRendertime();

        particles.removeIf(cloud -> cloud.disposeIfFaded(now));

        if (newParticles != null) {
            try (AutoLock.Section ignored = newLock.open()) {
                newParticles.granulate()
                        .peek(ParticleCloud::writeToGL)
                        .forEach(particles::add);

                newParticles = null;
            }
        }

        for (ParticleCloud cloud : particles) {
            gl.render(cloud, null);
        }
    }

    @Override
    public void cleanup() {
        newLock.lock();

        particles.forEach(ParticleCloud::dispose);
        particles.clear();

        if (newParticles != null) {
            newParticles.dispose();
            newParticles = null;
        }

        newLock.unlock();
    }
}
