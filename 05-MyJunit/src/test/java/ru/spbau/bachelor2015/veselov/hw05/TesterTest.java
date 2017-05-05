package ru.spbau.bachelor2015.veselov.hw05;

import com.google.common.collect.Sets;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import ru.spbau.bachelor2015.veselov.hw05.examples.*;
import ru.spbau.bachelor2015.veselov.hw05.examples.invalid.AfterWithArgumentClass;
import ru.spbau.bachelor2015.veselov.hw05.examples.invalid.BeforeWithArgumentClass;
import ru.spbau.bachelor2015.veselov.hw05.examples.invalid.PrivateConstructorClass;
import ru.spbau.bachelor2015.veselov.hw05.examples.invalid.TestWithArgumentClass;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.Exception1;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.Exception2;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.InvalidTestClassException;
import ru.spbau.bachelor2015.veselov.hw05.reports.FailureReport;
import ru.spbau.bachelor2015.veselov.hw05.reports.PassReport;
import ru.spbau.bachelor2015.veselov.hw05.reports.TestReport;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class TesterTest {
    @Test(expected = InvalidTestClassException.class)
    public void testPrivateConstructorClass() throws Exception {
        testClass(PrivateConstructorClass.class, Collections.emptySet());
    }

    @Test(expected = InvalidTestClassException.class)
    public void testTestWithArgumentsClass() throws Exception {
        testClass(TestWithArgumentClass.class, Collections.emptySet());
    }

    @Test
    public void testOnePassingTestClass() throws Exception {
        testClass(OnePassingTestClass.class, Collections.singleton(passReport()));
    }

    @Test
    public void testOneFailingTestClass() throws Exception {
        testClass(OneFailingTestClass.class, Collections.singleton(failureReport(Exception.class)));
    }

    @Test
    public void testClassWithSimpleTests() throws Exception {
        testClass(ClassWithSimpleTests.class, Sets.newHashSet(passReport(),
                                                              passReport(),
                                                              failureReport(Exception.class),
                                                              failureReport(Exception.class)));
    }

    @Test
    public void testFailingBeforeClass() throws Exception {
        testClass(FailingBeforeClass.class, Collections.singleton(failureReport(Exception.class)));
    }

    @Test(expected = InvalidTestClassException.class)
    public void testBeforeWithArgumentClass() throws Exception {
        testClass(BeforeWithArgumentClass.class, Collections.emptySet());
    }

    @Test
    public void testFailingAfterClass() throws Exception {
        testClass(FailingAfterClass.class, Collections.singleton(failureReport(Exception.class)));
    }

    @Test(expected = InvalidTestClassException.class)
    public void testAfterWithArgumentClass() throws Exception {
        testClass(AfterWithArgumentClass.class, Collections.emptySet());
    }

    @Test
    public void testBeforeAfterCombinationClass() throws Exception {
        testClass(BeforeAfterCombinationClass.class, Sets.newHashSet(passReport(),
                                                                     failureReport(Exception1.class),
                                                                     failureReport(Exception2.class)));
    }

    /**
     * Such strange collection matchers type because of issue.
     *
     * https://github.com/hamcrest/JavaHamcrest/issues/156
     */
    private void testClass(final @NotNull Class<?> clazz,
                           final @NotNull Collection<Matcher<? super TestReport>> matchers)
            throws InvalidTestClassException {
        List<TestReport> report = new Tester(clazz).test();
        assertThat(report, containsInAnyOrder(matchers));
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
