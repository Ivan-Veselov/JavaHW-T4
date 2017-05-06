package ru.spbau.bachelor2015.veselov.hw05.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods annotated with this annotation are considered as test-cases.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Test {
    /**
     * Special string constant String value for ignore parameter. This value denotes that test will not be ignored.
     */
    String noIgnoranceDescription = "";

    /**
     * This parameter denotes expected exception which test-case should throw. Default value is a special class which
     * denotes no exception.
     */
    Class expected() default None.class;

    /**
     * This parameter is a reason which describes why particular test-case should be ignored. If reason is a special
     * string then test-case will not be ignored.
     */
    String ignore() default noIgnoranceDescription;

    /**
     * Special class which denotes no expected exception.
     */
    class None {}
}
