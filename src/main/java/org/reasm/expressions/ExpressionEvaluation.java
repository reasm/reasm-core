package org.reasm.expressions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.WeakHashMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.meta.When;

import org.reasm.Assembly;
import org.reasm.FloatValue;
import org.reasm.Function;
import org.reasm.SignedIntValue;
import org.reasm.StringValue;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueToBooleanVisitor;
import org.reasm.ValueVisitor;
import org.reasm.messages.CannotConvertStringToFloatErrorMessage;
import org.reasm.messages.DivisionByZeroErrorMessage;
import org.reasm.messages.FunctionOperandNotApplicableErrorMessage;

import com.google.common.primitives.UnsignedLongs;

final class ExpressionEvaluation {

    @Immutable
    enum Arithmetic {

        MULTIPLICATION("multiplication") {

            @Nonnull
            @Override
            public Value applyFloat(double value1, double value2, @Nonnull EvaluationContext evaluationContext) {
                return new FloatValue(value1 * value2);
            }

            @Nonnull
            @Override
            public Value applySigned(long value1, long value2, @Nonnull EvaluationContext evaluationContext) {
                return new SignedIntValue(value1 * value2);
            }

            @Nonnull
            @Override
            public Value applyUnsigned(long value1, long value2, @Nonnull EvaluationContext evaluationContext) {
                return new UnsignedIntValue(value1 * value2);
            }

        },

        DIVISION("division") {

            @CheckForNull
            @Override
            public Value applyFloat(double value1, double value2, @Nonnull EvaluationContext evaluationContext) {
                if (value2 == 0) {
                    evaluationContext.getAssemblyMessageConsumer().accept(new DivisionByZeroErrorMessage());
                    return null;
                }

                return new FloatValue(value1 / value2);
            }

            @CheckForNull
            @Override
            public Value applySigned(long value1, long value2, @Nonnull EvaluationContext evaluationContext) {
                if (value2 == 0) {
                    evaluationContext.getAssemblyMessageConsumer().accept(new DivisionByZeroErrorMessage());
                    return null;
                }

                return new SignedIntValue(value1 / value2);
            }

            @CheckForNull
            @Override
            public Value applyUnsigned(long value1, long value2, @Nonnull EvaluationContext evaluationContext) {
                if (value2 == 0) {
                    evaluationContext.getAssemblyMessageConsumer().accept(new DivisionByZeroErrorMessage());
                    return null;
                }

                return new UnsignedIntValue(UnsignedLongs.divide(value1, value2));
            }

        },

        MODULUS("modulus") {

            @CheckForNull
            @Override
            public Value applyFloat(double value1, double value2, @Nonnull EvaluationContext evaluationContext) {
                if (value2 == 0) {
                    evaluationContext.getAssemblyMessageConsumer().accept(new DivisionByZeroErrorMessage());
                    return null;
                }

                return new FloatValue(value1 % value2);
            }

            @CheckForNull
            @Override
            public Value applySigned(long value1, long value2, @Nonnull EvaluationContext evaluationContext) {
                if (value2 == 0) {
                    evaluationContext.getAssemblyMessageConsumer().accept(new DivisionByZeroErrorMessage());
                    return null;
                }

                return new SignedIntValue(value1 % value2);
            }

            @CheckForNull
            @Override
            public Value applyUnsigned(long value1, long value2, @Nonnull EvaluationContext evaluationContext) {
                if (value2 == 0) {
                    evaluationContext.getAssemblyMessageConsumer().accept(new DivisionByZeroErrorMessage());
                    return null;
                }

                return new UnsignedIntValue(UnsignedLongs.remainder(value1, value2));
            }

        },

        SUBTRACTION("subtraction") {

            @Nonnull
            @Override
            public Value applyFloat(double value1, double value2, @Nonnull EvaluationContext evaluationContext) {
                return new FloatValue(value1 - value2);
            }

            @Nonnull
            @Override
            public Value applySigned(long value1, long value2, @Nonnull EvaluationContext evaluationContext) {
                return new SignedIntValue(value1 - value2);
            }

            @Nonnull
            @Override
            public Value applyUnsigned(long value1, long value2, @Nonnull EvaluationContext evaluationContext) {
                // If the result is negative, return it as a signed integer. Note that the result might underflow.
                if (UnsignedLongs.compare(value1, value2) < 0) {
                    return new SignedIntValue(value1 - value2);
                }

                return new UnsignedIntValue(value1 - value2);
            }

        };

