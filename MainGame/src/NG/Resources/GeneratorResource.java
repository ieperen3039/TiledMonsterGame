package NG.Resources;

/**
 * @author Geert van Ieperen created on 26-2-2020.
 */
public class GeneratorResource<T> extends Resource<T> {
    private final ResourceGenerator<T> function;
    private final ResourceCleaner<T> cleanup;

    public GeneratorResource(ResourceGenerator<T> function) {
        this.function = function;
        this.cleanup = t -> {};
    }

    public GeneratorResource(ResourceGenerator<T> function, ResourceCleaner<T> cleanup) {
        this.function = function;
        this.cleanup = cleanup;
    }

    @Override
    protected T reload() throws ResourceException {
        return function.get();
    }

    @Override
    public void drop() {
        if (element != null) {
            cleanup.accept(element);
        }

        super.drop();
    }

}
