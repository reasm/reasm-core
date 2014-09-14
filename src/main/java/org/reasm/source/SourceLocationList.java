package org.reasm.source;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Vector;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Architecture;

import ca.fragag.collections.UnmodifiableList;

/**
 * A list of {@link SourceLocation} objects.
 *
 * @author Francis Gagné
 */
@Immutable
final class SourceLocationList extends UnmodifiableList<SourceLocation> implements RandomAccess {

    private final class Itr implements ListIterator<SourceLocation> {

        private final int fromIndex;
        private final int toIndex;
        private int index;

        Itr(int fromIndex, int toIndex, int index) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            this.index = index;
        }

        @Override
        public final void add(SourceLocation e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final boolean hasNext() {
            return this.index < this.toIndex;
        }

        @Override
        public final boolean hasPrevious() {
            return this.index > this.fromIndex;
        }

        @Nonnull
        @Override
        public final SourceLocation next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException("No item at index " + this.nextIndex());
            }

            return SourceLocationList.this.get(this.index++);
        }

        @Override
        public final int nextIndex() {
            return this.index - this.fromIndex;
        }

        @Override
        public final SourceLocation previous() {
            if (!this.hasPrevious()) {
                throw new NoSuchElementException("No item at index " + this.previousIndex());
            }

            return SourceLocationList.this.get(--this.index);
        }

        @Override
        public final int previousIndex() {
            return this.index - this.fromIndex - 1;
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public final void set(SourceLocation e) {
            throw new UnsupportedOperationException();
        }

    }

    @Immutable
    private final class SubList extends UnmodifiableList<SourceLocation> implements RandomAccess {

        private final int fromIndex;
        private final int toIndex;

        SubList(int fromIndex, int toIndex) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        @Override
        public final boolean contains(@CheckForNull Object object) {
            return this.indexOf(object) >= 0;
        }

        @Override
        public final boolean containsAll(@Nonnull Collection<?> collection) {
            for (Object object : collection) {
                if (!this.contains(object)) {
                    return false;
                }
            }

            return true;
        }

        @Nonnull
        @Override
        public final SourceLocation get(int index) {
            this.checkInterval(index, 0, this.size() - 1);
            return SourceLocationList.this.get(index + this.fromIndex);
        }

        @Override
        public final int indexOf(@CheckForNull Object object) {
            return SourceLocationList.this.indexOf(object, this.fromIndex, this.toIndex);
        }

        @Override
        public final boolean isEmpty() {
            return this.size() == 0;
        }

        @Nonnull
        @Override
        public final Iterator<SourceLocation> iterator() {
            return this.listIterator(0);
        }

        @Override
        public final int lastIndexOf(@CheckForNull Object object) {
            return SourceLocationList.this.lastIndexOf(object, this.fromIndex, this.toIndex);
        }

        @Nonnull
        @Override
        public final ListIterator<SourceLocation> listIterator() {
            return new Itr(this.fromIndex, this.toIndex, this.fromIndex);
        }

        @Nonnull
        @Override
        public final ListIterator<SourceLocation> listIterator(int index) {
            this.checkInterval(index, 0, this.size());
            return new Itr(this.fromIndex, this.toIndex, this.fromIndex + index);
        }

        @Override
        public final int size() {
            return this.toIndex - this.fromIndex;
        }

        @Nonnull
        @Override
        public final List<SourceLocation> subList(int fromIndex, int toIndex) {
            final int size = this.size();
            if (fromIndex == 0 && toIndex == size) {
                return this;
            }

            this.checkInterval(fromIndex, 0, size);
            this.checkInterval(toIndex, fromIndex, size);
            return new SubList(this.fromIndex + fromIndex, this.fromIndex + toIndex);
        }

        @Nonnull
        @Override
        public final Object[] toArray() {
            return SourceLocationList.this.toArray(this.fromIndex, this.toIndex);
        }

        @Nonnull
        @Override
        public final <T> T[] toArray(@Nonnull T[] array) {
            return SourceLocationList.this.toArray(array, this.fromIndex, this.toIndex);
        }

        private final void checkInterval(int index, int startIndex, int endIndex) {
            SourceLocationList.checkInterval(index, startIndex, endIndex, this.size());
        }

    }

    static void checkInterval(int index, int startIndex, int endIndex, int size) {
        if (index < startIndex || index > endIndex) {
            throw new IndexOutOfBoundsException("Invalid index: " + index + ", size=" + size);
        }
    }

    @Nonnull
    private final Vector<SourceLocation> lazyList = new Vector<>();
    @Nonnull
    private final LazySourceLocationGenerator lazyGenerator;
    @Nonnull
    private final List<SourceNode> sourceNodes;

    /**
     * Initializes a new SourceLocationList.
     *
     * @param file
     *            the source file that contains the source nodes
     * @param architecture
     *            the architecture under which the source file was parsed and is being assembled
     * @param sourceNodes
     *            a {@link List} of source nodes that will be used to generate the {@link SourceLocation}s
     * @param initialTextPosition
     *            the position of the start of the text of the first {@link SourceLocation} that will be generated
     */
    SourceLocationList(@Nonnull AbstractSourceFile<?> file, @Nonnull Architecture architecture,
            @Nonnull List<SourceNode> sourceNodes, int initialTextPosition) {
        this.lazyGenerator = new LazySourceLocationGenerator(file, architecture, sourceNodes, initialTextPosition);
        this.sourceNodes = sourceNodes;
    }

    @Override
    public final boolean contains(@CheckForNull Object object) {
        return this.indexOf(object) >= 0;
    }

    @Override
    public final boolean containsAll(@Nonnull Collection<?> collection) {
        for (Object object : collection) {
            if (!this.contains(object)) {
                return false;
            }
        }

        return true;
    }

    @Nonnull
    @Override
    public final SourceLocation get(int index) {
        this.checkInterval(index, 0, this.size() - 1);

        synchronized (this.lazyList) {
            while (index >= this.lazyList.size()) {
                this.lazyList.add(this.lazyGenerator.next());
            }

            return this.lazyList.get(index);
        }
    }

    @Override
    public final int indexOf(@CheckForNull Object object) {
        return this.indexOf(object, 0, this.size());
    }

    @Override
    public final boolean isEmpty() {
        return this.sourceNodes.isEmpty();
    }

    @Nonnull
    @Override
    public final Iterator<SourceLocation> iterator() {
        return this.listIterator();
    }

    @Override
    public final int lastIndexOf(@CheckForNull Object object) {
        return this.lastIndexOf(object, 0, this.size());
    }

    @Nonnull
    @Override
    public final ListIterator<SourceLocation> listIterator() {
        return new Itr(0, this.size(), 0);
    }

    @Nonnull
    @Override
    public final ListIterator<SourceLocation> listIterator(int index) {
        this.checkInterval(index, 0, this.size());
        return new Itr(0, this.size(), index);
    }

    @Override
    public final int size() {
        return this.sourceNodes.size();
    }

    @Nonnull
    @Override
    public final List<SourceLocation> subList(int fromIndex, int toIndex) {
        final int size = this.size();
        if (fromIndex == 0 && toIndex == size) {
            return this;
        }

        this.checkInterval(fromIndex, 0, size);
        this.checkInterval(toIndex, fromIndex, size);
        return new SubList(fromIndex, toIndex);
    }

    @Nonnull
    @Override
    public final Object[] toArray() {
        return this.toArray(0, this.size());
    }

    @Nonnull
    @Override
    public final <T> T[] toArray(@Nonnull T[] array) {
        return this.toArray(array, 0, this.size());
    }

    final int indexOf(@CheckForNull Object object, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (this.get(i).equals(object)) {
                return i - fromIndex;
            }
        }

        return -1;
    }

    final int lastIndexOf(@CheckForNull Object object, int fromIndex, int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--) {
            if (this.get(i).equals(object)) {
                return i - fromIndex;
            }
        }

        return -1;
    }

    @Nonnull
    final Object[] toArray(int fromIndex, int toIndex) {
        final int size = toIndex - fromIndex;
        final Object[] result = new Object[size];
        for (int i = 0; i < size; i++) {
            result[i] = this.get(fromIndex + i);
        }

        return result;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    final <T> T[] toArray(@Nonnull T[] array, int fromIndex, int toIndex) {
        if (array == null) {
            throw new NullPointerException("array");
        }

        final int size = toIndex - fromIndex;
        if (array.length < size) {
            array = (T[]) Array.newInstance(array.getClass().getComponentType(), size);
        }

        for (int i = 0; i < size; i++) {
            array[i] = (T) this.get(fromIndex + i);
        }

        if (array.length > size) {
            array[size] = null;
        }

        return array;
    }

    private void checkInterval(int index, int startIndex, int endIndex) {
        checkInterval(index, startIndex, endIndex, this.size());
    }

}