        @Nonnull
        private final String name;

        private Arithmetic(@Nonnull String name) {
            this.name = name;
        }

        @CheckForNull
        public abstract Value applyFloat(double value1, double value2, @Nonnull EvaluationContext evaluationContext);

        @CheckForNull
        public abstract Value applySigned(long value1, long value2, @Nonnull EvaluationContext evaluationContext);

        @CheckForNull
        public abstract Value applyUnsigned(long value1, long value2, @Nonnull EvaluationContext evaluationContext);

        @Nonnull
        public final String getName() {
            return this.name;
        }

    }

    @Immutable
    enum BinaryBitwise {

        BITWISE_AND("bitwise AND") {
            @Override
            public long apply(long value1, long value2) {
                return value1 & value2;
            }
        },

        BITWISE_XOR("bitwise XOR") {
            @Override
            public long apply(long value1, long value2) {
                return value1 ^ value2;
            }
        },

        BITWISE_OR("bitwise OR") {
            @Override
            public long apply(long value1, long value2) {
                return value1 | value2;
            }
        };

        @Nonnull
        private final String name;

        private BinaryBitwise(@Nonnull String name) {
            this.name = name;
        }

        public abstract long apply(long value1, long value2);

        @Nonnull
        public final String getName() {
            return this.name;
        }

    }

    @Immutable
    enum BinaryLogical {

        LOGICAL_AND(false),

        LOGICAL_OR(true);

        private final boolean shortCircuitValue;

        private BinaryLogical(boolean shortCircuitValue) {
            this.shortCircuitValue = shortCircuitValue;
        }

        public final boolean getShortCircuitValue() {
            return this.shortCircuitValue;
        }

    }

    @Immutable
    enum BitShift {

        BIT_SHIFT_LEFT("bit shift left") {

            @Nonnull
            @Override
            public Value applySigned(long value1, long value2) {
                return new SignedIntValue(value1 << value2);
            }

            @Nonnull
            @Override
            public Value applyUnsigned(long value1, long value2) {
                return new UnsignedIntValue(value1 << value2);
            }

        },

        BIT_SHIFT_RIGHT("bit shift right") {

            @Nonnull
            @Override
            public Value applySigned(long value1, long value2) {
                return new SignedIntValue(value1 >> value2);
            }

            @Nonnull
            @Override
            public Value applyUnsigned(long value1, long value2) {
                return new UnsignedIntValue(value1 >>> value2);
            }

        };

        @Nonnull
        private final String name;

        private BitShift(@Nonnull String name) {
            this.name = name;
        }

        @Nonnull
        public abstract Value applySigned(long value1, long value2);

        @Nonnull
        public abstract Value applyUnsigned(long value1, long value2);

        @Nonnull
        public final String getName() {
            return this.name;
        }

    }

    @Immutable
    enum Comparison {

        LESS_THAN {

            @Override
            public boolean testComparisonResult(int comparisonResult) {
                return comparisonResult < 0;
            }

        },

        LESS_THAN_OR_EQUAL_TO {

            @Override
            public boolean testComparisonResult(int comparisonResult) {
                return comparisonResult <= 0;
            }

        },

        GREATER_THAN {

            @Override
            public boolean testComparisonResult(int comparisonResult) {
                return comparisonResult > 0;
            }

        },

        GREATER_THAN_OR_EQUAL_TO {

            @Override
            public boolean testComparisonResult(int comparisonResult) {
                return comparisonResult >= 0;
            }

        },

        EQUAL_TO {

            @Override
            public boolean testComparisonResult(int comparisonResult) {
                return comparisonResult == 0;
            }

        },

        DIFFERENT_FROM {

            @Override
            public boolean testComparisonResult(int comparisonResult) {
                return comparisonResult != 0;
            }

        };

        public abstract boolean testComparisonResult(int comparisonResult);

    }

    @Immutable
    enum StrictEquality {

        STRICTLY_EQUAL_TO(true),

        STRICTLY_DIFFERENT_FROM(false);

        private final boolean equal;

        private StrictEquality(boolean equal) {
            this.equal = equal;
        }

        public final boolean isEqual() {
            return this.equal;
        }

    }

    private static abstract class BaseValueTransformer implements ValueVisitor<Value> {

        BaseValueTransformer() {
        }

        @Override
        public Value visitUndetermined() {
            return null;
        }

    }

    private static abstract class BinaryValueTransformer extends NumericValueTransformer {

