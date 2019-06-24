package NG.Actions;

import NG.Tools.Vectors;

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
        super(String.format(
                "Action %s does not follow %s (%s != %s)",
                second, first,
                Vectors.toString(second.getStartPosition()),
                Vectors.toString(first.getPositionAt(relativeTime))
        ));
    }
}
