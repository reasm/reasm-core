package org.reasm.testhelpers;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyBuilder;
import org.reasm.Output;
import org.reasm.OutputTransformation;

/**
 * An implementation of {@link OutputTransformation} that reverses the order of the bytes of a transformation block's output.
 *
 * @author Francis Gagné
 */
@Immutable
public final class ReverseTransformation implements OutputTransformation {

    /** The single instance of this class. */
    public static final ReverseTransformation INSTANCE = new ReverseTransformation();

    private ReverseTransformation() {
    }

    @Override
    public void transform(@Nonnull Output output, @Nonnull AssemblyBuilder builder) throws IOException {
        final int bufferSize = 1024;
        final byte[] buffer = new byte[bufferSize];
        final byte[] reversedBuffer = new byte[bufferSize];

        for (long i = output.size() - bufferSize;; i -= bufferSize) {
            if (i < 0) {
                i = 0;
            }

            final int bytesRead = output.read(i, buffer, 0, bufferSize);

            for (int j = 0; j < bytesRead; j++) {
                reversedBuffer[j] = buffer[bytesRead - j - 1];
            }

            builder.appendAssembledData(reversedBuffer, 0, bytesRead);

            if (i == 0) {
                break;
            }
        }
    }

}
