package org.reasm;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.reasm.source.SourceFile;

/**
 * Provides methods to get a {@link SourceFile} or a byte array for a file path referenced in source code.
 *
 * @author Francis Gagné
 */
public interface FileFetcher {

    /**
     * Returns the contents of the file corresponding to the given file path.
     *
     * @param filePath
     *            the path to the file, as written in the source code
     * @return the contents of the file as a byte array.
     * @throws IOException
     *             an I/O exception occurred while accessing the file
     */
    @Nonnull
    byte[] fetchBinaryFile(@Nonnull String filePath) throws IOException;

    /**
     * Returns the {@link SourceFile} corresponding to the given file path.
     *
     * @param filePath
     *            the path to the file, as written in the source code
     * @return the {@link SourceFile} corresponding to the file.
     * @throws IOException
     *             an I/O exception occurred while accessing the file
     */
    @Nonnull
    SourceFile fetchSourceFile(@Nonnull String filePath) throws IOException;

}
