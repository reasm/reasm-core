package org.reasm;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class OutputImpl extends Output implements Closeable {

    private static final int DEFAULT_MEMORY_SIZE = 0x10000; // 64 KiB

    private boolean closed;
    @Nonnull
    private byte[] memoryData;
    @CheckForNull
    private Path tempFile;
    @CheckForNull
    private FileChannel tempFileChannel;
    private int memoryDataSize;

    OutputImpl(int memoryDataSize) {
        try {
            this.memoryData = new byte[memoryDataSize == 0 ? DEFAULT_MEMORY_SIZE : memoryDataSize];
        } catch (OutOfMemoryError e) {
            this.memoryData = new byte[DEFAULT_MEMORY_SIZE];
        }
    }

    @Override
    public void close() throws IOException {
        this.close(true);
    }

    @Override
    public int read(long fromOffset, @Nonnull ByteBuffer buffer) throws IOException {
        this.checkClosed();

        if (fromOffset < 0) {
            throw new IllegalArgumentException(String.format("fromOffset (%d) < 0", fromOffset));
        }

        if (fromOffset > this.size()) {
            throw new IllegalArgumentException(String.format("fromOffset (%d) > size (%d)", fromOffset, this.size()));
        }

        if (buffer == null) {
            throw new NullPointerException("buffer");
        }

        final FileChannel fileChannel = this.tempFileChannel;
        if (fileChannel == null) {
            final int putLength = Math.min(buffer.remaining(), this.memoryDataSize - (int) fromOffset);
            buffer.put(this.memoryData, (int) fromOffset, putLength);
            return putLength;
        }

        this.flush();
        long previousPosition = fileChannel.position();
        try {
            fileChannel.position(fromOffset);
            return fileChannel.read(buffer);
        } finally {
            fileChannel.position(previousPosition);
        }
    }

    @Override
    public long size() throws IOException {
        this.checkClosed();

        if (this.tempFileChannel != null) {
            return this.tempFileChannel.position() + this.memoryDataSize;
        }

        return this.memoryDataSize;
    }

    @Override
    public void writeTo(@Nonnull OutputStream out) throws IOException {
        if (out == null) {
            throw new NullPointerException("out");
        }

        this.checkClosed();

        final FileChannel fileChannel = this.tempFileChannel;
        if (fileChannel == null) {
            out.write(this.memoryData, 0, this.memoryDataSize);
        } else {
            this.flush();
            final ByteBuffer bb = ByteBuffer.wrap(this.memoryData);
            long previousPosition = fileChannel.position();
            try {
                fileChannel.position(0);
                int bytesRead;
                while ((bytesRead = fileChannel.read(bb)) != -1) {
                    out.write(this.memoryData, 0, bytesRead);
                    bb.position(0);
                }
            } finally {
                fileChannel.position(previousPosition);
            }
        }
    }

    protected void close(boolean closing) throws IOException {
        if (!this.closed) {
            try {
                try {
                    if (closing) {
                        FileChannel fileChannel = this.tempFileChannel;
                        if (fileChannel != null) {
                            this.tempFileChannel = null;
                            fileChannel.close();
                        }
                    }
                } finally {
                    final Path tempFile = this.tempFile;
                    if (tempFile != null) {
                        this.tempFile = null;
                        Files.deleteIfExists(tempFile);
                    }
                }
            } finally {
                this.closed = true;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.close(false);
    }

    void clear() throws IOException {
        this.checkClosed();

        if (this.tempFileChannel != null) {
            this.tempFileChannel.truncate(0);
        }

        this.memoryDataSize = 0;
    }

    void write(byte b) throws IOException {
        this.checkClosed();

        if (this.memoryDataSize >= this.memoryData.length) {
            if (this.tempFileChannel == null) {
                this.createTempFile();
            }

            this.flush();
        }

        this.memoryData[this.memoryDataSize++] = b;
    }

    void write(@Nonnull byte[] data) throws IOException {
        this.write(data, 0, data.length);
    }

    void write(@Nonnull byte[] data, int start, int length) throws IOException {
        this.checkClosed();

        if (this.memoryDataSize + length > this.memoryData.length) {
            if (this.tempFileChannel == null) {
                this.createTempFile();
            }

            this.flush();
        }

        if (length > this.memoryData.length) {
            this.writeToTempFile(data, start, length);
        } else {
            System.arraycopy(data, start, this.memoryData, this.memoryDataSize, length);
            this.memoryDataSize += length;
        }
    }

    private void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("Output was closed");
        }
    }

    private void createTempFile() throws IOException {
        final Path tempFile = Files.createTempFile("reasm", ".out");
        this.tempFileChannel = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.DELETE_ON_CLOSE);
        this.tempFile = tempFile;
    }

    private void flush() throws IOException {
        this.writeToTempFile(this.memoryData, 0, this.memoryDataSize);
        this.memoryDataSize = 0;
    }

    private void writeToTempFile(@Nonnull byte[] data, int offset, int length) throws IOException {
        final FileChannel fileChannel = this.tempFileChannel;
        assert fileChannel != null;

        final ByteBuffer buffer = ByteBuffer.wrap(data, offset, length);
        do {
            fileChannel.write(buffer);
        } while (buffer.hasRemaining());
    }

}
