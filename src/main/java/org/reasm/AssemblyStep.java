package org.reasm;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.reasm.source.SourceNode;

/**
 * A step of an {@link Assembly}.
 *
 * @author Francis Gagné
 */
public final class AssemblyStep {

    @Nonnull
    private final AssemblyStepLocation location;
    private final long programCounter;
    @Nonnull
    private final OutputImpl output;
    private final long assembledDataStart;
    private long assembledDataLength;
    private boolean hasSideEffects;

    AssemblyStep(@Nonnull AssemblyStepLocation location, long programCounter, @Nonnull OutputImpl output) throws IOException {
        this.location = location;
        this.programCounter = programCounter;
        this.output = output;
        this.assembledDataStart = output.size();
    }

    /**
     * Gets the length of the assembled form of this assembly step.
     *
     * @return the length of the assembled form of this assembly step
     */
    public final long getAssembledDataLength() {
        return this.assembledDataLength;
    }

    /**
     * Gets the starting position of the assembled form of this assembly step in the {@linkplain #getOutput() output referenced by
     * this assembly step}.
     *
     * @return the starting position of the assembled form of this assembly step
     */
    public final long getAssembledDataStart() {
        return this.assembledDataStart;
    }

    /**
     * Gets the location of this assembly step.
     *
     * @return the assembly step location
     */
    @Nonnull
    public final AssemblyStepLocation getLocation() {
        return this.location;
    }

    /**
     * Gets the output where the assembled form of this assembly step is written to.
     *
     * @return an {@link Output} that gives access to the assembly step's output
     */
    public final Output getOutput() {
        return this.output;
    }

    /**
     * Gets the program counter on this assembly step.
     *
     * @return the program counter on this assembly step
     */
    public final long getProgramCounter() {
        return this.programCounter;
    }

    /**
     * Appends a byte to the assembled representation of this assembly step.
     *
     * @param b
     *            byte to append to the assembled representation of this assembly step
     * @throws IOException
     *             an I/O exception occurred while performing the operation
     */
    final void appendAssembledData(byte b) throws IOException {
        this.output.write(b);
        this.assembledDataLength += 1;
    }

    /**
     * Appends bytes to the assembled representation of this assembly step.
     *
     * @param data
     *            bytes to append to the assembled representation of this assembly step
     * @throws IOException
     *             an I/O exception occurred while performing the operation
     */
    final void appendAssembledData(byte[] data) throws IOException {
        this.output.write(data);
        this.assembledDataLength += data.length;
    }

    /**
     * Appends bytes to the assembled representation of this assembly step.
     *
     * @param data
     *            array of bytes containing the data to append to the assembled representation of the step
     * @param offset
     *            the starting offset in the data array
     * @param length
     *            the length of the data in the data array to append to this assembly step
     * @throws IOException
     *             an I/O exception occurred while performing the operation
     */
    final void appendAssembledData(byte[] data, int offset, int length) throws IOException {
        this.output.write(data, offset, length);
        this.assembledDataLength += length;
    }

    final boolean hasSideEffects() {
        return this.hasSideEffects;
    }

    /**
     * Indicates that this assembly step has a side effect, i.e. it changes the assembler's state beyond outputting data and
     * defining symbols. This should be called when it is necessary to call {@link SourceNode#assembleCore(AssemblyBuilder)} on all
     * passes. Once this has been called for an assembly step, it is not possible to undo it.
     */
    final void setHasSideEffects() {
        this.hasSideEffects = true;
    }

}
