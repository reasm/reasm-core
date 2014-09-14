package org.reasm.messages;

import org.reasm.AssemblyFatalErrorMessage;
import org.reasm.Configuration;
import org.reasm.FileFetcher;

/**
 * A fatal error message that is generated during an assembly when a directive that tries to include another file is encountered,
 * but the configuration used to create the assembly didn't specify a file fetcher.
 *
 * @author Francis Gagné
 * @see Configuration#setFileFetcher(FileFetcher)
 */
public final class NoFileFetcherErrorMessage extends AssemblyFatalErrorMessage {

    /**
     * Initializes a new NoFileFetcherErrorMessage.
     */
    public NoFileFetcherErrorMessage() {
        super("No file fetcher in the current configuration; cannot determine how to retrieve external files");
    }

}