        BinaryValueTransformer(@Nonnull EvaluationContext evaluationContext, @Nonnull String operator) {
            super(evaluationContext, operator);
        }

        @Override
        public Value visitFloat(double value) {
            return this.visitSignedInt((long) value);
        }

    }

    private static abstract class NumericValueTransformer extends BaseValueTransformer {

        @Nonnull
        protected final EvaluationContext evaluationContext;
        @Nonnull
        protected final String operatorName;

        NumericValueTransformer(@Nonnull EvaluationContext evaluationContext, @Nonnull String operatorName) {
            this.evaluationContext = evaluationContext;
            this.operatorName = operatorName;
        }

        @Override
        public Value visitFunction(@Nonnull Function value) {
            this.evaluationContext.getAssemblyMessageConsumer().accept(
                    new FunctionOperandNotApplicableErrorMessage(this.operatorName));
            return null;
        }

        @Override
        public Value visitString(@Nonnull String value) {
            final Double floatValue = parseFloat(value, this.evaluationContext);
            if (floatValue == null) {
                return null;
            }

            return this.visitFloat(floatValue);
        }

    }

    private static abstract class StrictEqualityValueTester implements ValueVisitor<Boolean> {

        StrictEqualityValueTester() {
        }

        @Override
        public Boolean visitFloat(double value) {
            return false;
        }

        @Override
        public Boolean visitFunction(@Nonnull Function value) {
            return false;
        }

        @Override
        public Boolean visitSignedInt(long value) {
            return false;
        }

        @Override
        public Boolean visitString(@Nonnull String value) {
            return false;
        }

        @Nonnull(when = When.NEVER)
        @Override
        public Boolean visitUndetermined() {
            return null;
        }

        @Override
        public Boolean visitUnsignedInt(long value) {
            return false;
        }

    }

    private static abstract class ValueTransformer extends BaseValueTransformer {

        ValueTransformer() {
        }

        public abstract Value visitDefault();

        @Override
        public Value visitFloat(double value) {
            return this.visitDefault();
        }

        @Override
        public Value visitFunction(@Nonnull Function value) {
            return this.visitDefault();
        }

        @Override
        public Value visitSignedInt(long value) {
            return this.visitDefault();
        }

        @Override
        public Value visitString(@Nonnull String value) {
            return this.visitDefault();
        }

        @Override
        public Value visitUnsignedInt(long value) {
            return this.visitDefault();
        }

    }

    static final WeakHashMap<Assembly, ArrayList<WeakReference<Function>>> FUNCTION_LISTS_BY_ASSEMBLY = new WeakHashMap<>();

    @Nonnull
    static Value booleanToValue(boolean value) {
        return new UnsignedIntValue(value ? 1 : 0);
    }

    @CheckForNull
    static Value evaluateAddition(@CheckForNull Value operand1, @CheckForNull final Value operand2,
            final @Nonnull EvaluationContext evaluationContext) {
        final String operatorName = "addition";
        return Value.accept(operand1, new NumericValueTransformer(evaluationContext, operatorName) {

            @Override
            public Value visitFloat(final double value1) {
                return Value.accept(operand2, new NumericValueTransformer(this.evaluationContext, this.operatorName) {

                    @Override
                    public Value visitFloat(double value2) {
                        return new FloatValue(value1 + value2);
                    }

                    @Override
                    public Value visitSignedInt(long value2) {
                        return new FloatValue(value1 + value2);
                    }

                    @Override
                    public Value visitString(@Nonnull String value2) {
                        return new StringValue(Double.toString(value1) + value2);
                    }

                    @Override
                    public Value visitUnsignedInt(long value2) {
                        return new FloatValue(value1 + unsignedToFloat(value2));
                    }

                });
            }

            @Override
            public Value visitSignedInt(final long value1) {
                return Value.accept(operand2, new NumericValueTransformer(this.evaluationContext, this.operatorName) {

                    @Override
                    public Value visitFloat(double value2) {
                        return new FloatValue(value1 + value2);
                    }

                    @Override
                    public Value visitSignedInt(long value2) {
                        return new SignedIntValue(value1 + value2);
                    }

                    @Override
                    public Value visitString(@Nonnull String value2) {
                        return new StringValue(Long.toString(value1) + value2);
                    }

                    @Override
                    public Value visitUnsignedInt(long value2) {
                        return new SignedIntValue(value1 + value2);
                    }

                });
            }

            @Override
            public Value visitString(@Nonnull final String value1) {
                return Value.accept(operand2, new NumericValueTransformer(this.evaluationContext, this.operatorName) {

                    @Override
                    public Value visitFloat(double value2) {
                        return this.visitString(Double.toString(value2));
                    }

                    @Override
                    public Value visitSignedInt(long value2) {
                        return this.visitString(Long.toString(value2));
                    }

                    @Override
                    public Value visitString(@Nonnull String value2) {
                        return new StringValue(value1 + value2);
                    }

                    @Override
                    public Value visitUnsignedInt(long value2) {
                        return this.visitString(UnsignedLongs.toString(value2));
                    }

                });
            }

            @Override
            public Value visitUnsignedInt(final long value1) {
                return Value.accept(operand2, new NumericValueTransformer(this.evaluationContext, this.operatorName) {

                    @Override
                    public Value visitFloat(double value2) {
                        return new FloatValue(unsignedToFloat(value1) + value2);
                    }

                    @Override
                    public Value visitSignedInt(long value2) {
                        return new SignedIntValue(value1 + value2);
                    }

                    @Override
                    public Value visitString(@Nonnull String value2) {
                        return new StringValue(UnsignedLongs.toString(value1) + value2);
                    }

                    @Override
                    public Value visitUnsignedInt(long value2) {
                        return new UnsignedIntValue(value1 + value2);
                    }

                });
            }

        });
    }

