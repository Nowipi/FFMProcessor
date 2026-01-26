package io.github.nowipi.ffm.processor.annotations;

import java.lang.annotation.*;

/**
 * Used to tell the annotation processor that this parameter or return type is passed-by-value.
 * @see Function
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Value { }
