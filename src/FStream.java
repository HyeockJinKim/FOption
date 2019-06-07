import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * FStream similar to Stream API in JAVA 8
 *
 * @author HyeockJin Kim
 * @param <T> Generic Type
 */
public interface FStream<T> extends AutoCloseable {
    public FOption<T> next();

    /**
     * Empty FStream with empty FOption
     * @param <T> Type of FStream
     * @return Object of FStream
     */
    public static <T> FStream<T> empty() {
        return new FStream<T>() {
            @Override
            public FOption<T> next() {
                return FOption.empty();
            }

            @Override
            public void close() throws Exception {}
        };
    }

    /**
     * Generate FStream using array of T
     * @param values Array of T type
     * @param <T> Type of FStream
     * @return FStream with T value
     */
    @SafeVarargs
    public static <T> FStream<T> of(T... values) {
        return new FStream<T>() {
            T[] arr = values;
            int index = 0;

            @Override
            public FOption<T> next() {
                if (index >= values.length)
                    return FOption.empty();

                return FOption.of(arr[index++]);
            }

            @Override
            public void close() throws Exception {
                if (index >= values.length)
                    throw new Exception("Stream has already been closed");

                index = values.length;
            }
        };
    }

    /**
     * Generate FStream from T Iterable
     * @param values Iterable of T type
     * @param <T> Type of FStream
     * @return FStream with T value
     */
    public static <T> FStream<T> from(Iterable<? extends T> values) {
        return new FStream<T>() {
            Iterator<? extends T> iterator = values.iterator();
            @Override
            public FOption<T> next() {
                if (!iterator.hasNext())
                    return FOption.empty();

                return FOption.of(iterator.next());
            }

            @Override
            public void close() throws Exception {
                if (!iterator.hasNext())
                    throw new Exception("Stream has already been closed");

                iterator = Collections.emptyIterator();
            }
        };
    }

    /**
     * Generate FStream from T Iterator
     * @param iter Iterator of T type
     * @param <T> Type of FStream
     * @return FStream with T value
     */
    public static <T> FStream<T> from(Iterator<? extends T> iter) {
        return new FStream<T>() {
            Iterator<? extends T> iterator = iter;
            @Override
            public FOption<T> next() {
                if (!iterator.hasNext())
                    return FOption.empty();

                return FOption.of(iterator.next());
            }

            @Override
            public void close() throws Exception {
                if (!iterator.hasNext())
                    throw new Exception("Stream has already been closed");

                iterator = Collections.emptyIterator();
            }
        };
    }

    /**
     * Generate FStream from T Stream
     * @param stream Stream of T type
     * @param <T> Type of FStream
     * @return FStream with T value
     */
    public static <T> FStream<T> from(Stream<? extends T> stream) {
        return new FStream<T>() {
            Iterator<? extends T> iterator = stream.iterator();
            @Override
            public FOption<T> next() {
                if (!iterator.hasNext())
                    return FOption.empty();

                return FOption.of(iterator.next());
            }

            @Override
            public void close() throws Exception {
                if (!iterator.hasNext())
                    throw new Exception("Stream has already been closed");

                iterator = Collections.emptyIterator();
            }
        };
    }

    /**
     * Limit number of FStream by count
     * @param count count for limit
     * @return FStream limited by count
     */
    public default FStream<T> take(long count) {
        return new FStream<T>() {
            int index = 0;
            @Override
            public FOption<T> next() {
                if (index < count) {
                    ++index;
                    return FStream.this.next();
                } else {
                    // Consume full stream
                    FOption<T> next = FStream.this.next();
                    while (next.isPresent()) {
                        next = FStream.this.next();
                    }
                }
                return FOption.empty();
            }

            @Override
            public void close() throws Exception {}
        };
    }

    /**
     * Skip FStream by count
     * @param count count for skip
     * @return FStream skipped by count
     */
    public default FStream<T> drop(long count) {
        return new FStream<T>() {
            int index = 0;
            @Override
            public FOption<T> next() {
                for (; index < count; ++index) {
                    FStream.this.next();
                }
                return FStream.this.next();
            }

            @Override
            public void close() throws Exception {}
        };
    }

