package NG.MonsterSoul;

import NG.Storable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public interface Type extends Storable {
    /**
     * a stimulus type based on class equality
     */
    class ClassEquality implements Type {
        private final Class<?> id;

        public ClassEquality(Object source) {
            if (source instanceof Class<?>) {
                id = (Class<?>) source;

            } else {
                this.id = source.getClass();
            }
        }

        @Override
        public void writeToDataStream(DataOutput out) throws IOException {
            Storable.writeClass(out, id);
        }

        private ClassEquality(DataInput in) throws IOException, ClassNotFoundException {
            id = Storable.readClass(in, Object.class);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ClassEquality) {
                ClassEquality other = (ClassEquality) obj;
                return id.equals(other.id);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}

