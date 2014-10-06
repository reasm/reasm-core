package org.reasm;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A block specifies a list of steps to perform and can react to {@linkplain BlockEvents events}.
 *
 * @author Francis Gagné
 */
public final class Block {

    @Nonnull
    private final AssemblyStepLocationGenerator stepLocationGenerator;
    @CheckForNull
    private final BlockEvents events;

    Block(@Nonnull AssemblyStepLocationGenerator stepLocationGenerator, @CheckForNull BlockEvents events) {
        this.stepLocationGenerator = stepLocationGenerator;
        this.events = events;
    }

    /**
     * Gets the object that handles this block's events.
     *
     * @return the {@link BlockEvents}
     */
    @CheckForNull
    public final BlockEvents getEvents() {
        return this.events;
    }

    final void exitBlock() throws IOException {
        if (this.events != null) {
            this.events.exitBlock();
        }
    }

    final boolean hasNextLocation() {
        return this.stepLocationGenerator.hasNext();
    }

    @Nonnull
    final AssemblyStepLocation nextLocation() {
        return this.stepLocationGenerator.next();
    }

}
