package org.reasm;

import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.source.SourceFile;

import com.google.common.collect.ImmutableMap;

/**
 * A configuration. Configurations describe how some source code is assembled. Different configurations can be created to assemble
 * the same source code differently.
 * <p>
 * This class is immutable.
 *
 * @author Francis Gagné
 */
@Immutable
public final class Configuration {

    @Nonnull
    private final Environment environment;
    @Nonnull
    private final SourceFile mainSourceFile;
    @Nonnull
    private final Architecture initialArchitecture;
    @CheckForNull
    private final FileFetcher fileFetcher;
    @Nonnull
    private final PredefinedSymbolTable predefinedSymbols;
    @Nonnull
    private final Map<Object, Object> customConfigurationOptions;

    /**
     * Initializes a new Configuration.
     *
     * @param environment
     *            the environment in which the source code will be assembled
     * @param mainSourceFile
     *            the main source file of the source code to assemble
     * @param initialArchitecture
     *            the initial architecture to set for this configuration
     */
    public Configuration(@Nonnull Environment environment, @Nonnull SourceFile mainSourceFile,
            @Nonnull Architecture initialArchitecture) {
        this(Objects.requireNonNull(environment, "environment"), Objects.requireNonNull(mainSourceFile, "mainSourceFile"), Objects
                .requireNonNull(initialArchitecture, "initialArchitecture"), null, PredefinedSymbolTable.EMPTY, ImmutableMap.of());
    }

    /**
     * Initializes a new Configuration.
     *
     * @param environment
     *            the environment in which the source code will be assembled
     * @param mainSourceFile
     *            the main source file of the source code to assemble
     * @param initialArchitecture
     *            the initial architecture to set for this configuration
     * @param fileFetcher
     *            an object that returns the contents of files (as a {@link SourceFile} object or as an array of bytes) for file
     *            names specified in directives that contain file name operands
     * @param predefinedSymbols
     *            a table of predefined symbols that will be defined in any assembly that is based on this configuration
     * @param customConfigurationOptions
     *            a {@link Map} of custom configuration options. The keys in this map are treated as opaque objects. The values in
     *            the map should be immutable. The Configuration will store a copy of the map.
     */
    private Configuration(@Nonnull Environment environment, @Nonnull SourceFile mainSourceFile,
            @Nonnull Architecture initialArchitecture, @CheckForNull FileFetcher fileFetcher,
            @Nonnull PredefinedSymbolTable predefinedSymbols, @Nonnull Map<Object, Object> customConfigurationOptions) {
        this.environment = environment;
        this.mainSourceFile = mainSourceFile;
        this.initialArchitecture = initialArchitecture;
        this.fileFetcher = fileFetcher;
        this.predefinedSymbols = predefinedSymbols;
        this.customConfigurationOptions = customConfigurationOptions;
    }

    /**
     * Gets the custom configuration options object associated with the specified key, or <code>null</code> if there is no such
     * object.
     *
     * @param key
     *            the key
     * @return the custom configuration options object, or <code>null</code> if there is no such object
     * @see #hasCustomConfigurationOptions(Object)
     */
    public final Object getCustomConfigurationOptions(Object key) {
        return this.customConfigurationOptions.get(key);
    }

    /**
     * Gets the environment in which the source code is assembled in this configuration.
     *
     * @return the environment
     */
    @Nonnull
    public final Environment getEnvironment() {
        return this.environment;
    }

    /**
     * Gets the file fetcher for this configuration.
     *
     * @return the file fetcher
     */
    @CheckForNull
    public final FileFetcher getFileFetcher() {
        return this.fileFetcher;
    }

    /**
     * Gets the initial architecture for this configuration.
     *
     * @return the initial architecture
     */
    @Nonnull
    public final Architecture getInitialArchitecture() {
        return this.initialArchitecture;
    }

    /**
     * Gets the main source file of this configuration.
     *
     * @return the main source file
     */
    @Nonnull
    public final SourceFile getMainSourceFile() {
        return this.mainSourceFile;
    }

