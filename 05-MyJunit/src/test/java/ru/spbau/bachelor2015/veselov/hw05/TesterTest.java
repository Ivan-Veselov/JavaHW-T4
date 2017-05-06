package ru.spbau.bachelor2015.veselov.hw05;

import com.google.common.collect.Sets;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import ru.spbau.bachelor2015.veselov.hw05.examples.*;
import ru.spbau.bachelor2015.veselov.hw05.examples.invalid.*;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.*;
import ru.spbau.bachelor2015.veselov.hw05.reports.FailureReport;
import ru.spbau.bachelor2015.veselov.hw05.reports.IgnoreReport;
import ru.spbau.bachelor2015.veselov.hw05.reports.PassReport;
import ru.spbau.bachelor2015.veselov.hw05.reports.TestReport;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class TesterTest {
    @Test(expected = InvalidTestClassException.class)
    public void testPrivateConstructor() throws Exception {
        testClass(PrivateConstructor.class, Collections.emptySet());
    }

    @Test(expected = InvalidTestClassException.class)
    public void testTestWithArguments() throws Exception {
        testClass(TestWithArgument.class, Collections.emptySet());
    }

    @Test
    public void testOnePassingTest() throws Exception {
        testClass(OnePassingTest.class, Collections.singleton(passReport()));
    }

    @Test
    public void testOneFailingTest() throws Exception {
        testClass(OneFailingTest.class, Collections.singleton(failureReport(Exception.class)));
    }

    @Test
    public void testClassWithSimple() throws Exception {
        testClass(ClassWithSimpleTests.class, Sets.newHashSet(passReport(),
                                                              passReport(),
                                                              failureReport(Exception.class),
                                                              failureReport(Exception.class)));
    }

    @Test
    public void testFailingBefore() throws Exception {
        testClass(FailingBefore.class, Collections.singleton(failureReport(Exception.class)));
    }

    @Test(expected = InvalidTestClassException.class)
    public void testBeforeWithArgument() throws Exception {
        testClass(BeforeWithArgument.class, Collections.emptySet());
    }

    @Test
    public void testFailingAfter() throws Exception {
        testClass(FailingAfter.class, Collections.singleton(failureReport(Exception.class)));
    }

    @Test(expected = InvalidTestClassException.class)
    public void testAfterWithArgument() throws Exception {
        testClass(AfterWithArgument.class, Collections.emptySet());
    }

    @Test
    public void testBeforeAfterCombination() throws Exception {
        testClass(BeforeAfterCombination.class, Sets.newHashSet(passReport(),
                                                                     failureReport(Exception1.class),
                                                                     failureReport(Exception2.class)));
    }

    @Test(expected = BeforeClassStageFailedException.class)
    public void testFailingBeforeClass() throws Exception {
        testClass(FailingBeforeClass.class, Collections.emptySet());
    }

    @Test(expected = InvalidTestClassException.class)
    public void testNonStaticBeforeClass() throws Exception {
        testClass(NonStaticBeforeClass.class, Collections.emptySet());
    }

    @Test(expected = AfterClassStageFailedException.class)
    public void testFailingAfterClass() throws Exception {
        testClass(FailingAfterClass.class, Collections.emptySet());
    }

    @Test(expected = InvalidTestClassException.class)
    public void testNonStaticAfterClass() throws Exception {
        testClass(NonStaticAfterClass.class, Collections.emptySet());
    }

    @Test
    public void testTestsWithExpected() throws Exception {
        testClass(TestsWithExpected.class, Sets.newHashSet(failureReport(Exception2.class),
                                                           passReport()));
    }

    @Test
    public void testIgnoredTests() throws Exception {
        testClass(IgnoredTests.class, Sets.newHashSet(ignoreReport("Reason 1"),
                                                      ignoreReport("Reason 2")));
    }

    /**
     * Such strange collection matchers type because of issue.
     *
     * https://github.com/hamcrest/JavaHamcrest/issues/156
     */
    private void testClass(final @NotNull Class<?> clazz,
                           final @NotNull Collection<Matcher<? super TestReport>> matchers) throws Exception {
        List<TestReport> report = new Tester(clazz).test();
        assertThat(report, containsInAnyOrder(matchers));
    }

    private @NotNull PassReportMatcher passReport() {
        return new PassReportMatcher();
    }

    private @NotNull FailureReportMatcher failureReport(final @NotNull Class<?> expectedCauseType) {
        return new FailureReportMatcher(expectedCauseType);
    }

    private @NotNull IgnoreReportMatcher ignoreReport(final @NotNull String expectedReason) {
        return new IgnoreReportMatcher(expectedReason);
    }

    private abstract class TestReportMatcher extends BaseMatcher<TestReport> {}

    private final class PassReportMatcher extends TestReportMatcher {
        @Override
        public boolean matches(final @NotNull Object item) {
            return item instanceof PassReport;
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

    private final class IgnoreReportMatcher extends TestReportMatcher {
        private final @NotNull String expectedReason;

        public IgnoreReportMatcher(final @NotNull String expectedReason) {
            this.expectedReason = expectedReason;
        }

        @Override
        public boolean matches(final @NotNull Object item) {
            if (!(item instanceof IgnoreReport)) {
                return false;
            }

            IgnoreReport actual = (IgnoreReport) item;
            return expectedReason.equals(actual.getReason());
        }

        @Override
        public void describeTo(final @NotNull Description description) {
            description.appendText("Ignore report with reason ").appendText(expectedReason);
        }
    }
}
