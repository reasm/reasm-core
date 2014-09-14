package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.reasm.source.SourceFile;
import org.reasm.source.SourceLocation;
import org.reasm.testhelpers.HexArchitecture;

import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link AssemblyStepLocation}.
 *
 * @author Francis Gagné
 */
public class AssemblyStepLocationTest extends ObjectHashCodeEqualsContract {

    private static final SourceFile SOURCE_FILE = new SourceFile("0001FC", "test.hex");
    private static final List<SourceLocation> SOURCE_LOCATIONS = SOURCE_FILE.getSourceLocations(HexArchitecture.INSTANCE);
    private static final SourceLocation ROOT_SOURCE_LOCATION = SOURCE_LOCATIONS.get(0);
    private static final SourceLocation CHILD_SOURCE_LOCATION_0 = ROOT_SOURCE_LOCATION.getChildSourceLocations().get(0);
    private static final SourceLocation CHILD_SOURCE_LOCATION_1 = ROOT_SOURCE_LOCATION.getChildSourceLocations().get(1);

    private static final AssemblyStepLocation ROOT_LOCATION = new AssemblyStepLocation(ROOT_SOURCE_LOCATION, 0, null, false);
    private static final AssemblyStepLocation MAIN_OBJECT = new AssemblyStepLocation(CHILD_SOURCE_LOCATION_0, 0, ROOT_LOCATION,
            true);
    private static final AssemblyStepLocation OTHER_EQUAL_OBJECT = new AssemblyStepLocation(CHILD_SOURCE_LOCATION_0, 0,
            ROOT_LOCATION, true);
    private static final AssemblyStepLocation ANOTHER_EQUAL_OBJECT = new AssemblyStepLocation(CHILD_SOURCE_LOCATION_0, 0,
            ROOT_LOCATION, true);

    private static final AssemblyStepLocation DIFFERENT_OBJECT_1 = new AssemblyStepLocation(CHILD_SOURCE_LOCATION_1, 0,
            ROOT_LOCATION, true);
    private static final AssemblyStepLocation DIFFERENT_OBJECT_2 = new AssemblyStepLocation(CHILD_SOURCE_LOCATION_0, 1,
            ROOT_LOCATION, true);
    private static final AssemblyStepLocation DIFFERENT_OBJECT_3 = new AssemblyStepLocation(CHILD_SOURCE_LOCATION_0, 0, null, true);
    private static final AssemblyStepLocation DIFFERENT_OBJECT_4 = new AssemblyStepLocation(CHILD_SOURCE_LOCATION_0, 0,
            ROOT_LOCATION, false);

    /**
     * Initializes a new AssemblyStepLocationTest.
     */
    public AssemblyStepLocationTest() {
        super(MAIN_OBJECT, OTHER_EQUAL_OBJECT, ANOTHER_EQUAL_OBJECT, DIFFERENT_OBJECT_1, DIFFERENT_OBJECT_2, DIFFERENT_OBJECT_3,
                DIFFERENT_OBJECT_4, new Object());
    }

    /**
     * Asserts that {@link AssemblyStepLocation#getFullPath()} returns two path components for a location with a non-transparent
     * parent.
     */
    @Test
    public void getFullPathChild() {
        assertThat(new AssemblyStepLocation(CHILD_SOURCE_LOCATION_1, 0, ROOT_LOCATION, false).getFullPath(),
                is("test.hex(1,1):test.hex(1,3)"));
    }

    /**
     * Asserts that {@link AssemblyStepLocation#getFullPath()} returns a single path component for a location with a transparent
     * parent.
     */
    @Test
    public void getFullPathChildTransparentParent() {
        assertThat(new AssemblyStepLocation(CHILD_SOURCE_LOCATION_1, 0, ROOT_LOCATION, true).getFullPath(), is("test.hex(1,3)"));
    }

    /**
     * Asserts that {@link AssemblyStepLocation#getFullPath()} shows the iteration number on the proper path component when the
     * iteration number is not zero.
     */
    @Test
    public void getFullPathIterationNumber() {
        assertThat(new AssemblyStepLocation(CHILD_SOURCE_LOCATION_1, 2, ROOT_LOCATION, false).getFullPath(),
                is("test.hex(1,1):test.hex(1,3)[2]"));
    }

    /**
     * Asserts that {@link AssemblyStepLocation#getFullPath()} returns a single path component for a location with no parent.
     */
    @Test
    public void getFullPathRoot() {
        assertThat(ROOT_LOCATION.getFullPath(), is("test.hex(1,1)"));
    }

    /**
     * Asserts that {@link AssemblyStepLocation#getFullPath()} ignores the fact that a root location specifies that it has a
     * transparent parent.
     */
    @Test
    public void getFullPathRootTransparentParent() {
        assertThat(new AssemblyStepLocation(ROOT_SOURCE_LOCATION, 0, null, true).getFullPath(), is("test.hex(1,1)"));
    }

}
