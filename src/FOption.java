import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class FOption <T> {
    private final T value;
    private final boolean defined;

    private static final FOption<?> EMPTY = new FOption<>(null, false);

    /**
     * Private Constructor
     * @param value value of FOption
     * @param defined whether value of FOption is defined or not
     */
    private FOption(T value, boolean defined) {
        this.value = value;
        this.defined = defined;
    }

    /**
     * Return object of FOption with value
     * @param value value of FOption
     * @param <T> the class of the value
     * @return object of FOption
     */
    public static <T> FOption<T> of(T value) {
        if (value == null)
            return empty();

        return new FOption<>(value, true);
    }

    /**
     * Return empty object of FOption
     * @param <T> the class of value
     * @return EMPTY FOption object
     */
    @SuppressWarnings("unchecked")
    public static <T> FOption<T> empty() {
        return (FOption<T>) EMPTY;
    }

    /**
     * Return object of FOption using Optional object parameter
     * @param optional object of Optional
     * @param <T> the class of optional's value
     * @return object of FOption
     */
    public static <T> FOption<T> from(Optional<T> optional) {
        return of(optional.orElse(null));
    }

    /**
     * Whether value exists or not
     * @return Whether value exists or not
     */
    public boolean isPresent() {
        return defined;
    }

    /**
     * Whether value is defined or not
     * @return Whether value is defined or not
     */
    public boolean isAbsent() {
        return !defined;
    }

    /**
     * Get value if value exists
     * @return value of FOption
     */
    public T get() {
        if (!defined) {
            throw new NoSuchElementException("No element exists");
        }
        return value;
    }

    /**
     * Get value but no defined check
     * @return value of FOption
     */
    public T getOrNull() {
        return value;
    }

    /**
     * Get value but if value is defined then return elseValue
     * @return value of FOption or elseValue
     */
    public T getOrElse(T elseValue) {
        return defined ? value : elseValue;
    }

    /**
     * Get value but if value is defined then return get of elseSupplier
     * @return value of FOption or get of elseSupplier
     */
    public T getOrElse(Supplier<T> elseSupplier) {
        return defined ? value : elseSupplier.get();
    }

    /**
     * Get value but if value is defined then return get of elseSupplier
     * @return value of FOption or get of elseSupplier
     */
    public <X extends Throwable> T getOrElseThrow(Supplier<X> thrower) throws X {
        if (!defined) {
            throw thrower.get();
        }
        return value;
    }

    /**
     * If value is present, Consumer object accept value
     * @param effect object of work with value
     * @return this
     */
    public FOption<T> ifPresent(Consumer<T> effect) {
        if (defined)
            effect.accept(value);

        return this;
    }

    /**
     * If value is absent, Runnable object run
     * @param orElse object of work
     * @return this
     */
    public FOption<T> ifAbsent(Runnable orElse) {
        orElse.run();

        return this;
    }

    /**
     * Filter according to the input Predicate
     * @param pred Predicate for filter
     * @return If it is satisfied with the filter return this, else return empty
     */
    public FOption<T> filter(Predicate<T> pred) {
        if (!defined) {
            return this;
        }

        return pred.test(value) ? this : empty();
    }

    /**
     * Test value is defined and result of Predicate
     * @param pred Predicate for test
     * @return If value of FOption is defined and it is satisfied with the filter
     */
    public boolean test(Predicate<T> pred) {
        return defined && pred.test(value);
    }

    /**
     * Map value of FOption to mapper
     * @param mapper function for mapping
     * @param <S> function argument for mapping
     * @return Mapped result or empty
     */
    public <S> FOption<S> map(Function<T, S> mapper) {
        return defined ? FOption.of(mapper.apply(value)) : FOption.empty();
    }

    @Override
    public String toString() {
        return defined ? String.format("FOption(%s)", value) : "FOption.empty";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FOption) {
            FOption fOption = (FOption) obj;
            return Objects.equals(value, fOption.value);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return defined ? value.hashCode(): 0;
    }
}
