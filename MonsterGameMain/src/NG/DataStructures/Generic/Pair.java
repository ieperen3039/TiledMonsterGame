package NG.DataStructures.Generic;

import java.util.Objects;

/**
 * Pair class that simply holds two variables.
 * @param <L> Left type
 * @param <R> Right type
 */
public class Pair<L, R> {
    public final L left;
    public final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        Pair<?, ?> other = (Pair<?, ?>) o;
        return Objects.equals(left, other.left) && Objects.equals(right, other.right);
    }

    @Override
    public int hashCode() {
        int result = (left != null) ? left.hashCode() : 0;
        result = (31 * result) + ((right != null) ? right.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "[" + left + ", " + right + "]";
    }
}
