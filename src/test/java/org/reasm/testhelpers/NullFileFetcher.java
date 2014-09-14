package org.reasm.testhelpers;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.concurrent.Immutable;

import org.reasm.FileFetcher;
import org.reasm.source.SourceFile;

/**
 * Implements a file fetcher that always throws a {@link FileNotFoundException} in <code>fetchSourceFile</code> and
 * <code>fetchBinaryFile</code>.
 *
 * @author Francis Gagné
 */
@Immutable
public final class NullFileFetcher implements FileFetcher {

    @Override
    public byte[] fetchBinaryFile(String filePath) throws IOException {
        throw new FileNotFoundException(filePath);
    }

    @Override
    public SourceFile fetchSourceFile(String filePath) throws IOException {
        throw new FileNotFoundException(filePath);
    }

}
