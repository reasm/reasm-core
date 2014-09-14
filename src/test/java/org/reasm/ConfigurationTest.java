package org.reasm;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.reasm.source.SourceFile;
import org.reasm.testhelpers.NullArchitecture;
import org.reasm.testhelpers.NullFileFetcher;

/**
 * Test class for {@link Configuration}.
 *
 * @author Francis Gagné
 */
public class ConfigurationTest {

    private static final Environment EMPTY_ENVIRONMENT = Environment.DEFAULT;
    private static final SourceFile EMPTY_SOURCE_FILE = new SourceFile("", "");
    private static final FileFetcher NULL_FILE_FETCHER = new NullFileFetcher();
    private static final Architecture NULL_ARCHITECTURE = NullArchitecture.DEFAULT;
    private static final HashMap<Object, Object> SAMPLE_CUSTOM_CONFIGURATION_OPTIONS = new HashMap<>();
    private static final Object SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY = new Object();
    private static final Object SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_VALUE = new Object();

    private static final Configuration SAMPLE_CONFIGURATION = new Configuration(EMPTY_ENVIRONMENT, EMPTY_SOURCE_FILE,
            NULL_ARCHITECTURE);

    static {
        SAMPLE_CUSTOM_CONFIGURATION_OPTIONS.put(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY, SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_VALUE);
    }

    /**
     * Asserts that {@link Configuration#Configuration(Environment, SourceFile, Architecture)} correctly initializes a
     * {@link Configuration}.
     */
    @Test
    public void configurationEnvironmentSourceFileArchitecture() {
        final Configuration configuration = SAMPLE_CONFIGURATION;
        assertThat(configuration.getEnvironment(), is(EMPTY_ENVIRONMENT));
        assertThat(configuration.getMainSourceFile(), is(EMPTY_SOURCE_FILE));
        assertThat(configuration.getInitialArchitecture(), is(NULL_ARCHITECTURE));
        assertThat(configuration.getFileFetcher(), is(nullValue()));
        assertThat(configuration.getPredefinedSymbols(), is(not(nullValue())));
        assertThat(configuration.getPredefinedSymbols().symbols(), is(empty()));
        assertThat(configuration.hasCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(false));
        assertThat(configuration.getCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(nullValue()));
    }

