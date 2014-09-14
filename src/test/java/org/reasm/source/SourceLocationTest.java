package org.reasm.source;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.reasm.Architecture;
import org.reasm.SubstringBounds;
import org.reasm.testhelpers.NullArchitecture;

import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link SourceLocation}.
 *
 * @author Francis Gagné
 */
public class SourceLocationTest extends ObjectHashCodeEqualsContract {

    private static final SourceFile SOURCE_FILE = new SourceFile("abcde\nfghijklm", null);
    private static final Architecture NULL_ARCHITECTURE = NullArchitecture.DEFAULT;
    private static final CompositeSourceNode ROOT_NODE = (CompositeSourceNode) NULL_ARCHITECTURE.parse(SOURCE_FILE.getText());
    private static final SourceNode SOURCE_NODE = ROOT_NODE.getChildNodes().get(1);
    private static final SourceLocation SOURCE_LOCATION = new SourceLocation(SOURCE_FILE, NULL_ARCHITECTURE, SOURCE_NODE, 6, 2, 1);

    private static final SourceLocation EQUAL_SOURCE_LOCATION_1 = new SourceLocation(SOURCE_FILE, NULL_ARCHITECTURE, SOURCE_NODE,
            6, 2, 1);
    private static final SourceLocation EQUAL_SOURCE_LOCATION_2 = new SourceLocation(SOURCE_FILE, NULL_ARCHITECTURE, SOURCE_NODE,
            6, 2, 1);

    // Note: most of these SourceLocation objects aren't valid. They just help get more code coverage.
    private static final SourceLocation DIFFERENT_SOURCE_LOCATION_1 = new SourceLocation(new SourceFile("abcde\nfghijk", null),
            NULL_ARCHITECTURE, SOURCE_NODE, 6, 2, 1);
    private static final SourceLocation DIFFERENT_SOURCE_LOCATION_2 = new SourceLocation(SOURCE_FILE, new NullArchitecture(),
            SOURCE_NODE, 6, 2, 1);
    private static final SourceLocation DIFFERENT_SOURCE_LOCATION_3 = new SourceLocation(SOURCE_FILE, NULL_ARCHITECTURE, ROOT_NODE
            .getChildNodes().get(0), 6, 2, 1);
    private static final SourceLocation DIFFERENT_SOURCE_LOCATION_4 = new SourceLocation(SOURCE_FILE, NULL_ARCHITECTURE,
            SOURCE_NODE, 7, 2, 1);
    private static final SourceLocation DIFFERENT_SOURCE_LOCATION_5 = new SourceLocation(SOURCE_FILE, NULL_ARCHITECTURE,
            SOURCE_NODE, 6, 1, 1);
    private static final SourceLocation DIFFERENT_SOURCE_LOCATION_6 = new SourceLocation(SOURCE_FILE, NULL_ARCHITECTURE,
            SOURCE_NODE, 6, 2, 2);

    /**
     * Initializes a new SourceLocationTest.
     */
    public SourceLocationTest() {
        super(SOURCE_LOCATION, EQUAL_SOURCE_LOCATION_1, EQUAL_SOURCE_LOCATION_2, DIFFERENT_SOURCE_LOCATION_1,
                DIFFERENT_SOURCE_LOCATION_2, DIFFERENT_SOURCE_LOCATION_3, DIFFERENT_SOURCE_LOCATION_4, DIFFERENT_SOURCE_LOCATION_5,
                DIFFERENT_SOURCE_LOCATION_6, new Object());
    }