    @CheckForNull
    static Value evaluateArithmetic(@CheckForNull Value operand1, @CheckForNull final Value operand2,
            @Nonnull EvaluationContext evaluationContext, @Nonnull final Arithmetic operator) {
        final String operatorName = operator.getName();
        return Value.accept(operand1, new NumericValueTransformer(evaluationContext, operatorName) {

            @Override
            public Value visitFloat(final double value1) {
                return Value.accept(operand2, new NumericValueTransformer(this.evaluationContext, this.operatorName) {

                    @Override
                    public Value visitFloat(double value2) {
                        return operator.applyFloat(value1, value2, this.evaluationContext);
                    }

                    @Override
                    public Value visitSignedInt(long value2) {
                        return operator.applyFloat(value1, value2, this.evaluationContext);
                    }

                    @Override
                    public Value visitUnsignedInt(long value2) {
                        return operator.applyFloat(value1, unsignedToFloat(value2), this.evaluationContext);
                    }

                });
            }

            @Override
            public Value visitSignedInt(final long value1) {
                return Value.accept(operand2, new NumericValueTransformer(this.evaluationContext, this.operatorName) {

                    @Override
                    public Value visitFloat(double value2) {
                        return operator.applyFloat(value1, value2, this.evaluationContext);
                    }

                    @Override
                    public Value visitSignedInt(long value2) {
                        return operator.applySigned(value1, value2, this.evaluationContext);
                    }

                    @Override
                    public Value visitUnsignedInt(long value2) {
                        return operator.applySigned(value1, value2, this.evaluationContext);
                    }

                });
            }

            @Override
            public Value visitUnsignedInt(final long value1) {
                return Value.accept(operand2, new NumericValueTransformer(this.evaluationContext, this.operatorName) {

                    @Override
                    public Value visitFloat(double value2) {
                        return operator.applyFloat(unsignedToFloat(value1), value2, this.evaluationContext);
                    }

                    @Override
                    public Value visitSignedInt(long value2) {
                        return operator.applySigned(value1, value2, this.evaluationContext);
                    }

                    @Override
                    public Value visitUnsignedInt(long value2) {
                        return operator.applyUnsigned(value1, value2, this.evaluationContext);
                    }

                });
            }

        });
    }

    @CheckForNull
    static Value evaluateBinaryBitwise(@CheckForNull Value operand1, @CheckForNull final Value operand2,
            @Nonnull EvaluationContext evaluationContext, @Nonnull final BinaryBitwise operator) {
        final String operatorName = operator.getName();
        return Value.accept(operand1, new BinaryValueTransformer(evaluationContext, operatorName) {

            @Override
            public Value visitSignedInt(final long value1) {
                return Value.accept(operand2, new BinaryValueTransformer(this.evaluationContext, this.operatorName) {

                    @Override
                    public Value visitSignedInt(long value2) {
                        return new UnsignedIntValue(operator.apply(value1, value2));
                    }

                    @Override
                    public Value visitUnsignedInt(long value2) {
                        return new UnsignedIntValue(operator.apply(value1, value2));
                    }

                });
            }

            @Override
            public Value visitUnsignedInt(final long value1) {
                return Value.accept(operand2, new BinaryValueTransformer(this.evaluationContext, this.operatorName) {

                    @Override
                    public Value visitSignedInt(long value2) {
                        return new UnsignedIntValue(operator.apply(value1, value2));
                    }

                    @Override
                    public Value visitUnsignedInt(long value2) {
                        return new UnsignedIntValue(operator.apply(value1, value2));
                    }

                });
            }

        });
    }

