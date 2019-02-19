package NG.GameMap;

import NG.Engine.Game;
import NG.Engine.GameAspect;
import NG.Entities.Entity;
import NG.Storable;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geert van Ieperen created on 17-2-2019.
 */
public class ClaimRegistry implements GameAspect, Storable {
    private final HashMap<Vector2ic, Entity> claimRegistry;
    private final Lock claimLock;
    private Game game;

    public ClaimRegistry() {
        this.claimRegistry = new HashMap<>();
        this.claimLock = new ReentrantLock(false);
    }

    @Override
    public void init(Game game) throws Exception {
        this.game = game;
    }

    /**
     * try to assign the given entity to the given coordinate. If there is already an entity on the coordinate, no claim
     * is made and false is returned
     * @param coordinate a coordinate on the map
     * @param entity     the entity that wants to enter the coordinate
     * @return true if the claim was made, false if the claim could not be made.
     * @throws NullPointerException when entity or coordinate is null
     * @see #dropClaim(Vector2ic, Entity)
     */
    public boolean createClaim(Vector2ic coordinate, Entity entity) {
        // check bounds

        int xc = coordinate.x();
        int yc = coordinate.y();
        Vector2ic size = game.map().getSize();
        if (xc >= size.x() || xc < 0 || yc >= size.y() || yc < 0) {
            return false;
        }

        // perform claim
        claimLock.lock();
        try {
            boolean isClaimed = claimRegistry.containsKey(coordinate);

            if (!isClaimed) {
                Vector2i copy = new Vector2i(coordinate);
                claimRegistry.put(copy, entity);
                return true;
            }
            return false;

        } finally {
            claimLock.unlock();
        }
    }

    /**
     * queries what entity owns the claim on the given coordinate.
     * @param coordinate a coordinate on the map
     * @return the entity that claimed the coordinate, and didn't drop it's claim yet
     */
    public Entity getClaim(Vector2ic coordinate) {
        return claimRegistry.get(coordinate); // no lock needed
    }

    /**
     * release a previously made claim
     * @param coordinate a coordinate on the map
     * @param entity     the entity that has claimed the given coordinate, or null to drop it regardless
     * @return true if the entity previously owned the claim, or if entity is null. When false is returned, the claim
     * register is not updated
     */
    public boolean dropClaim(Vector2ic coordinate, Entity entity) {
        claimLock.lock();
        try {
            Entity claimant = claimRegistry.get(coordinate);

            if (entity == null || entity == claimant) {
                claimRegistry.remove(coordinate);
                return true;
            }
            return false;

        } finally {
            claimLock.unlock();
        }
    }

    @Override
    public void cleanup() {
        claimLock.lock();
        claimRegistry.clear();
        claimLock.unlock();
    }

    @Override
    public void writeToFile(DataOutput out) throws IOException {
        claimLock.lock();
        try {
            out.writeInt(claimRegistry.size());

            for (Vector2ic vec : claimRegistry.keySet()) {

                Entity entity = claimRegistry.get(vec);
                if (entity instanceof Storable) {

                    out.writeInt(vec.x());
                    out.writeInt(vec.y());

                    Storable box = (Storable) entity;
                    Storable.writeToFile(out, box);
                }
            }

        } finally {
            claimLock.unlock();
        }
    }

    public ClaimRegistry(DataInput in) throws IOException, ClassNotFoundException {
        this();

        int x = in.readInt();
        int y = in.readInt();
        Entity entity = Storable.readFromFile(in, Entity.class);
        Vector2i pos = new Vector2i(x, y);
        claimRegistry.put(pos, entity);
    }
}