    /**
     * Asserts that {@link SourceLocation#getChildSourceLocations()} returns the correct source locations.
     */
    @Test
    public void getChildSourceLocations() {
        final SourceLocation sourceLocation = new SourceLocation(SOURCE_FILE, NULL_ARCHITECTURE, ROOT_NODE, 0, 1, 1);
        final List<SourceLocation> childSourceLocations = sourceLocation.getChildSourceLocations();

        assertThat(childSourceLocations.size(), is(2));

        SourceLocation childSourceLocation = childSourceLocations.get(0);
        assertThat(childSourceLocation.getFile(), is(sameInstance((Object) SOURCE_FILE)));
        assertThat(childSourceLocation.getArchitecture(), is(sameInstance(NULL_ARCHITECTURE)));
        assertThat(childSourceLocation.getSourceNode(), is(sameInstance(ROOT_NODE.getChildNodes().get(0))));
        assertThat(childSourceLocation.getTextPosition(), is(0));
        assertThat(childSourceLocation.getLineNumber(), is(1));
        assertThat(childSourceLocation.getLinePosition(), is(1));

        childSourceLocation = childSourceLocations.get(1);
        assertThat(childSourceLocation.getFile(), is(sameInstance((Object) SOURCE_FILE)));
        assertThat(childSourceLocation.getArchitecture(), is(sameInstance(NULL_ARCHITECTURE)));
        assertThat(childSourceLocation.getSourceNode(), is(sameInstance(SOURCE_NODE)));
        assertThat(childSourceLocation.getTextPosition(), is(6));
        assertThat(childSourceLocation.getLineNumber(), is(2));
        assertThat(childSourceLocation.getLinePosition(), is(1));
    }

    /**
     * Asserts that {@link SourceLocation#getChildSourceLocations()} throws a {@link ClassCastException} when the source location's
     * source node is not a {@link CompositeSourceNode}.
     */
    @Test(expected = ClassCastException.class)
    public void getChildSourceLocationsNotComposite() {
        SOURCE_LOCATION.getChildSourceLocations();
    }

    /**
     * Asserts that {@link SourceLocation#getText()} returns a {@link CharSequence} that contains the text at the source location.
     */
    @Test
    public void getText() {
        assertThat(SOURCE_LOCATION.getText().toString(), is("fghijklm"));
    }

    /**
     * Asserts that {@link SourceLocation#getTextReader()} returns a {@link SourceNodeRangeReader} that reads the text at the source
     * location, one character at a time.
     */
    @Test
    public void getTextReader() {
        final String text = "fghijklm";
        final SourceNodeRangeReader reader = SOURCE_LOCATION.getTextReader();

        for (int i = 0; i < text.length(); i++) {
            assertThat(reader.getCurrentChar(), is(text.charAt(i)));
            reader.advance();
        }

        assertThat(reader.atEnd(), is(true));
    }

    /**
     * Asserts that {@link SourceLocation#getTextSubstring(int, int)} returns a substring of the text at the source location.
     */
    @Test
    public void getTextSubstringIntInt() {
        assertThat(SOURCE_LOCATION.getTextSubstring(2, 5), is("hij"));
    }

    /**
     * Asserts that {@link SourceLocation#getTextSubstring(SubstringBounds)} returns a substring of the text at the source location.
     */
    @Test
    public void getTextSubstringSubstringBounds() {
        assertThat(SOURCE_LOCATION.getTextSubstring(new SubstringBounds(2, 5)), is("hij"));
    }

    /**
     * Asserts that {@link SourceLocation#SourceLocation(AbstractSourceFile, Architecture, SourceNode, int, int, int)} correctly
     * initializes a {@link SourceLocation}.
     */
    @Test
    public void sourceLocation() {
        assertThat(SOURCE_LOCATION.getFile(), is(sameInstance((Object) SOURCE_FILE)));
        assertThat(SOURCE_LOCATION.getArchitecture(), is(sameInstance(NULL_ARCHITECTURE)));
        assertThat(SOURCE_LOCATION.getSourceNode(), is(sameInstance(SOURCE_NODE)));
        assertThat(SOURCE_LOCATION.getTextPosition(), is(6));
        assertThat(SOURCE_LOCATION.getLineNumber(), is(2));
        assertThat(SOURCE_LOCATION.getLinePosition(), is(1));
    }

}
