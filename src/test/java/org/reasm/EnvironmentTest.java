package org.reasm;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.testhelpers.NullArchitecture;

import com.google.common.base.Function;

/**
 * Test class for {@link Environment}.
 *
 * @author Francis Gagné
 */
public class EnvironmentTest {

    /**
     * Tests that calling the transformation methods on an environment in any order yields an environment with the same properties.
     *
     * @author Francis Gagné
     */
    @RunWith(Parameterized.class)
    public static class AllSettingsTest {

        private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

        static {
            final Function<Environment, Environment> addArchitecture = new Function<Environment, Environment>() {
                @Override
                public Environment apply(Environment input) {
                    return input.addArchitecture(ARCH1);
                }
            };

            final Function<Environment, Environment> setOutputMemorySize = new Function<Environment, Environment>() {
                @Override
                public Environment apply(Environment input) {
                    return input.setOutputMemorySize(SPECIFIC_OUTPUT_MEMORY_SIZE);
                }
            };

            final Function<Environment, Environment> addOutputTransformationFactory = new Function<Environment, Environment>() {
                @Override
                public Environment apply(Environment input) {
                    return input.addOutputTransformationFactory(OTF1);
                }
            };

            final List<Function<Environment, Environment>> functions = Arrays.asList(addArchitecture, setOutputMemorySize,
                    addOutputTransformationFactory);
            for (final List<Function<Environment, Environment>> rotation : rotations(functions)) {
                TEST_DATA.add(new Object[] { rotation });
            }
        }

        /**
         * Gets the test data for this parameterized test.
         *
         * @return the test data
         */
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        private static <T> List<List<T>> rotations(List<T> list) {
            List<List<T>> result = new ArrayList<>();
            result.add(list);

            final int size = list.size();
            for (int i = 1; i < size; i++) {
                List<T> rotation = new ArrayList<>(list.subList(i, size));
                rotation.addAll(list.subList(0, i));
                result.add(rotation);
            }

            return result;
        }

        private final List<Function<Environment, Environment>> transformations;

        /**
         * Initializes a new AllSettingsTest.
         *
         * @param transformations
         *            a list of transformations to apply to the {@linkplain Environment#DEFAULT default environment}
         */
        public AllSettingsTest(List<Function<Environment, Environment>> transformations) {
            this.transformations = transformations;
        }

        /**
         * Asserts that calling the transformation methods on an environment in any order yields an environment with the same
         * properties.
         */
        @Test
        public void test() {
            Environment environment = Environment.DEFAULT;

            for (final Function<Environment, Environment> transformation : this.transformations) {
                environment = transformation.apply(environment);
            }

            assertThat(environment.getArchitectures(), contains(ARCH1));
            assertThat(environment.getOutputMemorySize(), is(SPECIFIC_OUTPUT_MEMORY_SIZE));
            assertThat(environment.getOutputTransformationFactories(), contains(OTF1));
        }

    }

    static final Architecture ARCH1 = new NullArchitecture();
    private static final Architecture ARCH2 = new NullArchitecture();
    private static final List<Architecture> ARCHES = Arrays.asList(ARCH1, ARCH2);

    private static final int SPECIFIC_OUTPUT_MEMORY_SIZE = 0x4000;

    static final OutputTransformationFactory OTF1 = new DummyOutputTransformationFactory(null);
    private static final OutputTransformationFactory OTF2 = new DummyOutputTransformationFactory(null);
    private static final List<OutputTransformationFactory> OTFS = Arrays.asList(OTF1, OTF2);