    @CheckForNull
    static Value evaluateBinaryLogical(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull BinaryLogical operator) {
        Boolean left = valueToBoolean(operand1);

        if (left == null) {
            return null;
        }

        if (left.booleanValue() == operator.getShortCircuitValue()) {
            return operand1;
        }

        return operand2;
    }

    @CheckForNull
    static Value evaluateBitShift(@CheckForNull Value operand1, @CheckForNull final Value operand2,
            final @Nonnull EvaluationContext evaluationContext, @Nonnull final BitShift operator) {
        final String operatorName = operator.getName();
        return Value.accept(operand1, new BinaryValueTransformer(evaluationContext, operatorName) {

            @Override
            public Value visitSignedInt(final long value1) {
                return Value.accept(operand2, new BinaryValueTransformer(this.evaluationContext, this.operatorName) {

                    @Override
                    public Value visitSignedInt(long value2) {
                        return operator.applySigned(value1, value2);
                    }

                    @Override
                    public Value visitUnsignedInt(long value2) {
                        return operator.applySigned(value1, value2);
                    }

                });
            }

            @Override
            public Value visitUnsignedInt(final long value1) {
                return Value.accept(operand2, new BinaryValueTransformer(this.evaluationContext, this.operatorName) {

                    @Override
                    public Value visitSignedInt(long value2) {
                        return operator.applyUnsigned(value1, value2);
                    }

                    @Override
                    public Value visitUnsignedInt(long value2) {
                        return operator.applyUnsigned(value1, value2);
                    }

                });
            }

        });
    }

    @CheckForNull
    static Value evaluateBitwiseNot(@CheckForNull Value operand, @Nonnull EvaluationContext evaluationContext) {
        return Value.accept(operand, new NumericValueTransformer(evaluationContext, "bitwise NOT") {

            @Override
            public Value visitFloat(double value) {
                return this.visitUnsignedInt((long) value);
            }

            @Override
            public Value visitSignedInt(long value) {
                return this.visitUnsignedInt(value);
            }

            @Override
            public Value visitUnsignedInt(long value) {
                return new UnsignedIntValue(~value);
            }

        });
    }

