package io.github.nowipi.ffm.processor.annotations;

import java.lang.foreign.MemoryLayout;

/**
 * Used to map a method to a native function for libraries.
 * Map the native function's parameters and return type to the corresponding Java primitives or MemorySegment if it's a pointer or other datatype.
 * @see Library
 */
@LibraryComponent
public @interface Function {
    /**
     * Represents the name of the native function you are calling.
     * @return the function's native name
     */
    String value() default "";
}
