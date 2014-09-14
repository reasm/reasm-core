package org.reasm.source;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import ca.fragag.collections.AbstractImmutableTreeList;
import ca.fragag.collections.AbstractImmutableTreeListFactory;
import ca.fragag.collections.AbstractImmutableTreeNode;
import ca.fragag.collections.AbstractImmutableTreeNodeFactory;

/**
 * A list of line lengths.
 *
 * @author Francis Gagné
 */
@Immutable
class LineLengthList extends AbstractImmutableTreeList<Integer, LineLengthList.Node> {

    /**
     * The list factory class for {@link LineLengthList}.
     *
     * @author Francis Gagné
     */
    @Immutable
    static class Factory extends AbstractImmutableTreeListFactory<Integer, Node, LineLengthList> {

        public static final Factory INSTANCE = new Factory(Node.Factory.INSTANCE);

        /**
         * Initializes a new Factory.
         *
         * @param nodeFactory
         *            the node factory to use
         */
        protected Factory(@Nonnull Node.Factory nodeFactory) {
            super(nodeFactory);
        }

        @Override
        protected LineLengthList createList(@CheckForNull Node root) {
            return new LineLengthList(root);
        }

    }

    /**
     * An implementation of {@link AbstractImmutableTreeNode} that stores the sum of the value of itself and of its subtrees.
     *
     * @author Francis Gagné
     */
    @Immutable
    static class Node extends AbstractImmutableTreeNode<Integer, Node> {

        /**
         * The node factory class for {@link Node}.
         *
         * @author Francis Gagné
         */
        @Immutable
        static class Factory extends AbstractImmutableTreeNodeFactory<Integer, Node> {

            public static final Factory INSTANCE = new Factory();

            private Factory() {
            }

            @Override
            protected Node createNode(@CheckForNull Node left, @Nonnull Integer value, @CheckForNull Node right) {
                return new Node(left, value, right);
            }

        }

        /**
         * Gets the line index of the line starting at or before the specified position. The result is also the number of line
         * breaks that appear before that position.
         *
         * @param node
         *            the root node of a LineLengthList
         * @param textPosition
         *            the text position
         * @return the index
         */
        static int lineIndexOfTextPosition(@Nonnull Node node, int textPosition) {
            int index = 0;
            for (;;) {
                final Node left = node.getLeft();
                if (left != null) {
                    if (textPosition < left.totalLength) {
                        node = left;
                        continue;
                    }

                    textPosition -= left.totalLength;
                    index += left.getSize();
                }

                final int value = node.getIntValue();
                if (textPosition < value) {
                    return index;
                }

                node = node.getRight();
                assert node != null;
                textPosition -= value;
                index++;
            }
        }

        /**
         * Writes the location of the specified position in textLocation.
         *
         * @param textPosition
         *            the text position
         * @param textLocation
         *            the text location
         */
        static void textLocationOfTextPosition(@Nonnull Node node, int textPosition, @Nonnull TextLocation textLocation) {
            for (;;) {
                final Node left = node.getLeft();
                if (left != null) {
                    if (textPosition < left.totalLength) {
                        node = left;
                        continue;
                    }

                    textPosition -= left.totalLength;
                    textLocation.lineIndex += left.getSize();
                }

                int value = node.getIntValue();
                if (textPosition < value) {
                    textLocation.linePosition = textPosition;
                    return;
                }

                node = node.getRight();
                assert node != null;
                textPosition -= value;
                textLocation.lineIndex += 1;
            }
        }

        /**
         * Gets the text position of the start of the line at the specified line index.
         *
         * @param node
         *            the root node of a LineLengthList
         * @param lineIndex
         *            the line index
         * @return the text position
         */
        static int textPositionOfLineIndex(@Nonnull Node node, int lineIndex) {
            int textPosition = 0;
            for (;;) {
                final Node left = node.getLeft();
                if (left != null) {
                    if (lineIndex < left.getSize()) {
                        node = left;
                        continue;
                    }

                    lineIndex -= left.getSize();
                    textPosition += left.totalLength;
                }

                if (lineIndex == 0) {
                    return textPosition;
                }

                textPosition += node.getIntValue();
                node = node.getRight();
                assert node != null;
                lineIndex--;
            }
        }

        private final transient int totalLength;

        /**
         * Initializes a new Node.
         *
         * @param left
         *            the left subtree
         * @param value
         *            the element value (the line length)
         * @param right
         *            the right subtree
         */
        protected Node(@CheckForNull Node left, @Nonnull Integer value, @CheckForNull Node right) {
            super(left, value, right);
            this.totalLength = (left == null ? 0 : left.totalLength) + value + (right == null ? 0 : right.totalLength);
        }

        /**
         * Gets the total length of this node and its subtrees.
         *
         * @return the total length
         */
        public final int getTotalLength() {
            return this.totalLength;
        }

        @Nonnull
        private final int getIntValue() {
            final Integer value = super.getValue();
            assert value != null;
            return value;
        }

    }

    /**
     * The combination of a line index and a line position. Both values are zero-based.
     *
     * @author Francis Gagné
     */
    static class TextLocation {

        int lineIndex;
        int linePosition;

    }

    /**
     * Initializes a new LineLengthList.
     *
     * @param root
     *            the list's root node
     */
    protected LineLengthList(@CheckForNull Node root) {
        super(root);
    }

    /**
     * Gets the line index of the line starting at or before the specified position. The result is also the number of line breaks
     * that appear before that position.
     *
     * @param textPosition
     *            the text position
     * @return the index
     */
    public final int lineIndexOfTextPosition(int textPosition) {
        final Node root = this.getRoot();
        if (root == null) {
            if (textPosition != 0) {
                throw new IndexOutOfBoundsException("textPosition");
            }

            return 0;
        }

        if (textPosition < 0 || textPosition > root.getTotalLength()) {
            throw new IndexOutOfBoundsException("textPosition");
        }

        if (textPosition == root.getTotalLength()) {
            return this.size() - 1;
        }

        return Node.lineIndexOfTextPosition(root, textPosition);
    }

    /**
     * Writes the location of the specified position in textLocation.
     *
     * @param textPosition
     *            the text position
     * @param textLocation
     *            the text location
     */
    public final void textLocationOfTextPosition(int textPosition, @Nonnull TextLocation textLocation) {
        final Node root = this.getRoot();
        if (root == null) {
            if (textPosition != 0) {
                throw new IndexOutOfBoundsException("textPosition");
            }

            textLocation.lineIndex = 0;
            textLocation.linePosition = 0;
            return;
        }

        if (textPosition < 0 || textPosition > root.getTotalLength()) {
            throw new IndexOutOfBoundsException("textPosition");
        }

        textLocation.lineIndex = 0;
        textLocation.linePosition = 0;
        if (textPosition == root.getTotalLength()) {
            textLocation.lineIndex = this.size() - 1;
            textLocation.linePosition = this.get(textLocation.lineIndex);
        } else {
            Node.textLocationOfTextPosition(root, textPosition, textLocation);
        }

    }

    /**
     * Gets the text position of the start of the line at the specified line index.
     *
     * @param lineIndex
     *            the line index
     * @return the text position
     */
    public final int textPositionOfLineIndex(int lineIndex) {
        final Node root = this.getRoot();
        if (root == null) {
            if (lineIndex != 0) {
                throw new IndexOutOfBoundsException("lineIndex");
            }

            return 0;
        }

        if (lineIndex < 0 || lineIndex >= this.size()) {
            throw new IndexOutOfBoundsException("lineIndex");
        }

        return Node.textPositionOfLineIndex(root, lineIndex);
    }

}