    /**
     * Asserts that {@link Configuration#setCustomConfigurationOptions(Map)} creates a new {@link Configuration} with a different
     * map of custom configuration options.
     */
    @Test
    public void setCustomConfigurationOptions() {
        final Configuration configuration0 = SAMPLE_CONFIGURATION;
        final Configuration configuration1 = configuration0.setCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS);
        assertThat(configuration1, is(not(sameInstance(configuration0))));
        assertThat(configuration1.getEnvironment(), is(configuration0.getEnvironment()));
        assertThat(configuration1.getMainSourceFile(), is(configuration0.getMainSourceFile()));
        assertThat(configuration1.getInitialArchitecture(), is(configuration0.getInitialArchitecture()));
        assertThat(configuration1.getFileFetcher(), is(configuration0.getFileFetcher()));
        assertThat(configuration1.getPredefinedSymbols(), is(configuration0.getPredefinedSymbols()));
        assertThat(configuration1.hasCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(true));
        assertThat(configuration1.getCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY),
                is(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_VALUE));
    }

    /**
     * Asserts that {@link Configuration#setCustomConfigurationOptions(Map)} throws a {@link NullPointerException} when the
     * <code>customConfigurationOptions</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void setCustomConfigurationOptionsNull() {
        SAMPLE_CONFIGURATION.setCustomConfigurationOptions(null);
    }

    /**
     * Asserts that {@link Configuration#setEnvironment(Environment)} creates a new {@link Configuration} with a different
     * environment.
     */
    @Test
    public void setEnvironment() {
        final Configuration configuration0 = SAMPLE_CONFIGURATION;
        final Environment newEnvironment = Environment.DEFAULT.addArchitecture(NULL_ARCHITECTURE);
        final Configuration configuration1 = configuration0.setEnvironment(newEnvironment);
        assertThat(configuration1, is(not(sameInstance(configuration0))));
        assertThat(configuration1.getEnvironment(), is(sameInstance(newEnvironment)));
        assertThat(configuration1.getMainSourceFile(), is(configuration0.getMainSourceFile()));
        assertThat(configuration1.getInitialArchitecture(), is(configuration0.getInitialArchitecture()));
        assertThat(configuration1.getFileFetcher(), is(configuration0.getFileFetcher()));
        assertThat(configuration1.getPredefinedSymbols(), is(configuration0.getPredefinedSymbols()));
        assertThat(configuration1.hasCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(false));
        assertThat(configuration1.getCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(nullValue()));
    }

    /**
     * Asserts that {@link Configuration#setEnvironment(Environment)} throws a {@link NullPointerException} when the
     * <code>environment</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void setEnvironmentNull() {
        SAMPLE_CONFIGURATION.setEnvironment(null);
    }

    /**
     * Asserts that {@link Configuration#setEnvironment(Environment)} returns the original {@link Configuration} when the specified
     * environment is the same as the original {@link Configuration}'s environment.
     */
    @Test
    public void setEnvironmentSame() {
        final Configuration configuration0 = SAMPLE_CONFIGURATION;
        final Configuration configuration1 = configuration0.setEnvironment(configuration0.getEnvironment());
        assertThat(configuration1, is(sameInstance(configuration0)));
    }

    /**
     * Asserts that {@link Configuration#setFileFetcher(FileFetcher)} creates a new {@link Configuration} with a different file
     * fetcher.
     */
    @Test
    public void setFileFetcher() {
        final Configuration configuration0 = SAMPLE_CONFIGURATION;
        final FileFetcher newFileFetcher = NULL_FILE_FETCHER;
        final Configuration configuration1 = configuration0.setFileFetcher(newFileFetcher);
        assertThat(configuration1, is(not(sameInstance(configuration0))));
        assertThat(configuration1.getEnvironment(), is(configuration0.getEnvironment()));
        assertThat(configuration1.getMainSourceFile(), is(configuration0.getMainSourceFile()));
        assertThat(configuration1.getInitialArchitecture(), is(configuration0.getInitialArchitecture()));
        assertThat(configuration1.getFileFetcher(), is(newFileFetcher));
        assertThat(configuration1.getPredefinedSymbols(), is(configuration0.getPredefinedSymbols()));
        assertThat(configuration1.hasCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(false));
        assertThat(configuration1.getCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(nullValue()));
    }

    /**
     * Asserts that {@link Configuration#setFileFetcher(FileFetcher)} accepts a <code>null</code> argument.
     */
    @Test
    public void setFileFetcherNull() {
        final Configuration configuration0 = SAMPLE_CONFIGURATION.setFileFetcher(NULL_FILE_FETCHER);
        final FileFetcher newFileFetcher = null;
        final Configuration configuration1 = configuration0.setFileFetcher(newFileFetcher);
        assertThat(configuration1, is(not(sameInstance(configuration0))));
        assertThat(configuration1.getEnvironment(), is(configuration0.getEnvironment()));
        assertThat(configuration1.getMainSourceFile(), is(configuration0.getMainSourceFile()));
        assertThat(configuration1.getInitialArchitecture(), is(configuration0.getInitialArchitecture()));
        assertThat(configuration1.getFileFetcher(), is(newFileFetcher));
        assertThat(configuration1.getPredefinedSymbols(), is(configuration0.getPredefinedSymbols()));
        assertThat(configuration1.hasCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(false));
        assertThat(configuration1.getCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(nullValue()));
    }

    /**
     * Asserts that {@link Configuration#setFileFetcher(FileFetcher)} returns the original {@link Configuration} when the specified
     * file fetcher is the same as the original {@link Configuration}'s file fetcher.
     */
    @Test
    public void setFileFetcherSame() {
        final Configuration configuration0 = SAMPLE_CONFIGURATION.setFileFetcher(NULL_FILE_FETCHER);
        final Configuration configuration1 = configuration0.setFileFetcher(configuration0.getFileFetcher());
        assertThat(configuration1, is(sameInstance(configuration0)));
    }

    /**
     * Asserts that {@link Configuration#setMainSourceFile(SourceFile)} creates a new {@link Configuration} with a different main
     * source file.
     */
    @Test
    public void setMainSourceFile() {
        final Configuration configuration0 = SAMPLE_CONFIGURATION;
        final SourceFile newMainSourceFile = new SourceFile("a", "");
        final Configuration configuration1 = configuration0.setMainSourceFile(newMainSourceFile);
        assertThat(configuration1, is(not(sameInstance(configuration0))));
        assertThat(configuration1.getEnvironment(), is(configuration0.getEnvironment()));
        assertThat(configuration1.getMainSourceFile(), is(sameInstance(newMainSourceFile)));
        assertThat(configuration1.getInitialArchitecture(), is(configuration0.getInitialArchitecture()));
        assertThat(configuration1.getFileFetcher(), is(configuration0.getFileFetcher()));
        assertThat(configuration1.getPredefinedSymbols(), is(configuration0.getPredefinedSymbols()));
        assertThat(configuration1.hasCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(false));
        assertThat(configuration1.getCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(nullValue()));
    }

    /**
     * Asserts that {@link Configuration#setMainSourceFile(SourceFile)} throws a {@link NullPointerException} when the
     * <code>mainSourceFile</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void setMainSourceFileNull() {
        SAMPLE_CONFIGURATION.setMainSourceFile(null);
    }

    /**
     * Asserts that {@link Configuration#setMainSourceFile(SourceFile)} returns the original {@link Configuration} when the
     * specified main source file is the same as the original {@link Configuration}'s main source file.
     */
    @Test
    public void setMainSourceFileSame() {
        final Configuration configuration0 = SAMPLE_CONFIGURATION;
        final Configuration configuration1 = configuration0.setMainSourceFile(configuration0.getMainSourceFile());
        assertThat(configuration1, is(sameInstance(configuration0)));
    }

    /**
     * Asserts that {@link Configuration#setPredefinedSymbols(PredefinedSymbolTable)} creates a new {@link Configuration} with a
     * different table of predefined symbols.
     */
    @Test
    public void setPredefinedSymbols() {
        final Configuration configuration0 = SAMPLE_CONFIGURATION;
        final PredefinedSymbolTable newPredefinedSymbols = new PredefinedSymbolTable(Arrays.asList(new PredefinedSymbol(
                SymbolContext.VALUE, "foo", SymbolType.CONSTANT, new UnsignedIntValue(1))));
        final Configuration configuration1 = configuration0.setPredefinedSymbols(newPredefinedSymbols);
        assertThat(configuration1, is(not(sameInstance(configuration0))));
        assertThat(configuration1.getEnvironment(), is(configuration0.getEnvironment()));
        assertThat(configuration1.getMainSourceFile(), is(configuration0.getMainSourceFile()));
        assertThat(configuration1.getInitialArchitecture(), is(configuration0.getInitialArchitecture()));
        assertThat(configuration1.getFileFetcher(), is(configuration0.getFileFetcher()));
        assertThat(configuration1.getPredefinedSymbols(), is(newPredefinedSymbols));
        assertThat(configuration1.hasCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(false));
        assertThat(configuration1.getCustomConfigurationOptions(SAMPLE_CUSTOM_CONFIGURATION_OPTIONS_KEY), is(nullValue()));
    }

    /**
     * Asserts that {@link Configuration#setPredefinedSymbols(PredefinedSymbolTable)} throws a {@link NullPointerException} when the
     * <code>predefinedSymbols</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void setPredefinedSymbolsNull() {
        SAMPLE_CONFIGURATION.setPredefinedSymbols(null);
    }

    /**
     * Asserts that {@link Configuration#setPredefinedSymbols(PredefinedSymbolTable)} returns the original {@link Configuration}
     * when the specified predefined symbol table is the same as the original {@link Configuration}'s predefined symbol table.
     */
    @Test
    public void setPredefinedSymbolsSame() {
        final Configuration configuration0 = SAMPLE_CONFIGURATION;
        final Configuration configuration1 = configuration0.setPredefinedSymbols(configuration0.getPredefinedSymbols());
        assertThat(configuration1, is(sameInstance(configuration0)));
    }

}