    /**
     * Ensures that an environment is immutable.
     *
     * @param environment
     *            the environment to test
     */
    private static void assertEnvironmentIsImmutable(@Nonnull Environment environment) {
        try {
            environment.getArchitectures().add(ARCH1); // should throw UnsupportedOperationException
            fail("Set.add(T) on Environment.getArchitectures() should have thrown UnsupportedOperationException.");
        } catch (UnsupportedOperationException e) {
            // Exception is expected
        }

        try {
            environment.getArchitectures().remove(ARCH1); // should throw UnsupportedOperationException
            fail("Set.remove(T) on Environment.getArchitectures() should have thrown UnsupportedOperationException.");
        } catch (UnsupportedOperationException e) {
            // Exception is expected
        }

        try {
            environment.getOutputTransformationFactories().add(OTF1); // should throw UnsupportedOperationException
            fail("Set.add(T) on Environment.getOutputTransformationFactories() should have thrown UnsupportedOperationException.");
        } catch (UnsupportedOperationException e) {
            // Exception is expected
        }

        try {
            environment.getOutputTransformationFactories().remove(OTF1); // should throw UnsupportedOperationException
            fail("Set.remove(T) on Environment.getOutputTransformationFactories() should have thrown "
                    + "UnsupportedOperationException.");
        } catch (UnsupportedOperationException e) {
            // Exception is expected
        }
    }

    /**
     * Asserts that {@link Environment#addArchitecture(Architecture)} returns a new {@link Environment} with the specified
     * {@link Architecture} added to it when the architecture is not already part of the environment.
     */
    @Test
    public void addArchitecture() {
        final Environment initialEnvironment = Environment.DEFAULT.setOutputMemorySize(SPECIFIC_OUTPUT_MEMORY_SIZE)
                .addOutputTransformationFactory(OTF1);
        final Environment newEnvironment = initialEnvironment.addArchitecture(ARCH1);
        assertThat(newEnvironment, is(not(initialEnvironment)));
        assertThat(newEnvironment.getArchitectures(), contains(ARCH1));
        assertThat(newEnvironment.getOutputMemorySize(), is(SPECIFIC_OUTPUT_MEMORY_SIZE));
        assertThat(newEnvironment.getOutputTransformationFactories(), contains(OTF1));
        assertEnvironmentIsImmutable(newEnvironment);
    }

    /**
     * Asserts that {@link Environment#addArchitecture(Architecture)} throws a {@link NullPointerException} when the
     * <code>architecture</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void addArchitectureNull() {
        Environment.DEFAULT.addArchitecture(null);
    }

    /**
     * Asserts that {@link Environment#addArchitecture(Architecture)} returns the original {@link Environment} when the specified
     * architecture is already part of the environment.
     */
    @Test
    public void addArchitectureRedundant() {
        final Environment initialEnvironment = Environment.DEFAULT.addArchitecture(ARCH1);
        final Environment newEnvironment = initialEnvironment.addArchitecture(ARCH1);
        assertThat(newEnvironment, is(sameInstance(initialEnvironment)));
    }

    /**
     * Asserts that {@link Environment#addArchitectures(Collection)} returns a new {@link Environment} with the specified
     * {@link Architecture Architectures} added to it when some of the architectures are not already part of the environment.
     */
    @Test
    public void addArchitectures() {
        final Environment initialEnvironment = Environment.DEFAULT.setOutputMemorySize(SPECIFIC_OUTPUT_MEMORY_SIZE)
                .addOutputTransformationFactory(OTF1);
        final Environment newEnvironment = initialEnvironment.addArchitectures(ARCHES);
        assertThat(newEnvironment, is(not(initialEnvironment)));
        assertThat(newEnvironment.getArchitectures(), containsInAnyOrder(ARCH1, ARCH2));
        assertThat(newEnvironment.getOutputMemorySize(), is(SPECIFIC_OUTPUT_MEMORY_SIZE));
        assertThat(newEnvironment.getOutputTransformationFactories(), contains(OTF1));
        assertEnvironmentIsImmutable(newEnvironment);
    }

    /**
     * Asserts that {@link Environment#addArchitectures(Collection)} throws a {@link NullPointerException} when the
     * <code>architectures</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void addArchitecturesNull() {
        Environment.DEFAULT.addArchitectures(null);
    }

    /**
     * Asserts that {@link Environment#addArchitectures(Collection)} returns the original {@link Environment} when all of the
     * specified architectures are already part of the environment.
     */
    @Test
    public void addArchitecturesRedundant() {
        final Environment initialEnvironment = Environment.DEFAULT.addArchitectures(ARCHES);
        final Environment newEnvironment = initialEnvironment.addArchitectures(ARCHES);
        assertThat(newEnvironment, is(sameInstance(initialEnvironment)));
    }

