package org.reasm;

/**
 * An exception that indicates that reasm-core received an unacceptable result after calling a virtual method on some object. This
 * usually indicates a bug in a library used by reasm-core, not in reasm-core itself.
 *
 * @author Francis Gagné
 */
public class ContractViolationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Initializes a new ContractViolationException with <code>null</code> as its detail message. The cause is not initialized, and
     * may subsequently be initialized by a call to {@link #initCause}.
     *
     */
    public ContractViolationException() {
        super();
    }

    /**
     * Initializes a new ContractViolationException with the specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message
     *            the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public ContractViolationException(String message) {
        super(message);
    }

    /**
     * Initializes a new ContractViolationException with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with <code>cause</code> is <i>not</i> automatically incorporated in this runtime
     * exception's detail message.
     *
     * @param message
     *            the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <code>null</code> value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ContractViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes a new ContractViolationException with the specified cause and a detail message of
     * <code>(cause==null ? null : cause.toString())</code> (which typically contains the class and detail message of
     * <code>cause</code>). This constructor is useful for runtime exceptions that are little more than wrappers for other
     * throwables.
     *
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <code>null</code> value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ContractViolationException(Throwable cause) {
        super(cause);
    }

    /**
     * Initializes a new ContractViolationExceptionwith the specified detail message, cause, suppression enabled or disabled, and
     * writable stack trace enabled or disabled.
     *
     * @param message
     *            the detail message
     * @param cause
     *            the cause. (A <code>null</code> value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression
     *            whether or not suppression is enabled or disabled
     * @param writableStackTrace
     *            whether or not the stack trace should be writable
     */
    protected ContractViolationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
