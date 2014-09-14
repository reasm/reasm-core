package org.reasm;

import java.io.IOException;

import javax.annotation.Nonnull;

final class TransformationBlock {

    @Nonnull
    private final OutputTransformation outputTransformation;
    @Nonnull
    private final AssemblyStep start;
    @Nonnull
    private final OutputImpl output;

    /**
     * Initializes a new TransformationBlock.
     *
     * @param outputTransformation
     * @param start
     * @param environment
     */
    TransformationBlock(@Nonnull OutputTransformation outputTransformation, @Nonnull AssemblyStep start,
            @Nonnull Environment environment) {
        this.outputTransformation = outputTransformation;
        this.start = start;
        this.output = new OutputImpl(environment.getOutputMemorySize());
    }

    @Nonnull
    final OutputImpl getOutput() {
        return this.output;
    }

    @Nonnull
    final AssemblyStep getStart() {
        return this.start;
    }

    final void transform(@Nonnull AssemblyBuilder builder) throws IOException {
        this.outputTransformation.transform(this.output, builder);
    }

}