    /**
     * Gets the predefined symbol table for this configuration.
     *
     * @return a {@link PredefinedSymbolTable} that contains the predefined symbols
     */
    @Nonnull
    public final PredefinedSymbolTable getPredefinedSymbols() {
        return this.predefinedSymbols;
    }

    /**
     * Determines whether there is a custom configuration options object associated with the specified key.
     *
     * @param key
     *            the key
     * @return <code>true</code> if there is a custom configuration options object associated with the specified key; otherwise,
     *         <code>false</code>
     * @see #getCustomConfigurationOptions(Object)
     */
    public final boolean hasCustomConfigurationOptions(Object key) {
        return this.customConfigurationOptions.containsKey(key);
    }

    /**
     * Creates a new configuration from this configuration with the specified custom configuration options.
     *
     * @param customConfigurationOptions
     *            a {@link Map} of custom configuration options. The keys in this map are treated as opaque objects. The values in
     *            the map should be immutable. The Configuration will store a copy of the map.
     * @return the new configuration
     */
    public final Configuration setCustomConfigurationOptions(@Nonnull Map<Object, Object> customConfigurationOptions) {
        if (customConfigurationOptions == null) {
            throw new NullPointerException("customConfigurationOptions");
        }

        return new Configuration(this.environment, this.mainSourceFile, this.initialArchitecture, this.fileFetcher,
                this.predefinedSymbols, ImmutableMap.copyOf(customConfigurationOptions));
    }

    /**
     * Creates a new configuration from this configuration with the specified environment.
     *
     * @param environment
     *            the environment in which the source code will be assembled
     * @return the new configuration
     */
    public final Configuration setEnvironment(@Nonnull Environment environment) {
        if (environment == null) {
            throw new NullPointerException("environment");
        }

        if (this.environment == environment) {
            return this;
        }

        return new Configuration(environment, this.mainSourceFile, this.initialArchitecture, this.fileFetcher,
                this.predefinedSymbols, this.customConfigurationOptions);
    }

    /**
     * Creates a new configuration from this configuration with the specified file fetcher.
     *
     * @param fileFetcher
     *            an object that returns the contents of files (as a {@link SourceFile} object or as an array of bytes) for file
     *            names specified in directives that contain file name operands
     * @return the new configuration
     */
    public final Configuration setFileFetcher(@CheckForNull FileFetcher fileFetcher) {
        if (this.fileFetcher == fileFetcher) {
            return this;
        }

        return new Configuration(this.environment, this.mainSourceFile, this.initialArchitecture, fileFetcher,
                this.predefinedSymbols, this.customConfigurationOptions);
    }

    /**
     * Creates a new configuration from this configuration with the specified main source file.
     *
     * @param mainSourceFile
     *            the main source file of the source code to assemble
     * @return the new configuration
     */
    public final Configuration setMainSourceFile(@Nonnull SourceFile mainSourceFile) {
        if (mainSourceFile == null) {
            throw new NullPointerException("mainSourceFile");
        }

        if (this.mainSourceFile == mainSourceFile) {
            return this;
        }

        return new Configuration(this.environment, mainSourceFile, this.initialArchitecture, this.fileFetcher,
                this.predefinedSymbols, this.customConfigurationOptions);
    }

    /**
     * Creates a new configuration from this configuration with the specified predefined symbols.
     *
     * @param predefinedSymbols
     *            a table of predefined symbols that will be defined in any assembly that is based on this configuration
     * @return the new configuration
     */
    public final Configuration setPredefinedSymbols(@Nonnull PredefinedSymbolTable predefinedSymbols) {
        if (predefinedSymbols == null) {
            throw new NullPointerException("predefinedSymbols");
        }

        if (this.predefinedSymbols == predefinedSymbols) {
            return this;
        }

        return new Configuration(this.environment, this.mainSourceFile, this.initialArchitecture, this.fileFetcher,
                predefinedSymbols, this.customConfigurationOptions);
    }

}
