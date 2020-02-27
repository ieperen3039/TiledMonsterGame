package NG.Resources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 25-2-2020.
 */
public abstract class Resource<T> implements Serializable {
    private static final List<Resource<?>> allResources = new ArrayList<>();
    private static final List<Resource<?>> allResView = Collections.unmodifiableList(allResources);

    /** heat will not rise above this number */
    private static final int MAX_HEAT = 300;

    /** the cached element */
    protected transient T element = null;
    /** indicator how often it is used */
    private transient int heat = 0;
    private transient boolean isUsed = false;

    public Resource() {
        allResources.add(this);
    }

    /**
     * returns the cached element, possibly generating a new element
     * @return the element itself
     * @throws ResourceException if the reloading operation fails
     */
    public T get() throws ResourceException {
        if (element == null) {
            element = reload();
            heat = MAX_HEAT;
        }
        isUsed = true;

        return element;
    }

    /**
     * marks some time period. If this resource has not been used after at most {@link #MAX_HEAT} calls to this method,
     * it is dropped as if calling {@link #drop()}.
     */
    public void cool() {
        if (element != null) {
            if (isUsed) {
                heat = Math.min(MAX_HEAT, heat + 1);
            } else {
                heat--;
                if (heat == 0) drop();
            }
        }
    }

    /**
     * drops the cached element, causing a reload on the next get
     */
    public void drop() {
        element = null;
    }

    /**
     * reloads the resource.
     * @throws ResourceException whenever the resource could not be generated
     */
    protected abstract T reload() throws ResourceException;

    public static List<Resource<?>> getAll() {
        return allResView;
    }

    /**
     * create a resource that is generated from another resource.
     * @param source    a resource generating an element of type A
     * @param extractor a function that generates the desired element of type B using source
     * @return a resource generating an element of type B
     */
    public static <A, B> Resource<B> derive(Resource<A> source, ResourceConverter<A, B> extractor) {
        return new GeneratorResource<>(() -> extractor.apply(source.get()));
    }

    public static <A, B> Resource<B> derive(
            Resource<A> source, ResourceConverter<A, B> extractor, ResourceCleaner<B> cleanup
    ) {
        return new GeneratorResource<>(() -> extractor.apply(source.get()), cleanup);
    }

    /** serializable version of {@link Supplier} */
    public interface ResourceGenerator<T> extends Supplier<T>, Serializable {}

    /** serializable version of {@link Consumer} */
    public interface ResourceCleaner<T> extends Consumer<T>, Serializable {}

    /** serializable version of {@link Function} */
    public interface ResourceConverter<A, B> extends Function<A, B>, Serializable {}

    /**
     * error to indicate a failure to fetch a resource. Usually this indicates that this resource has been sent over,
     * and the receiving end does not have this resource on the same place.
     */
    public static class ResourceException extends RuntimeException {
        public ResourceException(Exception cause, String message) {
            super(message, cause);
        }
    }
}
