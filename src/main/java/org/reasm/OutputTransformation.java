package org.reasm;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Provides a way to transform the contents of a {@link TransformationBlock} and to write the result to an {@link Assembly}.
 *
 * @author Francis Gagné
 */
public interface OutputTransformation {

    /**
     * Transforms an output and writes the result to the {@link Assembly} managed by the specified {@link AssemblyBuilder}, using
     * {@link AssemblyBuilder#appendAssembledData(byte)}, {@link AssemblyBuilder#appendAssembledData(byte[])} and/or
     * {@link AssemblyBuilder#appendAssembledData(byte[], int, int)}.
     *
     * @param output
     *            the output to transform
     * @param builder
     *            {@link AssemblyBuilder} that will receive the result
     * @throws IOException
     *             an I/O exception occurred while writing the result to the {@link Assembly}
     */
    void transform(@Nonnull Output output, @Nonnull AssemblyBuilder builder) throws IOException;

}
