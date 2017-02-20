package hudson.plugins.warnings.parser;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import hudson.plugins.analysis.util.NullLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link WarningsFilter}.
 *
 * @author StKlug
 */
public class WarningsFilterTest {
    private WarningsFilter filter;

    /**
     * Tests the exclusion of certain warning messages from the report.
     */
    @Test
    public void testMessagesPatternNonRegex() {
        assertArg1MatchesArg2DoesntMatchPattern("Javadoc: Missing tag for parameter arg1",
                "The import java.io.OutputStream is never used", "Javadoc: Missing tag for parameter arg1");
    }

    @Test
    public void testMessagesPatternWithRegex() {
        assertArg1MatchesArg2DoesntMatchPattern("Javadoc: Missing tag for parameter arg1",
                "The import java.io.OutputStream is never used", ".*1");
    }

    @Test
    public void testMultilineMessageMatchesRegex(){
        assertArg1MatchesArg2DoesntMatchPattern("This multiline expression\nShould match 1 present",
                "This multiline expression\r\nShould not match", ".*1.*");
        assertArg1MatchesArg2DoesntMatchPattern("This multiline expression\r\nShould match 1 present",
                "This multiline expression\r\nShould not match", ".*1.*");
    }

    @Test
    public void testMessagesPatternWithRegexMatchesJavaRegexSyntax() {
        assertArg1MatchesArg2DoesntMatchPattern("Javadoc: Missing tag for parameter arg1",
                "The import java.io.OutputStream is never used", ".*1.*");
    }

    @Test
    public void testDirectoryExclusionRegexWithAntPathSyntax() {
        Warning w1 = createDummyWarning("", "path/directory1");
        Warning w2 = createDummyWarning("", "path/directory2");
        Collection<FileAnnotation> warnings = buildWarningsCollection(w1, w2);
        String excludeDirectoryPattern = "**/directory1";
        warnings = filter.apply(warnings, null, excludeDirectoryPattern, null, new NullLogger());

        assertFalse(warnings.contains(w1));
        assertTrue(warnings.contains(w2));
    }

    @Test
    public void testDirectoryInclusionRegexWithAntPathSyntax() {
        Warning w1 = createDummyWarning("", "path/directory1");
        Warning w2 = createDummyWarning("", "path/directory2");
        Collection<FileAnnotation> warnings = buildWarningsCollection(w1, w2);
        String includeDirectoryPattern = "**/directory1";
        warnings = filter.apply(warnings, includeDirectoryPattern, null, null, new NullLogger());

        assertTrue(warnings.contains(w1));
        assertFalse(warnings.contains(w2));
    }

    private void assertArg1MatchesArg2DoesntMatchPattern(String matchesExclusionPattern, String nonMatch, String excludePattern) {
        Warning w1 = createDummyWarning(matchesExclusionPattern);
        Warning w2 = createDummyWarning(nonMatch);
        Collection<FileAnnotation> warnings = buildWarningsCollection(w1, w2);

        warnings = filter.apply(warnings, null, null, excludePattern, new NullLogger());

        assertFalse("Pattern "+excludePattern+" should have matched string:"+matchesExclusionPattern, warnings.contains(w1));
        assertTrue("Pattern "+excludePattern+" expected to not match string:"+nonMatch, warnings.contains(w2));
    }

    @Before
    public void setup(){
        this.filter = new WarningsFilter();
    }

    private Warning createDummyWarning(final String message) {
        return createDummyWarning(message, "dummyFile.java");
    }

    private Warning createDummyWarning(final String message, String fileName) {
        return new Warning(fileName, 0, "warningType", "warningCategory", message, Priority.LOW);
    }

    private Collection<FileAnnotation> buildWarningsCollection(Warning... warnings){
        Collection<FileAnnotation> warningCollection = new LinkedList<FileAnnotation>();
        for (Warning warning: warnings){
            warningCollection.add(warning);
        }
        return warningCollection;
    }
}