    /**
     * Asserts that {@link Environment#addOutputTransformationFactories(Collection)} returns a new {@link Environment} with the
     * specified {@link OutputTransformationFactory OutputTransformationFactories} added to it when some of the output
     * transformation factories are not already part of the environment.
     */
    @Test
    public void addOutputTransformationFactories() {
        final Environment initialEnvironment = Environment.DEFAULT.addArchitecture(ARCH1).setOutputMemorySize(
                SPECIFIC_OUTPUT_MEMORY_SIZE);
        final Environment newEnvironment = initialEnvironment.addOutputTransformationFactories(OTFS);
        assertThat(newEnvironment, is(not(initialEnvironment)));
        assertThat(newEnvironment.getArchitectures(), contains(ARCH1));
        assertThat(newEnvironment.getOutputMemorySize(), is(SPECIFIC_OUTPUT_MEMORY_SIZE));
        assertThat(newEnvironment.getOutputTransformationFactories(), containsInAnyOrder(OTF1, OTF2));
        assertEnvironmentIsImmutable(newEnvironment);
    }

    /**
     * Asserts that {@link Environment#addOutputTransformationFactories(Collection)} throws a {@link NullPointerException} when the
     * <code>outputTransformationFactories</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void addOutputTransformationFactoriesNull() {
        Environment.DEFAULT.addOutputTransformationFactories(null);
    }

    /**
     * Asserts that {@link Environment#addOutputTransformationFactories(Collection)} returns the original {@link Environment} when
     * all of the specified output transformation factories are already part of the environment.
     */
    @Test
    public void addOutputTransformationFactoriesRedundant() {
        final Environment initialEnvironment = Environment.DEFAULT.addOutputTransformationFactories(OTFS);
        final Environment newEnvironment = initialEnvironment.addOutputTransformationFactories(OTFS);
        assertThat(newEnvironment, is(sameInstance(initialEnvironment)));
    }

    /**
     * Asserts that {@link Environment#addOutputTransformationFactory(OutputTransformationFactory)} returns a new
     * {@link Environment} with the specified {@link OutputTransformationFactory} added to it when the output transformation factory
     * is not already part of the environment.
     */
    @Test
    public void addOutputTransformationFactory() {
        final Environment initialEnvironment = Environment.DEFAULT.addArchitecture(ARCH1).setOutputMemorySize(
                SPECIFIC_OUTPUT_MEMORY_SIZE);
        final Environment newEnvironment = initialEnvironment.addOutputTransformationFactory(OTF1);
        assertThat(newEnvironment, is(not(initialEnvironment)));
        assertThat(newEnvironment.getArchitectures(), contains(ARCH1));
        assertThat(newEnvironment.getOutputMemorySize(), is(SPECIFIC_OUTPUT_MEMORY_SIZE));
        assertThat(newEnvironment.getOutputTransformationFactories(), contains(OTF1));
        assertEnvironmentIsImmutable(newEnvironment);
    }

    /**
     * Asserts that {@link Environment#addOutputTransformationFactory(OutputTransformationFactory)} throws a
     * {@link NullPointerException} when the <code>outputTransformationFactory</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void addOutputTransformationFactoryNull() {
        Environment.DEFAULT.addOutputTransformationFactory(null);
    }

    /**
     * Asserts that {@link Environment#addOutputTransformationFactory(OutputTransformationFactory)} returns the original
     * {@link Environment} when the specified output transformation factory is already part of the environment.
     */
    @Test
    public void addOutputTransformationFactoryRedundant() {
        final Environment initialEnvironment = Environment.DEFAULT.addOutputTransformationFactory(OTF1);
        final Environment newEnvironment = initialEnvironment.addOutputTransformationFactory(OTF1);
        assertThat(newEnvironment, is(sameInstance(initialEnvironment)));
    }

