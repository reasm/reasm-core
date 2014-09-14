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
        final byte[] buf = new byte[0x10000];
        final ByteBuffer byteBuffer = ByteBuffer.wrap(buf);

        long offset = 0;
        while (offset < size) {
            byteBuffer.rewind();
            final int bytesRead = output.read(offset, byteBuffer);
            builder.appendAssembledData(buf, 0, bytesRead);
            offset += bytesRead;
        }
    }

}
