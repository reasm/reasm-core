package org.reasm;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * An {@link OutputTransformation} that emits the output unaltered.
 *
 * @author Francis Gagné
 */
public final class IdentityTransformation implements OutputTransformation {

    /** The single instance of the {@link IdentityTransformation} class. */
    public static final IdentityTransformation INSTANCE = new IdentityTransformation();

    private IdentityTransformation() {
    }

    @Override
    public final void transform(Output output, AssemblyBuilder builder) throws IOException {
        final long size = output.size();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(0x10000);

        long offset = 0;
        while (offset < size) {
            byteBuffer.clear();
            final int bytesRead = output.read(offset, byteBuffer);
            byteBuffer.flip();
            builder.appendAssembledData(byteBuffer);
            offset += bytesRead;
        }
    }

}