    /**
     * Asserts that {@link Environment#DEFAULT} has the expected attributes.
     */
    @Test
    public void defaultEnvironment() {
        Environment environment = Environment.DEFAULT;
        assertThat(environment.getArchitectures(), is(empty()));
        assertThat(environment.getOutputMemorySize(), is(0));
        assertThat(environment.getOutputTransformationFactories(), is(empty()));
        assertEnvironmentIsImmutable(environment);
    }

    /**
     * Asserts that {@link Environment#findArchitectureByName(String)} finds a named architecture in an {@link Environment}.
     */
    @Test
    public void findArchitectureByName() {
        final Architecture architecture = new NullArchitecture("a");
        final Environment environment = Environment.DEFAULT.addArchitecture(architecture);
        assertThat(environment.findArchitectureByName("a"), is(sameInstance(architecture)));
    }

    /**
     * Asserts that {@link Environment#findArchitectureByName(String)} finds an architecture that has several names by its first
     * name.
     */
    @Test
    public void findArchitectureByNameFirstName() {
        final Architecture architecture = new NullArchitecture("a", "b", "c");
        final Environment environment = Environment.DEFAULT.addArchitecture(architecture);
        assertThat(environment.findArchitectureByName("a"), is(sameInstance(architecture)));
    }

    /**
     * Asserts that {@link Environment#findArchitectureByName(String)} finds an architecture that has several names by its last
     * name.
     */
    @Test
    public void findArchitectureByNameLastName() {
        final Architecture architecture = new NullArchitecture("a", "b", "c");
        final Environment environment = Environment.DEFAULT.addArchitecture(architecture);
        assertThat(environment.findArchitectureByName("c"), is(sameInstance(architecture)));
    }

    /**
     * Asserts that {@link Environment#findArchitectureByName(String)} doesn't find an architecture for a name that is not present
     * on the environment's architectures.
     */
    @Test
    public void findArchitectureByNameNotFound() {
        final Architecture architecture = new NullArchitecture("a");
        final Environment environment = Environment.DEFAULT.addArchitecture(architecture);
        assertThat(environment.findArchitectureByName("b"), is(nullValue()));
    }

    /**
     * Asserts that {@link Environment#findArchitectureByName(String)} returns <code>null</code> when the
     * <code>architectureName</code> argument is <code>null</code>, even when the environment contains an architecture that has a
     * <code>null</code> name.
     */
    @Test
    public void findArchitectureByNameNullName() {
        final Architecture architecture = new NullArchitecture(new String[] { null });
        final Environment environment = Environment.DEFAULT.addArchitecture(architecture);
        assertThat(environment.findArchitectureByName(null), is(nullValue()));
    }

    /**
     * Asserts that {@link Environment#findOutputTransformationFactoryByName(String)} finds a named output transformation factory in
     * an {@link Environment}.
     */
    @Test
    public void findOutputTransformationFactoryByName() {
        final OutputTransformationFactory outputTransformationFactory = new DummyOutputTransformationFactory(
                Collections.singleton("a"));
        final Environment environment = Environment.DEFAULT.addOutputTransformationFactory(outputTransformationFactory);
        assertThat(environment.findOutputTransformationFactoryByName("a"), is(sameInstance(outputTransformationFactory)));
    }

    /**
     * Asserts that {@link Environment#removeArchitecture(Architecture)} return a new {@link Environment} with the specified
     * {@link Architecture} removed from it when the architecture is part of the environment.
     */
    @Test
    public void removeArchitecture() {
        final Environment initialEnvironment = Environment.DEFAULT.addArchitecture(ARCH1)
                .setOutputMemorySize(SPECIFIC_OUTPUT_MEMORY_SIZE).addOutputTransformationFactory(OTF1);
        final Environment newEnvironment = initialEnvironment.removeArchitecture(ARCH1);
        assertThat(newEnvironment, is(not(initialEnvironment)));
        assertThat(newEnvironment.getArchitectures(), is(empty()));
        assertThat(newEnvironment.getOutputMemorySize(), is(SPECIFIC_OUTPUT_MEMORY_SIZE));
        assertThat(newEnvironment.getOutputTransformationFactories(), contains(OTF1));
        assertEnvironmentIsImmutable(newEnvironment);
    }

