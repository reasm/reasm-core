package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

import org.junit.Test;

/**
 * Test class for {@link OutputImpl}.
 *
 * @author Francis Gagné
 */
public class OutputImplTest {

    private static void checkOutput(@Nonnull OutputImpl o, @Nonnull byte[] bytes) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        o.writeTo(out);
        assertThat(out.toByteArray(), is(bytes));
    }

    /**
     * Asserts that {@link OutputImpl#clear()} clears the output's data.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void clear() throws IOException {
        try (final OutputImpl o = new OutputImpl(0x100)) {
            final byte[] bytes = new byte[0x40];
            o.write(bytes);
            o.clear();
            assertThat(o.size(), is(0L));
            checkOutput(o, new byte[0]);
        }
    }

    /**
     * Asserts that {@link OutputImpl#clear()} clears the output's data when a temporary file is used.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void clearFile() throws IOException {
        try (final OutputImpl o = new OutputImpl(0x100)) {
            final byte[] bytes = new byte[0x4000];
            o.write(bytes);
            o.clear();
            assertThat(o.size(), is(0L));
            checkOutput(o, new byte[0]);
        }
    }

    /**
     * Asserts that {@link OutputImpl#read(long, ByteBuffer)} reads data that was previously written to the output.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void read() throws IOException {
        try (final OutputImpl o = new OutputImpl(0)) {
            final byte[] data = new byte[] { 0x12, 0x34, 0x56, 0x78 };
            o.write(data);

            final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
            assertThat(o.read(0, byteBuffer), is(4));
            for (int i = 0; i < data.length; i++) {
                assertThat(byteBuffer.get(i), is(data[i]));
            }
        }
    }

    /**
     * Asserts that {@link OutputImpl#read(long, ByteBuffer)} throws an {@link IllegalArgumentException} when the
     * <code>fromOffset</code> argument is greater than the size of the data in the output.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void readFromOffsetTooHigh() throws IOException {
        try (final OutputImpl o = new OutputImpl(0)) {
            o.write(new byte[] { 0x12, 0x34, 0x56, 0x78 });

            try {
                o.read(5, ByteBuffer.allocateDirect(4));
                fail("OutputImpl.read() should have thrown IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // Exception is expected
            }
        }
    }

    /**
     * Asserts that {@link OutputImpl#read(long, ByteBuffer)} throws an {@link IllegalArgumentException} when the
     * <code>fromOffset</code> argument is negative.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void readFromOffsetTooLow() throws IOException {
        try (final OutputImpl o = new OutputImpl(0)) {
            o.write(new byte[] { 0x12, 0x34, 0x56, 0x78 });

            try {
                o.read(-1, ByteBuffer.allocateDirect(4));
                fail("OutputImpl.read() should have thrown IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // Exception is expected
            }
        }
    }

    /**
     * Asserts that {@link OutputImpl#read(long, ByteBuffer)} throws a {@link NullPointerException} when the <code>buffer</code>
     * argument is <code>null</code>.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void readNullByteBuffer() throws IOException {
        try (final OutputImpl o = new OutputImpl(0)) {
            try {
                o.read(0, null);
                fail("OutputImpl.read() should have thrown NullPointerException");
            } catch (NullPointerException e) {
                // Exception is expected
            }
        }
    }

    /**
     * Asserts that {@link OutputImpl#read(long, ByteBuffer)} reads data that was previously written to the output when a temporary
     * file was used.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void readTempFile() throws IOException {
        try (final OutputImpl o = new OutputImpl(1)) {
            final byte[] data = new byte[] { 0x12, 0x34, 0x56, 0x78 };
            o.write(data);

            final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
            assertThat(o.read(0, byteBuffer), is(4));
            for (int i = 0; i < data.length; i++) {
                assertThat(byteBuffer.get(i), is(data[i]));
            }
        }
    }

    /**
     * Asserts that {@link OutputImpl#write(byte)} writes a byte to the output.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void writeByte() throws IOException {
        try (final OutputImpl o = new OutputImpl(0x100)) {
            final byte b0 = (byte) 123;
            o.write(b0);
            assertThat(o.size(), is(1L));
            checkOutput(o, new byte[] { b0 });

            final byte b1 = (byte) 76;
            o.write(b1);
            assertThat(o.size(), is(2L));
            checkOutput(o, new byte[] { b0, b1 });
        }
    }

    /**
     * Asserts that {@link OutputImpl#write(byte[])} writes an array of bytes to the output.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void writeByteArray() throws IOException {
        try (final OutputImpl o = new OutputImpl(0x100)) {
            byte[] allBytes;

            final byte[] bytes0 = new byte[] { 12, 34, 56 };
            o.write(bytes0);
            assertThat(o.size(), is((long) bytes0.length));
            checkOutput(o, bytes0);

            final byte[] bytes1 = new byte[] { 65, 43, 21 };
            o.write(bytes1);
            assertThat(o.size(), is((long) (bytes0.length + bytes1.length)));
            allBytes = new byte[bytes0.length + bytes1.length];
            System.arraycopy(bytes0, 0, allBytes, 0, bytes0.length);
            System.arraycopy(bytes1, 0, allBytes, bytes0.length, bytes1.length);
            checkOutput(o, allBytes);

            o.write(new byte[0]);
            assertThat(o.size(), is((long) (bytes0.length + bytes1.length)));
            checkOutput(o, allBytes);
        }
    }

    /**
     * Asserts that {@link OutputImpl#write(byte[])} writes an array of bytes to the output when a temporary file is used.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void writeByteArrayFile() throws IOException {
        try (final OutputImpl o = new OutputImpl(0x100)) {
            final byte[] bytes = new byte[0x4000];
            o.write(bytes);
            assertThat(o.size(), is((long) bytes.length));
            checkOutput(o, bytes);

            o.write(bytes);
            assertThat(o.size(), is(bytes.length * 2L));
            checkOutput(o, new byte[0x8000]);
        }
    }

    /**
     * Asserts that accessing an {@link OutputImpl} after closing it throws an {@link IllegalStateException}.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void writeByteClosed() throws IOException {
        final OutputImpl o = new OutputImpl(0);
        try (OutputImpl o2 = o) {
        }

        try {
            o.write((byte) 0);
            fail("OutputImpl.checkClosed() (called through OutputImpl.write(byte)) should have thrown an IllegalStateException");
        } catch (IllegalStateException e) {
            // Exception is expected
        }
    }

    /**
     * Asserts that {@link OutputImpl#write(byte)} writes a byte to the output when a temporary file is used.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void writeByteFile() throws IOException {
        try (final OutputImpl o = new OutputImpl(1)) {
            final byte b0 = (byte) 123;
            o.write(b0);
            assertThat(o.size(), is(1L));
            checkOutput(o, new byte[] { b0 });

            final byte b1 = (byte) 76;
            o.write(b1);
            assertThat(o.size(), is(2L));
            checkOutput(o, new byte[] { b0, b1 });

            final byte b2 = (byte) 84;
            final byte b3 = (byte) 49;
            o.write(b2);
            o.write(b3);
            assertThat(o.size(), is(4L));
            checkOutput(o, new byte[] { b0, b1, b2, b3 });
        }
    }

    /**
     * Asserts that {@link OutputImpl#writeTo(java.io.OutputStream)} throws a {@link NullPointerException} when the <code>out</code>
     * argument is <code>null</code>.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void writeToNullOutputStream() throws IOException {
        try (final OutputImpl o = new OutputImpl(0)) {
            try {
                o.writeTo(null);
                fail("OutputImpl.writeTo() should have thrown a NullPointerException");
            } catch (NullPointerException e) {
                // Exception is expected
            }
        }
    }

}
