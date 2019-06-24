package NG.GameMap;

/**
 * @author Geert van Ieperen created on 1-5-2019.
 */
public enum TileProperties {
    /** this object allows light through */
    TRANSPARENT,
    /** this object does not allow entities through */
    OBSTRUCTING,
    /** this object can be destroyed *///TODO what if this happens?
    DESTRUCTIBLE,

    ;
}