    /**
     * Asserts that {@link Environment#removeArchitecture(Architecture)} throws a {@link NullPointerException} when the
     * <code>architecture</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void removeArchitectureNull() {
        Environment.DEFAULT.removeArchitecture(null);
    }

    /**
     * Asserts that {@link Environment#removeArchitecture(Architecture)} returns the original {@link Environment} when the specified
     * architecture is not part of the environment.
     */
    @Test
    public void removeArchitectureRedundant() {
        final Environment initialEnvironment = Environment.DEFAULT;
        final Environment newEnvironment = initialEnvironment.removeArchitecture(ARCH1);
        assertThat(newEnvironment, is(sameInstance(initialEnvironment)));
    }

    /**
     * Asserts that {@link Environment#removeArchitectures(Collection)} returns a new {@link Environment} with the specified
     * {@link Architecture Architectures} removed from when some of the architectures are part of the environment.
     */
    @Test
    public void removeArchitectures() {
        final Environment initialEnvironment = Environment.DEFAULT.addArchitectures(ARCHES)
                .setOutputMemorySize(SPECIFIC_OUTPUT_MEMORY_SIZE).addOutputTransformationFactory(OTF1);
        final Environment newEnvironment = initialEnvironment.removeArchitectures(ARCHES);
        assertThat(newEnvironment, is(not(initialEnvironment)));
        assertThat(newEnvironment.getArchitectures(), is(empty()));
        assertThat(newEnvironment.getOutputMemorySize(), is(SPECIFIC_OUTPUT_MEMORY_SIZE));
        assertThat(newEnvironment.getOutputTransformationFactories(), contains(OTF1));
        assertEnvironmentIsImmutable(newEnvironment);
    }

    /**
     * Asserts that {@link Environment#removeArchitectures(Collection)} throws a {@link NullPointerException} when the
     * <code>architectures</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void removeArchitecturesNull() {
        Environment.DEFAULT.removeArchitectures(null);
    }

    /**
     * Asserts that {@link Environment#removeArchitectures(Collection)} returns the original {@link Environment} when none of the
     * specified architectures are part of the environment.
     */
    @Test
    public void removeArchitecturesRedundant() {
        final Environment initialEnvironment = Environment.DEFAULT;
        final Environment newEnvironment = initialEnvironment.removeArchitectures(ARCHES);
        assertThat(newEnvironment, is(sameInstance(initialEnvironment)));
    }

    /**
     * Asserts that {@link Environment#removeOutputTransformationFactories(Collection)} returns a new {@link Environment} with the
     * specified {@link OutputTransformationFactory OutputTransformationFactories} removed from when some of the output
     * transformation factories are part of the environment.
     */
    @Test
    public void removeOutputTransformationFactories() {
        final Environment initialEnvironment = Environment.DEFAULT.addArchitecture(ARCH1)
                .setOutputMemorySize(SPECIFIC_OUTPUT_MEMORY_SIZE).addOutputTransformationFactories(OTFS);
        final Environment newEnvironment = initialEnvironment.removeOutputTransformationFactories(OTFS);
        assertThat(newEnvironment, is(not(initialEnvironment)));
        assertThat(newEnvironment.getArchitectures(), contains(ARCH1));
        assertThat(newEnvironment.getOutputMemorySize(), is(SPECIFIC_OUTPUT_MEMORY_SIZE));
        assertThat(newEnvironment.getOutputTransformationFactories(), is(empty()));
        assertEnvironmentIsImmutable(newEnvironment);
    }

    /**
     * Asserts that {@link Environment#removeOutputTransformationFactories(Collection)} throws a {@link NullPointerException} when
     * the <code>outputTransformationFactories</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void removeOutputTransformationFactoriesNull() {
        Environment.DEFAULT.removeOutputTransformationFactories(null);
    }

    /**
     * Asserts that {@link Environment#removeOutputTransformationFactories(Collection)} returns the original {@link Environment}
     * when none of the specified output transformation factories are part of the environment.
     */
    @Test
    public void removeOutputTransformationFactoriesRedundant() {
        final Environment initialEnvironment = Environment.DEFAULT;
        final Environment newEnvironment = initialEnvironment.removeOutputTransformationFactories(OTFS);
        assertThat(newEnvironment, is(sameInstance(initialEnvironment)));
    }

