package org.reasm;

import javax.annotation.Nonnull;

/**
 * An expression value visitor. This interface provides a method for each type of value.
 *
 * @param <T>
 *            the return type of all methods in the interface
 *
 * @author Francis Gagné
 */
public interface ValueVisitor<T> {

    /**
     * This method is called when an expression evaluates to a floating-point number.
     *
     * @param value
     *            the value of the expression
     * @return the result of visiting the value
     *
     * @see FloatValue
     */
    T visitFloat(double value);

    /**
     * This method is called when an expression evaluates to a function.
     *
     * @param value
     *            the value of the expression
     * @return the result of visiting the value
     *
     * @see FunctionValue
     */
    T visitFunction(@Nonnull Function value);

    /**
     * This method is called when an expression evaluates to an signed integer.
     *
     * @param value
     *            the value of the expression
     * @return the result of visiting the value
     *
     * @see SignedIntValue
     */
    T visitSignedInt(long value);

    /**
     * This method is called when an expression evaluates to a string.
     *
     * @param value
     *            the value of the expression
     * @return the result of visiting the value
     *
     * @see StringValue
     */
    T visitString(@Nonnull String value);

    /**
     * This method is called when an expression evaluates to <code>null</code>, which represents an undetermined value. This may
     * occur if the expression references symbols that are not defined at the point of evaluation.
     *
     * @return the result of visiting the value
     */
    T visitUndetermined();

    /**
     * This method is called when an expression evaluates to an unsigned integer. Note that the value is transmitted as a long;
     * negative values should be treated as if they were unsigned (for example, -1 is to be interpreted as 18446744073709551615).
     *
     * @param value
     *            the value of the expression
     * @return the result of visiting the value
     *
     * @see UnsignedIntValue
     */
    T visitUnsignedInt(long value);

}
