package org.reasm;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

/**
 * Provides access to the output of an {@link Assembly} or of a transformation block.
 *
 * @author Francis Gagné
 */
public abstract class Output {

    Output() {
    }

    /**
     * Reads a sequence of bytes from this output.
     *
     * @param fromOffset
     *            the offset within this output to start reading from
     * @param buffer
     *            an array of bytes that will receive the output
     * @param toOffset
     *            the offset within the array of bytes to start reading into
     * @param length
     *            the length of the output to read
     * @return the number of bytes that was actually read
     * @throws IOException
     *             an I/O exception occurred
     */
    public final int read(long fromOffset, byte[] buffer, int toOffset, int length) throws IOException {
        return this.read(fromOffset, ByteBuffer.wrap(buffer, toOffset, length));
    }

    /**
     * Reads a sequence of bytes from this output.
     *
     * @param fromOffset
     *            the offset within this output to start reading from
     * @param buffer
     *            the {@link ByteBuffer} to read into
     * @return the number of bytes that was actually read
     * @throws IOException
     *             an I/O exception occurred
     */
    public abstract int read(long fromOffset, ByteBuffer buffer) throws IOException;

    /**
     * Gets the size of this output.
     *
     * @return the number of bytes in this output
     * @throws IOException
     *             an I/O exception occurred
     */
    public abstract long size() throws IOException;

    /**
     * Writes the contents of this output to the specified {@link OutputStream}.
     *
     * @param out
     *            the {@link OutputStream} that will receive the output
     * @throws IOException
     *             an I/O exception occurred
     */
    public abstract void writeTo(@Nonnull OutputStream out) throws IOException;

}