    @CheckForNull
    static Value evaluateComparison(@CheckForNull Value operand1, @CheckForNull final Value operand2,
            final @Nonnull EvaluationContext evaluationContext, @Nonnull final Comparison operator) {
        final Integer comparisonResult = Value.accept(operand1, new ValueVisitor<Integer>() {

            @Override
            public Integer visitFloat(final double value1) {
                return Value.accept(operand2, new ValueVisitor<Integer>() {

                    @Override
                    public Integer visitFloat(double value2) {
                        return Double.compare(value1, value2);
                    }

                    @Override
                    public Integer visitFunction(@Nonnull Function value2) {
                        // Functions are "greater" than non-function values.
                        return -1;
                    }

                    @Override
                    public Integer visitSignedInt(long value2) {
                        return Double.compare(value1, value2);
                    }

                    @Override
                    public Integer visitString(@Nonnull String value2) {
                        return Double.toString(value1).compareTo(value2);
                    }

                    @Override
                    public Integer visitUndetermined() {
                        return null;
                    }

                    @Override
                    public Integer visitUnsignedInt(long value2) {
                        return Double.compare(value1, unsignedToFloat(value2));
                    }

                });
            }

            @Override
            public Integer visitFunction(@Nonnull final Function value1) {
                return Value.accept(operand2, new ValueVisitor<Integer>() {

                    @Override
                    public Integer visitFloat(double value2) {
                        // Functions are "greater" than non-function values.
                        return 1;
                    }

                    @Override
                    public Integer visitFunction(@Nonnull Function value2) {
                        // Best case: we are comparing a function with itself.
                        if (value1 == value2) {
                            return 0;
                        }

                        // To provide coherent comparison for functions, we store them in a list and compare their index in the
                        // list. We can't use hash codes because we can't guarantee that distinct functions will have distinct hash
                        // codes. We use a distinct list for each assembly so that the results will be the same when you try to
                        // assemble the same source code multiple times.

                        ArrayList<WeakReference<Function>> functions;
                        synchronized (FUNCTION_LISTS_BY_ASSEMBLY) {
                            final Assembly assembly = evaluationContext.getAssembly();
                            functions = FUNCTION_LISTS_BY_ASSEMBLY.get(assembly);

                            if (functions == null) {
                                functions = new ArrayList<>();
                                FUNCTION_LISTS_BY_ASSEMBLY.put(assembly, functions);
                            }
                        }

                        synchronized (functions) {
                            for (int i = 0; i < functions.size();) {
                                final Function function = functions.get(i).get();
                                if (function == null) {
                                    // The reference's referent has been garbage collected; remove the entry.
                                    functions.remove(i);
                                    continue; // do NOT increment i
                                }

                                if (function == value1) {
                                    // We found value1 first: value1 < value2.
                                    return -1;
                                }

                                if (function == value2) {
                                    // We found value2 first: value1 > value2.
                                    return 1;
                                }

                                i++;
                            }

                            // We found neither; add value1 to the list and return -1 (value1 < value2).
                            functions.add(new WeakReference<>(value1));
                            return -1;
                        }
                    }

                    @Override
                    public Integer visitSignedInt(long value2) {
                        // Functions are "greater" than non-function values.
                        return 1;
                    }

                    @Override
                    public Integer visitString(@Nonnull String value2) {
                        // Functions are "greater" than non-function values.
                        return 1;
                    }

                    @Override
                    public Integer visitUndetermined() {
                        return null;
                    }

                    @Override
                    public Integer visitUnsignedInt(long value2) {
                        // Functions are "greater" than non-function values.
                        return 1;
                    }

                });
            }

            @Override
            public Integer visitSignedInt(final long value1) {
                return Value.accept(operand2, new ValueVisitor<Integer>() {

                    @Override
                    public Integer visitFloat(double value2) {
                        return Double.compare(value1, value2);
                    }

                    @Override
                    public Integer visitFunction(@Nonnull Function value2) {
                        // Functions are "greater" than non-function values.
                        return -1;
                    }

                    @Override
                    public Integer visitSignedInt(long value2) {
                        return Long.compare(value1, value2);
                    }

                    @Override
                    public Integer visitString(@Nonnull String value2) {
                        return Long.toString(value1).compareTo(value2);
                    }

                    @Override
                    public Integer visitUndetermined() {
                        return null;
                    }

                    @Override
                    public Integer visitUnsignedInt(long value2) {
                        int comparisonResult;
                        if (value1 < 0 || value2 < 0) { // value1 < 0 || value2 >= 2**63
                            comparisonResult = -1;
                        } else {
                            comparisonResult = Long.compare(value1, value2);
                        }

                        return comparisonResult;
                    }

                });
            }

            @Override
            public Integer visitString(@Nonnull final String value1) {
                return Value.accept(operand2, new ValueVisitor<Integer>() {

                    @Override
                    public Integer visitFloat(double value2) {
                        return value1.compareTo(Double.toString(value2));
                    }

                    @Override
                    public Integer visitFunction(@Nonnull Function value2) {
                        // Functions are "greater" than non-function values.
                        return -1;
                    }

                    @Override
                    public Integer visitSignedInt(long value2) {
                        return value1.compareTo(Long.toString(value2));
                    }

                    @Override
                    public Integer visitString(@Nonnull String value2) {
                        return value1.compareTo(value2);
                    }

                    @Override
                    public Integer visitUndetermined() {
                        return null;
                    }

                    @Override
                    public Integer visitUnsignedInt(long value2) {
                        return value1.compareTo(UnsignedLongs.toString(value2));
                    }

                });
            }

            @Override
            public Integer visitUndetermined() {
                return null;
            }

            @Override
            public Integer visitUnsignedInt(final long value1) {
                return Value.accept(operand2, new ValueVisitor<Integer>() {

                    @Override
                    public Integer visitFloat(double value2) {
                        return Double.compare(unsignedToFloat(value1), value2);
                    }

                    @Override
                    public Integer visitFunction(@Nonnull Function value2) {
                        // Functions are "greater" than non-function values.
                        return -1;
                    }

                    @Override
                    public Integer visitSignedInt(long value2) {
                        int comparisonResult;
                        if (value1 < 0 || value2 < 0) { // value1 >= 2**63 || value2 < 0
                            comparisonResult = 1;
                        } else {
                            comparisonResult = Long.compare(value1, value2);
                        }

                        return comparisonResult;
                    }

                    @Override
                    public Integer visitString(@Nonnull String value2) {
                        return UnsignedLongs.toString(value1).compareTo(value2);
                    }

                    @Override
                    public Integer visitUndetermined() {
                        return null;
                    }

                    @Override
                    public Integer visitUnsignedInt(long value2) {
                        return UnsignedLongs.compare(value1, value2);
                    }

                });
            }

        });

        if (comparisonResult == null) {
            return null;
        }

        return booleanToValue(operator.testComparisonResult(comparisonResult));
    }

