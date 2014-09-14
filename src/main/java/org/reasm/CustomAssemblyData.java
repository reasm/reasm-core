package org.reasm;

/**
 * An interface for custom assembly data. This interface defines methods that are called by the assembling process to notify it of
 * events that occur during assembly.
 *
 * @author Francis Gagné
 */
public interface CustomAssemblyData {

    /**
     * Notifies the custom assembly data that the assembly is complete.
     */
    void completed();

    /**
     * Notifies the custom assembly data that a new pass has started in the assembly.
     */
    void startedNewPass();

}
