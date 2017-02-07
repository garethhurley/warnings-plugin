package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

import hudson.plugins.warnings.parser.jcreport.File;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Tests the class {@link MavenConsoleParser}.
 *
 * @author Ulli Hafner
 */
public class MavenConsoleParserTest extends ParserTester {
    /**
     * Verifies that errors and warnings are correctly picked up.
     *
     * @throws IOException
     *             if the file could not be read
     */
    @Test
    public void testParsing() throws IOException {
        Collection<FileAnnotation> warnings = new MavenConsoleParser().parse(openFile());

        assertEquals("Wrong number of warnings detected.", 4, warnings.size());
        AnnotationContainer result = new DefaultAnnotationContainer(warnings);
        assertEquals("Wrong number of errors detected.", 2, result.getNumberOfAnnotations(Priority.HIGH));
        assertEquals("Wrong number of warnings detected.", 2, result.getNumberOfAnnotations(Priority.NORMAL));
    }

    @Test
    public void a_warning_line_with_a_specific_message_is_treated_as_a_new_warning() throws Exception {
        String messages = buildWarningString("warning1","Some problems were encountered while SOMETHING-2","warning3");
        Collection<FileAnnotation> warnings = new MavenConsoleParser().parse(new StringReader(messages));
        assertEquals(2, warnings.size());
        Iterator<FileAnnotation> parsed_warnings_iterator = warnings.iterator();
        parsed_warnings_iterator.next();
        assertEquals("Some problems were encountered while SOMETHING-2\nwarning3", parsed_warnings_iterator.next().getMessage());
    }

    @Test
    public void two_consecutive_warning_lines_are_treated_as_part_of_the_same_warning_by_default() throws Exception {
        String messages = buildWarningString("warning1","warning2");
        Collection<FileAnnotation> warnings = new MavenConsoleParser().parse(new StringReader(messages));
        assertEquals(1, warnings.size());
    }

    private String buildWarningString(String... warningLines) {
        String eol = "\r\n";
        String warning = "[WARNING] ";
        StringBuilder warningString = new StringBuilder();
        for(String line: warningLines){
            warningString.append(warning).append(line).append(eol);
        }
        return warningString.toString();
    }

    /**
     * Parses a file with three warnings, two of them will be ignored beacuse they are blank.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-16826">Issue 16826</a>
     */
    @Test
    public void issue16826() throws IOException {
        Collection<FileAnnotation> warnings = new MavenConsoleParser().parse(openFile("issue16826.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
    }

    /**
     * Parses a file with three warnings, two of them will be ignored beacuse they are blank.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-25278">Issue 25278</a>
     */
    @Test @Ignore("Until JENKINS-25278 is fixed")
    public void largeFile() throws IOException {
        Collection<FileAnnotation> warnings = new MavenConsoleParser().parse(openFile("maven-large.log"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());
    }

    @Override
    protected String getWarningsFile() {
        return "maven-console.txt";
    }
}