    @CheckForNull
    static Value evaluateLogicalNot(@CheckForNull Value operand) {
        final Boolean booleanValue = valueToBoolean(operand);
        if (booleanValue != null) {
            return booleanToValue(!booleanValue);
        }

        return null;
    }

    @CheckForNull
    static Value evaluateNegation(@CheckForNull Value operand, @Nonnull EvaluationContext evaluationContext) {
        return Value.accept(operand, new NumericValueTransformer(evaluationContext, "negation") {

            @Override
            public Value visitFloat(double value) {
                return new FloatValue(-value);
            }

            @Override
            public Value visitSignedInt(long value) {
                return new SignedIntValue(-value);
            }

            @Override
            public Value visitUnsignedInt(long value) {
                return this.visitSignedInt(value);
            }

        });
    }

    @CheckForNull
    static Value evaluateStrictEquality(@CheckForNull Value operand1, @CheckForNull final Value operand2,
            @Nonnull StrictEquality operator) {
        // For strict equality, the types of the operands must match. If they don't match, the values are not strictly equal
        // (they are strictly different).

        Boolean equalityResult = Value.accept(operand1, new ValueVisitor<Boolean>() {

            @Override
            public Boolean visitFloat(final double value1) {
                return Value.accept(operand2, new StrictEqualityValueTester() {

                    @Override
                    public Boolean visitFloat(double value2) {
                        return value1 == value2;
                    }

                });
            }

            @Override
            public Boolean visitFunction(@Nonnull final Function value1) {
                return Value.accept(operand2, new StrictEqualityValueTester() {

                    @Override
                    public Boolean visitFunction(@Nonnull Function value2) {
                        return value1 == value2;
                    }

                });
            }

            @Override
            public Boolean visitSignedInt(final long value1) {
                return Value.accept(operand2, new StrictEqualityValueTester() {

                    @Override
                    public Boolean visitSignedInt(long value2) {
                        return value1 == value2;
                    }

                });
            }

            @Override
            public Boolean visitString(@Nonnull final String value1) {
                return Value.accept(operand2, new StrictEqualityValueTester() {

                    @Override
                    public Boolean visitString(@Nonnull String value2) {
                        return value1.equals(value2);
                    }

                });
            }

            @Nonnull(when = When.NEVER)
            @Override
            public Boolean visitUndetermined() {
                return null;
            }

            @Override
            public Boolean visitUnsignedInt(final long value1) {
                return Value.accept(operand2, new StrictEqualityValueTester() {

                    @Override
                    public Boolean visitUnsignedInt(long value2) {
                        return value1 == value2;
                    }

                });
            }

        });

        if (equalityResult == null) {
            return null;
        }

        return booleanToValue(equalityResult.booleanValue() == operator.isEqual());
    }

    @CheckForNull
    static Value evaluateUnaryPlus(@CheckForNull final Value operand, @CheckForNull final EvaluationContext evaluationContext) {
        // The unary plus operator forces the result to be signed.
        // - Unsigned integer values become signed integer values.
        // - String values are converted to float values.
        // - Function values raise an error.
        // - Other values are left unaltered.
        final Value result = Value.accept(operand, new ValueTransformer() {

            @Override
            public Value visitDefault() {
                return operand;
            }

            @Override
            public Value visitFunction(@Nonnull Function value) {
                evaluationContext.getAssemblyMessageConsumer().accept(new FunctionOperandNotApplicableErrorMessage("unary +"));
                return null;
            }

            @Override
            public Value visitString(@Nonnull String value) {
                final Double floatValue = parseFloat(value, evaluationContext);
                if (floatValue == null) {
                    return null;
                }

                return new FloatValue(floatValue);
            }

            @Override
            public Value visitUnsignedInt(long value) {
                return new SignedIntValue(value);
            }

        });

        return result;
    }

