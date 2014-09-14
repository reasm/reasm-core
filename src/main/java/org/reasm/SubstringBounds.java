package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The bounds of a substring.
 *
 * @author Francis Gagné
 */
@Immutable
public final class SubstringBounds {

    private final int start, end;

    /**
     * Initializes a new instance of the SubstringBounds class.
     *
     * @param start
     *            the starting position of the substring
     * @param end
     *            the ending position of the substring
     */
    public SubstringBounds(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean equals(@CheckForNull Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        SubstringBounds other = (SubstringBounds) obj;
        if (this.start != other.start) {
            return false;
        }

        if (this.end != other.end) {
            return false;
        }

        return true;
    }

    /**
     * Gets the ending position of the substring.
     *
     * @return the ending position of the substring
     */
    public final int getEnd() {
        return this.end;
    }

    /**
     * Gets the starting position of the substring.
     *
     * @return the starting position of the substring
     */
    public final int getStart() {
        return this.start;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.start;
        result = prime * result + this.end;
        return result;
    }

    @Nonnull
    @Override
    public String toString() {
        return "SubstringBounds [start=" + this.start + ", end=" + this.end + "]";
    }

}
