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
    private ByteBuffer memoryData;
    @CheckForNull
    private Path tempFile;
    @CheckForNull
    private FileChannel tempFileChannel;

    OutputImpl(int memoryDataSize) {
        try {
            this.memoryData = ByteBuffer.allocate(memoryDataSize == 0 ? DEFAULT_MEMORY_SIZE : memoryDataSize);
        } catch (OutOfMemoryError e) {
            this.memoryData = ByteBuffer.allocate(DEFAULT_MEMORY_SIZE);
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
            final int putLength = Math.min(buffer.remaining(), this.memoryData.position() - (int) fromOffset);
            final ByteBuffer src = this.memoryData.duplicate();
            src.position((int) fromOffset);
            src.limit(src.position() + putLength);
            buffer.put(src);
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
            return this.tempFileChannel.position() + this.memoryData.position();
        }

        return this.memoryData.position();
    }

    @Override
    public void writeTo(@Nonnull OutputStream out) throws IOException {
        if (out == null) {
            throw new NullPointerException("out");
        }

        this.checkClosed();

        final FileChannel fileChannel = this.tempFileChannel;
        if (fileChannel == null) {
            out.write(this.memoryData.array(), 0, this.memoryData.position());
        } else {
            this.flush();
            long previousPosition = fileChannel.position();
            try {
                fileChannel.position(0);
                int bytesRead;
                while ((bytesRead = fileChannel.read(this.memoryData)) != -1) {
                    out.write(this.memoryData.array(), 0, bytesRead);
                    this.memoryData.clear();
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

        this.memoryData.clear();
    }

    void write(byte b) throws IOException {
        this.checkClosed();

        if (this.memoryData.position() >= this.memoryData.limit()) {
            if (this.tempFileChannel == null) {
                this.createTempFile();
            }

            this.flush();
        }

        this.memoryData.put(b);
    }

    void write(@Nonnull byte[] data) throws IOException {
        this.write(data, 0, data.length);
    }

    void write(@Nonnull byte[] data, int start, int length) throws IOException {
        this.checkClosed();

        if (this.memoryData.position() + length > this.memoryData.limit()) {
            if (this.tempFileChannel == null) {
                this.createTempFile();
            }

            this.flush();
        }

        if (length > this.memoryData.limit()) {
            this.writeToTempFile(ByteBuffer.wrap(data, start, length));
        } else {
            this.memoryData.put(data, start, length);
        }
    }

    void write(@Nonnull ByteBuffer data) throws IOException {
        this.checkClosed();

        final int length = data.remaining();
        if (this.memoryData.position() + length > this.memoryData.limit()) {
            if (this.tempFileChannel == null) {
                this.createTempFile();
            }

            this.flush();
        }

        if (length > this.memoryData.limit()) {
            this.writeToTempFile(data);
        } else {
            this.memoryData.put(data);
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
        this.memoryData.flip();
        this.writeToTempFile(this.memoryData);
        this.memoryData.clear();
    }

    private void writeToTempFile(@Nonnull ByteBuffer data) throws IOException {
        final FileChannel fileChannel = this.tempFileChannel;
        assert fileChannel != null;

        do {
            fileChannel.write(data);
        } while (data.hasRemaining());
    }

}
