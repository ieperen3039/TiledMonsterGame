package NG.Core;

import java.io.Serializable;

/**
 * @author Geert van Ieperen created on 25-2-2020.
 */
public interface GameObject extends Serializable {
    void restore(Game game);
}
