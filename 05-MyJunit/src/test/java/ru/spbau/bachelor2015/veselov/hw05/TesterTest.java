package ru.spbau.bachelor2015.veselov.hw05;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import ru.spbau.bachelor2015.veselov.hw05.examples.*;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.Exception1;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.Exception2;
import ru.spbau.bachelor2015.veselov.hw05.reports.FailureReport;
import ru.spbau.bachelor2015.veselov.hw05.reports.PassReport;
import ru.spbau.bachelor2015.veselov.hw05.reports.TestReport;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

// TODO add tests with invalid testing classes
// TODO generify testing
public class TesterTest {
    @Test
    public void testOnePassingTestClass() throws Exception {
        List<TestReport> report = new Tester(OnePassingTestClass.class).test();

        assertThat(report, contains(passReport()));
    }

    @Test
    public void testOneFailingTestClass() throws Exception {
        List<TestReport> report = new Tester(OneFailingTestClass.class).test();

        assertThat(report, contains(failureReport(Exception.class)));
    }

    @Test
    public void testClassWithSimpleTests() throws Exception {
        List<TestReport> report = new Tester(ClassWithSimpleTests.class).test();

        assertThat(report, containsInAnyOrder(passReport(),
                                              passReport(),
                                              failureReport(Exception.class),
                                              failureReport(Exception.class)));
    }

    @Test
    public void testFailingBeforeClass() throws Exception {
        List<TestReport> report = new Tester(FailingBeforeClass.class).test();

        assertThat(report, contains(failureReport(Exception.class)));
    }

    @Test
    public void testFailingAfterClass() throws Exception {
        List<TestReport> report = new Tester(FailingAfterClass.class).test();

        assertThat(report, contains(failureReport(Exception.class)));
    }

    @Test
    public void testBeforeAfterCombinationClass() throws Exception {
        List<TestReport> report = new Tester(BeforeAfterCombinationClass.class).test();

        assertThat(report, containsInAnyOrder(passReport(),
                                              failureReport(Exception1.class),
                                              failureReport(Exception2.class)));
    }

    private @NotNull PassReportMatcher passReport() {
        return new PassReportMatcher();
    }

    private @NotNull FailureReportMatcher failureReport(final @NotNull Class<?> expectedCauseType) {
        return new FailureReportMatcher(expectedCauseType);
    }

    private abstract class TestReportMatcher extends BaseMatcher<TestReport> {}

    private final class PassReportMatcher extends TestReportMatcher {
        @Override
        public boolean matches(final @NotNull Object item) {
            if (!(item instanceof PassReport)) {
                return false;
            }

            return true;
        }

        @Override
        public void describeTo(final @NotNull Description description) {
            description.appendText("Pass report");
        }
    }

    private final class FailureReportMatcher extends TestReportMatcher {
        private final @NotNull Class<?> expectedCauseClass;

        public FailureReportMatcher(final @NotNull Class<?> expectedCauseClass) {
            this.expectedCauseClass = expectedCauseClass;
        }

        @Override
        public boolean matches(final @NotNull Object item) {
            if (!(item instanceof FailureReport)) {
                return false;
            }

            FailureReport actual = (FailureReport) item;
            return expectedCauseClass.isInstance(actual.getCause());
        }

        @Override
        public void describeTo(final @NotNull Description description) {
            description.appendText("Failure report caused by ").appendValue(expectedCauseClass);
        }
    }
}
