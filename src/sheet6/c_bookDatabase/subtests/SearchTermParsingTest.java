package sheet6.c_bookDatabase.subtests;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.hamcrest.Matcher;
import org.junit.Test;

import test.Input;

/**
 * Asserts the program's ability to correctly parse a complex search term. It should output an error message for bad
 * search terms and execute correct ones. Not that the results of the {@code search} command are asserted in
 * {@link CompleteSearchScenarioTest}.
 * 
 * @author Joshua Gleitze
 * @version 1.0
 * @since 31.01.2015
 */
public class SearchTermParsingTest extends BookDatabaseSubTest {
    private static String query;

    /**
     * Fails the test as this test is incomplete an therefore does not grant anything. Remove this method as soon as the
     * test reaches a certain degree of relevance.
     */
    @Test
    public void incomplete() {
        fail("\n\nThis test is under development and incomplete!\n\n");
    }

    /**
     * Tests the progam's ability to detect between right terms. Asserts that:
     * <ul>
     * <li>The program executes valid {@code search} commands without printing an error message
     * </ul>
     * Note that correct results are only asserted in {@link CompleteSearchScenarioTest}
     */
    @Test
    public void testCorrectSearchTerms() {
        List<String> correctSearchTerms = new LinkedList<>();

        // simple search term
        line = "creator=abc";
        oneLineTest(search(line), not(startsWith("Error,")), "0.5", Input.getFile(simpleValidFile));
        correctSearchTerms.add(line);

        // simple AND
        line = "AND(creator=abc, year=32)";
        oneLineTest(search(line), not(startsWith("Error,")), "0.5", Input.getFile(simpleValidFile));
        correctSearchTerms.add(line);

        // simple OR
        line = "OR(title=abadc, year=32)";
        oneLineTest(search(line), not(startsWith("Error,")), "0.5", Input.getFile(simpleValidFile));
        correctSearchTerms.add(line);

        // AND with two ORs
        line = "AND(OR(title=Musterbuch, creator=Elke_Heidenreich), OR(creator=Mustermann, title=Am_Suedpol_denkt_man_ist_es_heiss))";
        oneLineTest(search(line), not(startsWith("Error,")), "0.5", Input.getFile(simpleValidFile));
        correctSearchTerms.add(line);

        // OR with two ANDs
        line = "OR(AND(title=Musterbuch, creator=Mustermann), AND(creator=Elke_Heidenreich, title=Am_Suedpol_denkt_man_ist_es_heiss))";
        oneLineTest(search(line), not(startsWith("Error,")), "0.5", Input.getFile(simpleValidFile));
        correctSearchTerms.add(line);

        // more complex
        line = "OR(AND(title=Musterbuch, creator=Mustermann), AND(creator=Elke_Heidenreich, title=Am_Suedpol_denkt_man_ist_es_heiss))";
        oneLineTest(search(line), not(startsWith("Error,")), "0.5", Input.getFile(simpleValidFile));
        correctSearchTerms.add(line);

        // quite complex
        String a, b, c, d, e, f, g, h, i, j, k;
        a = "OR(year=0,year=52)";
        b = "AND(" + a + ",creator=Mama)";
        c = "AND(title=____d____,creator=-_-_-_-_)";
        d = "OR(creator=a,creator=a)";
        e = "AND(" + c + "," + d + ")";
        f = "OR(" + b + ",year=2015)";
        g = "OR(" + f + "," + e + ")";
        h = "AND(" + a + "," + a + ")";
        i = "OR(" + h + "," + c + ")";
        j = "OR(" + e + ",year=123)";
        k = "AND(" + j + "," + i + ")";
        line = "AND(" + g + "," + k + ")";
        oneLineTest(search(line), not(startsWith("Error,")), "0.5", Input.getFile(simpleValidFile));
        correctSearchTerms.add(line);

        // test the same lines but with random case and random spaces
        for (String term : correctSearchTerms) {
            oneLineTest(search(shuffle(term)), not(startsWith("Error,")), "0.5", Input.getFile(simpleValidFile));
        }

        // test multiple search requests
        int numberOfRuns = 7;
        int n = -1;
        Random random = new Random();
        while (n++ < numberOfRuns) {
            // at least 3, maximal correctSearchTerms.size() * 2 + 2 searches
            int numberOfSearches = random.nextInt(correctSearchTerms.size() * 2) + 3;
            expectedResultMatchers = new LinkedList<Matcher<String>>();
            int m = -1;
            while (++m < numberOfSearches) {
                expectedResultMatchers.add(not(startsWith("Error,")));
            }
            commands = new String[numberOfSearches + 1];
            m = -1;
            while (++m < numberOfSearches) {
                int termIndex = random.nextInt(correctSearchTerms.size());
                commands[m] = "search " + shuffle(correctSearchTerms.get(termIndex));
            }
            commands[numberOfSearches] = "quit";
            multiLineTest(commands, expectedResultMatchers, "0.5", Input.getFile(simpleValidFile));
        }
    }

    /**
     * Tests whether malformed search queries lead to error-messages.
     */
    @Test
    public void testIllegalSearchTerms() {

        // No arg
        command = "search";
        errorTest(addQuit(command), "0.3", Input.getFile(simpleValidFile));

        // Empty arg
        command = "search ";
        errorTest(addQuit(command), "0.3", Input.getFile(simpleValidFile));

        // No "="-sign
        query = "creator";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Two == signs
        query = "creator=v1=v2";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal Attribute name
        query = "crtor=test";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal Attribute name
        query = "creator?=test";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // No Attribute name
        query = "=test";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // No attribute value
        query = "creator=";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal Attribute value
        query = "creator=t?st";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "OR(title=abadc, year=32))";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "OR(title=abadc, year=32";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "ORtitle=abadc, year=32)";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "OR((title=abadc, year=32)";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "OR(title=abadc)";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "OR((title=abadc, year=32))";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "OR(title=abadc, AND(title=Musterbuch, creator=Mustermann)";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "OR(title=abadc, title=Musterbuch, creator=Mustermann)";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Same as above with and instead of or
        // Illegal brackets
        query = "AND(title=abadc, year=32))";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "AND(title=abadc, year=32";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "ANDtitle=abadc, year=32)";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "AND((title=abadc, year=32)";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "AND(title=abadc)";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "AND((title=abadc, year=32))";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "AND(title=abadc, OR(title=Musterbuch, creator=Mustermann)";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal brackets
        query = "AND(title=abadc, title=Musterbuch, creator=Mustermann)";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));

        // Illegal prefix
        query = "AN(title=abadc, year=32)";
        errorTest(search(query), "0.3", Input.getFile(simpleValidFile));
    }

    private String shuffle(String line) {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (char c : line.toCharArray()) {
            while (random.nextInt(2) != 0) {
                builder.append(" ");
            }
            builder.append((random.nextInt(2) != 0) ? Character.toUpperCase(c) : Character.toLowerCase(c));
        }
        while (random.nextInt(2) != 0) {
            builder.append(" ");
        }
        return builder.toString();
    }

    private String[] search(String line) {
        return new String[] {
                "search " + line,
                "quit"
        };
    }
}