    /**
     * Asserts that {@link Environment#removeOutputTransformationFactory(OutputTransformationFactory)} return a new
     * {@link Environment} with the specified {@link OutputTransformationFactory} removed from it when the output transformation
     * factory is part of the environment.
     */
    @Test
    public void removeOutputTransformationFactory() {
        final Environment initialEnvironment = Environment.DEFAULT.addArchitecture(ARCH1)
                .setOutputMemorySize(SPECIFIC_OUTPUT_MEMORY_SIZE).addOutputTransformationFactory(OTF1);
        final Environment newEnvironment = initialEnvironment.removeOutputTransformationFactory(OTF1);
        assertThat(newEnvironment, is(not(initialEnvironment)));
        assertThat(newEnvironment.getArchitectures(), contains(ARCH1));
        assertThat(newEnvironment.getOutputMemorySize(), is(SPECIFIC_OUTPUT_MEMORY_SIZE));
        assertThat(newEnvironment.getOutputTransformationFactories(), is(empty()));
        assertEnvironmentIsImmutable(newEnvironment);
    }

    /**
     * Asserts that {@link Environment#removeOutputTransformationFactory(OutputTransformationFactory)} throws a
     * {@link NullPointerException} when the <code>outputTransformationFactory</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void removeOutputTransformationFactoryNull() {
        Environment.DEFAULT.removeOutputTransformationFactory(null);
    }

    /**
     * Asserts that {@link Environment#removeOutputTransformationFactory(OutputTransformationFactory)} returns the original
     * {@link Environment} when the specified output transformation factory is not part of the environment.
     */
    @Test
    public void removeOutputTransformationFactoryRedundant() {
        final Environment initialEnvironment = Environment.DEFAULT;
        final Environment newEnvironment = initialEnvironment.removeOutputTransformationFactory(OTF1);
        assertThat(newEnvironment, is(sameInstance(initialEnvironment)));
    }

    /**
     * Asserts that {@link Environment#setOutputMemorySize(int)} returns a new {@link Environment} with the specified default output
     * memory size.
     */
    @Test
    public void setOutputMemorySize() {
        final Environment initialEnvironment = Environment.DEFAULT.addArchitectures(ARCHES)
                .setOutputMemorySize(SPECIFIC_OUTPUT_MEMORY_SIZE).addOutputTransformationFactory(OTF1);
        final Environment newEnvironment = initialEnvironment.setOutputMemorySize(0x8000);
        assertThat(newEnvironment, is(not(initialEnvironment)));
        assertThat(newEnvironment.getArchitectures(), hasSize(2));
        assertThat(newEnvironment.getArchitectures(), hasItem(ARCH1));
        assertThat(newEnvironment.getArchitectures(), hasItem(ARCH2));
        assertThat(newEnvironment.getOutputMemorySize(), is(0x8000));
        assertThat(newEnvironment.getOutputTransformationFactories(), contains(OTF1));
        assertEnvironmentIsImmutable(newEnvironment);
    }

    /**
     * Asserts that {@link Environment#setOutputMemorySize(int)} throws an {@link IllegalArgumentException} when the
     * <code>outputMemorySize</code> parameter is negative.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setOutputMemorySizeInvalid() {
        Environment.DEFAULT.setOutputMemorySize(-1);
    }

    /**
     * Asserts that {@link Environment#setOutputMemorySize(int)} returns the original {@link Environment} when the specified default
     * output memory size is equal to the environment's default output memory size.
     */
    @Test
    public void setOutputMemorySizeRedundant() {
        final Environment initialEnvironment = Environment.DEFAULT.setOutputMemorySize(SPECIFIC_OUTPUT_MEMORY_SIZE);
        final Environment newEnvironment = initialEnvironment.setOutputMemorySize(SPECIFIC_OUTPUT_MEMORY_SIZE);
        assertThat(newEnvironment, is(sameInstance(initialEnvironment)));
    }

}