    /**
     * Execute effect using all value of FStream
     * @param effect Function to execute
     */
    public default void forEach(Consumer<? super T> effect) {
        FOption<T> next = FStream.this.next();
        while (next.isPresent()) {
            effect.accept(next.get());
            next = FStream.this.next();
        }
    }

    /**
     * Filter FStream with Predicate
     * @param pred function for filtering FStream
     * @return FStream filtered by pred
     */
    public default FStream<T> filter(Predicate<? super T> pred) {
        return new FStream<T>() {
            @Override
            public FOption<T> next() {
                FOption<T> next;
                do {
                    next = FStream.this.next();

                    if (next.isAbsent())
                        return FOption.empty();
                } while (!pred.test(next.get()));

                return next;
            }

            @Override
            public void close() throws Exception {}
        };
    }

    /**
     * Map value of FStream using mapper
     * @param mapper Function to map value of FStream
     * @param <S> Type of Mapped value
     * @return FStream with Mapped value
     */
    public default <S> FStream<S> map(Function<? super T, ? extends S> mapper) {
        return new FStream<S>() {
            @Override
            public FOption<S> next() {
                FOption<T> next = FStream.this.next();

                if (next.isAbsent())
                    return FOption.empty();

                return FOption.of(mapper.apply(next.get()));
            }

            @Override
            public void close() throws Exception {}
        };
    }

    /**
     * Map value of FStream using mapper unwrapping FOption
     * @param mapper Function to map value of FStream unwrapping FOption
     * @param <V> Type of Mapped value
     * @return FStream with Mapped value
     */
    public default <V> FStream<V> flatMap(Function<? super T, ? extends FStream<V>> mapper) {
        return new FStream<V>() {

            @Override
            @SuppressWarnings("unchecked")
            public FOption<V> next() {
                FOption<T> next = FStream.this.next();
                if (next.isAbsent())
                    return FOption.empty();

                while (next.get() instanceof FOption) {
                    next = (FOption<T>) next.get();
                }

                return (FOption<V>) FOption.of(mapper.apply(next.get()));
            }

            @Override
            public void close() throws Exception {}
        };
    }

    /**
     * Get Iterator of FStream's value
     * @return Iterator with value of FStream
     */
    public default Iterator<T> iterator() {
        return toList().iterator();
    }

    /**
     * Get ArrayList of FStream's value
     * @return ArrayList with value of FStream
     */
    public default ArrayList<T> toList() {
        ArrayList<T> list = new ArrayList<>();
        FOption<T> next = FStream.this.next();

        while (next.isPresent()) {
            list.add(next.get());
            next = FStream.this.next();
        }

        return list;
    }

    /**
     * Get HashSet of FStream's value
     * @return HashSet with value of FStream
     */
    public default HashSet<T> toSet() {
        HashSet<T> set = new HashSet<>();
        FOption<T> next = FStream.this.next();

        while (next.isPresent()) {
            set.add(next.get());
            next = FStream.this.next();
        }

        return set;
    }

    /**
     * Get Array of FStream's value
     * @param componentType Object for casting type
     * @param <S> Type of Array
     * @return Array with value of FStream
     */
    @SuppressWarnings("unchecked")
    public default <S> S[] toArray(Class<S> componentType) {
        ArrayList<S> list = new ArrayList<>();
        FOption<T> next = FStream.this.next();

        while (next.isPresent()) {
            list.add(componentType.cast(next.get()));
            next = FStream.this.next();
        }

        return (S[]) list.toArray();
    }

    /**
     * Get Stream of FStream's value
     * @return Stream with value of FStream
     */
    public default Stream<T> stream() {
        return toList().stream();
    }

    /**
     * Sort value of FStream using Comparator
     * @param cmp Comparator for sorting FStream
     * @return Sorted FStream
     */
    @SuppressWarnings("unchecked")
    public default FStream<T> sort(Comparator<? super T> cmp) {
        ArrayList<T> list = toList();
        list.sort(cmp);

        return of((T[]) list.toArray());
    }
}
