package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyMessage;
import org.reasm.Function;
import org.reasm.Value;
import org.reasm.ValueVisitor;

import com.google.common.primitives.UnsignedLongs;

/**
 * An expression. Expressions produce values when they are evaluated.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class Expression {

    private static final ValueVisitor<String> VALUE_TO_STRING_VISITOR = new ValueVisitor<String>() {

        @Nonnull
        @Override
        public String visitFloat(double value) {
            return Double.toString(value);
        }

        @Nonnull
        @Override
        public String visitFunction(Function value) {
            return "<function>";
        }

        @Nonnull
        @Override
        public String visitSignedInt(long value) {
            return Long.toString(value);
        }

        @Nonnull
        @Override
        public String visitString(String value) {
            return quoteString(value);
        }

        @Nonnull
        @Override
        public String visitUndetermined() {
            return "<undetermined>";
        }

        @Nonnull
        @Override
        public String visitUnsignedInt(long value) {
            return UnsignedLongs.toString(value);
        }

    };

    /**
     * Parses a floating-point number from the specified {@link CharSequence}.
     *
     * @param value
     *            the {@link CharSequence} to parse a number from
     * @return the parsed number
     */
    public static double parseFloatWithOverflow(@Nonnull CharSequence value) {
        if (value == null) {
            throw new NullPointerException("value");
        }

        double result = 0.0;
        int i;
        int exponent = -1;
        char ch = '\0';

        // Find the exponent of the left-most digit.
        for (i = 0; i < value.length(); i++) {
            ch = value.charAt(i);
            if (!isDigit(ch)) {
                break;
            }

            exponent++;
        }

        loop: for (i = 0; i < value.length(); i++) {
            ch = value.charAt(i);

            switch (ch) {
            case '.':
            case 'E':
            case 'e':
                i++;
                break loop;
            }

            // Compute the value of the current digit.
            assert isDigit(ch);
            int digit = ch - '0';

            // Update the result with this digit. Multiply the digit by 10**exponent, where exponent corresponds to the digit's
            // position, to get the best precision.
            // TODO: detect overflow/underflow/loss of precision and report as a warning?
            result += digit * Math.pow(10, exponent);
            exponent--;
        }

        assert exponent == -1;

        if (ch == '.') {
            for (; i < value.length(); i++) {
                ch = value.charAt(i);

                if (ch == 'E' || ch == 'e') {
                    i++;
                    break;
                }

                // Compute the value of the current digit.
                assert isDigit(ch);
                int digit = ch - '0';

                // Update the fraction with this digit.
                // TODO: detect overflow/underflow/loss of precision and report as a warning?
                result += digit * Math.pow(10, exponent);
                exponent--;
            }
        }

        int scientificENotationExponent = 0;
        int scientificENotationExponentSign = 1;
        if (ch == 'E' || ch == 'e') {
            ch = value.charAt(i);
            if (ch == '+') {
                i++;
            } else if (ch == '-') {
                scientificENotationExponentSign = -1;
                i++;
            }

            for (; i < value.length(); i++) {
                ch = value.charAt(i);

                // Compute the value of the current digit.
                assert isDigit(ch);
                int digit = ch - '0';

                // Update the exponent with this digit.
                // TODO: detect overflow and report as a warning?
                scientificENotationExponent *= 10;
                scientificENotationExponent += digit;
            }
        }

        return result * Math.pow(10, scientificENotationExponent * scientificENotationExponentSign);
    }

    /**
     * Surrounds a string with quotes and escapes some characters.
     *
     * @param string
     *            the string to quote
     * @return the quoted string
     */
    @Nonnull
    public static String quoteString(@Nonnull String string) {
        if (string == null) {
            throw new NullPointerException("string");
        }

        StringBuilder sb = new StringBuilder();
        sb.append('"');

        int codePoint;
        for (int i = 0; i < string.length(); i += Character.charCount(codePoint)) {
            codePoint = string.codePointAt(i);

            switch (codePoint) {
            case 0:
                sb.append("\\0");
                break;

            case '\n':
                sb.append("\\n");
                break;

            case '\r':
                sb.append("\\r");
                break;

            case '"':
                sb.append("\\\"");
                break;

            default:
                sb.appendCodePoint(codePoint);
                break;
            }
        }

        return sb.append('"').toString();
    }

    /**
     * Returns a string containing a user-friendly representation of a {@link Value} for diagnostic purposes (for example, in an
     * {@link AssemblyMessage}).
     *
     * @param value
     *            the value
     * @return the string representation of the value
     */
    @Nonnull
    public static String valueToString(@CheckForNull Value value) {
        return Value.accept(value, VALUE_TO_STRING_VISITOR);
    }

    private static boolean isDigit(int codePoint) {
        return codePoint >= '0' && codePoint <= '9';
    }

    /**
     * Initializes a new Expression.
     */
    protected Expression() {
    }

    @Override
    public abstract boolean equals(Object obj);

    /**
     * Computes the {@link Value} of this expression.
     *
     * @param evaluationContext
     *            the {@link EvaluationContext} in which the expression is evaluated
     *
     * @return the value of this expression
     */
    @CheckForNull
    public abstract Value evaluate(@Nonnull EvaluationContext evaluationContext);

    @Override
    public abstract int hashCode();

    /**
     * Gets an identifier out of this expression.
     *
     * @param evaluationContext
     *            the {@link EvaluationContext} in which the expression is evaluated
     * @param valueVisitor
     *            a {@link ValueVisitor} that converts a {@link Value} to a {@link String}
     * @return the identifier
     */
    @CheckForNull
    public abstract String toIdentifier(@Nonnull EvaluationContext evaluationContext, @Nonnull ValueVisitor<String> valueVisitor);

}
