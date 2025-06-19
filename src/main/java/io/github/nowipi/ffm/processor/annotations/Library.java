package io.github.nowipi.ffm.processor.annotations;

import java.lang.annotation.*;

/**
 * Used to tell that the annotated class is a native library.
 * A new class that implements the annotated class will be made where you can call mappings of functions from the native library.
 * Functions can be mapped by using @Function and giving the function the corresponding java primitive arguments or MemorySegment if it's a pointer or other datatype.
 * @see LibraryComponent
 * @see Function
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Library {

    String DEFAULT_LIBRARY = "";

    /**
     * Used to configure when all handles get loaded.
     */
    enum LoadMethod {
        /**
         * Add a method called loadHandles() and make it so the user can choose when to load all handles.
         */
        EXPLICIT,
        /**
         * Load all handles when the class is loaded.
         */
        STATIC
    }

    /**
     * Represents the native library's name.
     * @return The native library's name.
     */
    String value() default DEFAULT_LIBRARY;
    LoadMethod loadMethod() default LoadMethod.STATIC;
}
