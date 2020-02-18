package NG.Actions;

import NG.Tools.Vectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * @author Geert van Ieperen created on 30-4-2019.
 */
public class BrokenMovementException extends RuntimeException {
    public BrokenMovementException(String message) {
        super(message);
    }

    public BrokenMovementException(String message, Throwable cause) {
        super(message, cause);
    }

    public BrokenMovementException(EntityAction first, EntityAction second, float relativeTime) {
        this(first, second, first.getPositionAt(relativeTime), second.getStartPosition());
    }

    private BrokenMovementException(
            EntityAction first, EntityAction second, Vector3f firstEndPos, Vector3fc secondPos
    ) {
        super(String.format(
                "Action %s does not follow %s (%s != %s, delta is %8.05f)",
                second, first,
                Vectors.toString(firstEndPos),
                Vectors.toString(secondPos),
                firstEndPos.distance(secondPos)
        ));
    }
}