    static Double parseFloat(@Nonnull String value, @Nonnull EvaluationContext evaluationContext) {
        int i = 0;
        int codePoint;

        // Skip leading whitespace.
        for (; i < value.length(); i += Character.charCount(codePoint)) {
            codePoint = value.codePointAt(i);
            if (!Character.isWhitespace(codePoint)) {
                break;
            }
        }

        if (i < value.length()) {
            // Keep some flags about what parts are present in the number.
            boolean haveIntegerDigits = false;
            boolean haveFractionalDigits = false;
            boolean haveScientificENotation = false;
            boolean haveScientificENotationDigits = false;

            // If the first character is a '+' or a '-', accept it.
            boolean negative = false;
            codePoint = value.codePointAt(i);
            switch (codePoint) {
            case '-':
                negative = true;
                // fall through

            case '+':
                i += Character.charCount(codePoint);
            }

            // The number starts here.
            int numberStartPosition = i;

            // Read digits.
            for (; i < value.length(); i += Character.charCount(codePoint)) {
                codePoint = value.codePointAt(i);
                if (!isDigit(codePoint)) {
                    break;
                }

                haveIntegerDigits = true;
            }

            if (i < value.length()) {
                // If the next character is a point, accept it, then parse the fractional part.
                if (codePoint == '.') {
                    i += Character.charCount(codePoint);

                    // Read fractional digits.
                    for (; i < value.length(); i += Character.charCount(codePoint)) {
                        codePoint = value.codePointAt(i);
                        if (!isDigit(codePoint)) {
                            break;
                        }

                        haveFractionalDigits = true;
                    }
                }

                if (i < value.length()) {
                    // If the next character is an 'E' or an 'e', parse the exponent.
                    if (codePoint == 'E' || codePoint == 'e') {
                        haveScientificENotation = true;
                        i += Character.charCount(codePoint);

                        if (i < value.length()) {
                            // If the next character is a '+' or a '-', accept it.
                            codePoint = value.codePointAt(i);
                            if (codePoint == '+' || codePoint == '-') {
                                i += Character.charCount(codePoint);
                            }

                            // Read exponent digits.
                            for (; i < value.length(); i += Character.charCount(codePoint)) {
                                codePoint = value.codePointAt(i);
                                if (!isDigit(codePoint)) {
                                    break;
                                }

                                haveScientificENotationDigits = true;
                            }
                        }
                    }
                }
            }

            // The number ends here.
            int numberEndPosition = i;

            // Skip trailing whitespace.
            for (; i < value.length(); i += Character.charCount(codePoint)) {
                codePoint = value.codePointAt(i);
                if (!Character.isWhitespace(codePoint)) {
                    break;
                }
            }

            // Ensure that there are no more characters left in the string.
            if (i == value.length()) {
                // Ensure that there are enough parts to make a sensible number.
                // This rejects values such as "-.E".
                // - Ensure that there are digits either in the integer part or in the fractional part.
                //   Values such as "1." and ".5" are accepted.
                // - If the 'E' or 'e' is present, ensure that there is at least one digit after it.
                if ((haveIntegerDigits || haveFractionalDigits) && implies(haveScientificENotation, haveScientificENotationDigits)) {
                    final double floatValue = Expression.parseFloatWithOverflow(value.substring(numberStartPosition,
                            numberEndPosition));
                    return negative ? -floatValue : floatValue;
                }
            }
        }

        evaluationContext.getAssemblyMessageConsumer().accept(new CannotConvertStringToFloatErrorMessage(value));
        return null;
    }

    static double unsignedToFloat(long value) {
        if (value < 0) { // value >= 2**63
            // By shifting, we lose the least significant bit, but a double doesn't have enough precision to represent that bit
            // anyway.
            return (value >>> 1) * 2.;
        }

        return value;
    }

    @CheckForNull
    static Boolean valueToBoolean(@CheckForNull Value value) {
        return Value.accept(value, ValueToBooleanVisitor.INSTANCE);
    }

    private static boolean implies(boolean a, boolean b) {
        return !a || b;
    }

    private static boolean isDigit(int codePoint) {
        return codePoint >= '0' && codePoint <= '9';
    }

    // This class is not meant to be instantiated.
    private ExpressionEvaluation() {
    }

}
