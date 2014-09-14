package org.reasm;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.source.SourceLocation;

/**
 * The location of an assembly step.
 * <p>
 * The same part of a source file can be assembled more than once in some situations. For example:
 * <ul>
 * <li>If a source file is included more than once, its contents will be assembled each time it is included.
 * <li>If the code is in a macro, it will be assembled for each invocation of the macro.
 * <li>If the code is in a loop, it will be assembled for each iteration of the loop.
 * </ul>
 * <p>
 * The source location specifies a reference to the source code. The source code text will be retrieved from this location.
 * <p>
 * An iteration number may indicate which iteration of a loop is being processed. A nonzero number represents a looping construct.
 * <p>
 * The parent of an assembly step location indicates how the source node was reached; a <code>null</code> parent indicates the top
 * level of the main source file.
 * <p>
 * This class is immutable and has value semantics.
 *
 * @author Francis Gagné
 * @see SourceLocation
 */
@Immutable
public final class AssemblyStepLocation {

    @Nonnull
    private final SourceLocation sourceLocation;
    private final long iterationNumber;
    @CheckForNull
    private final AssemblyStepLocation parent;
    private final boolean transparentParent;

    /**
     * Initializes a new assembly step location.
     *
     * @param sourceLocation
     *            the source location
     * @param iterationNumber
     *            the iteration number
     * @param parent
     *            the parent assembly step location
     * @param transparentParent
     *            <code>true</code> if the parent should not be represented in the assembly step location's full path, or
     *            <code>false</code> if it should
     */
    AssemblyStepLocation(@Nonnull SourceLocation sourceLocation, long iterationNumber, @CheckForNull AssemblyStepLocation parent,
            boolean transparentParent) {
        this.sourceLocation = sourceLocation;
        this.iterationNumber = iterationNumber;
        this.parent = parent;
        this.transparentParent = transparentParent;
    }

    @Override
    public final boolean equals(@CheckForNull Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final AssemblyStepLocation other = (AssemblyStepLocation) obj;

        if (!this.sourceLocation.equals(other.sourceLocation)) {
            return false;
        }

        if (this.iterationNumber != other.iterationNumber) {
            return false;
        }

        if (!Objects.equals(this.parent, other.parent)) {
            return false;
        }

        if (this.transparentParent != other.transparentParent) {
            return false;
        }

        return true;
    }

    /**
     * Gets the full path of this assembly step location as a string. The string is a list of source locations, joined by colons,
     * where each source location is formatted like this:
     * <p>
     * <em>file name</em>(<em>line number</em>,<em>line position</em>)[<em>iteration number</em>].
     * <p>
     * If the iteration number is 0, it and its surrounding brackets are omitted.
     * <p>
     * The first location is the deepest location (following the parent chain of this location) and the last location is this
     * location.
     *
     * @return the full path
     */
    @Nonnull
    public final String getFullPath() {
        StringBuilder sb = new StringBuilder();
        this.prependPath(sb);
        return sb.toString();
    }

    /**
     * Gets the iteration number of this assembly step location.
     *
     * @return the iteration number
     */
    public final long getIterationNumber() {
        return this.iterationNumber;
    }

    /**
     * Gets the parent location of this assembly step location.
     *
     * @return the parent
     */
    @CheckForNull
    public final AssemblyStepLocation getParent() {
        return this.parent;
    }

    /**
     * Gets the source location of this assembly step location.
     *
     * @return the source location
     */
    @Nonnull
    public final SourceLocation getSourceLocation() {
        return this.sourceLocation;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.sourceLocation.hashCode();
        result = prime * result + (int) (this.iterationNumber ^ this.iterationNumber >>> 32);
        result = prime * result + Objects.hashCode(this.parent);
        result = prime * result + (this.transparentParent ? 1231 : 1237);
        return result;
    }

    /**
     * Gets a value indicating whether the parent should not be represented in the assembly step location's full path.
     *
     * @return <code>true</code> if the parent should not be represented in the assembly step location's full path, or
     *         <code>false</code> if it should
     */
    public final boolean isParentTransparent() {
        return this.transparentParent;
    }

    private final void prependPath(@Nonnull StringBuilder sb) {
        AssemblyStepLocation loc = this;
        while (loc.transparentParent && loc.parent != null) {
            loc = loc.parent;
        }

        if (loc.parent != null) {
            loc.parent.prependPath(sb);
            sb.append(':');
        }

        sb.append(this.sourceLocation.getFile().getFileName());
        sb.append('(');
        sb.append(this.sourceLocation.getLineNumber());
        sb.append(',');
        sb.append(this.sourceLocation.getLinePosition());
        sb.append(')');

        if (loc.iterationNumber != 0) {
            sb.append('[');
            sb.append(loc.iterationNumber);
            sb.append(']');
        }
    }

}
