package io.github.nowipi.ffm.processor;

/**
 * Marks a native function as one that will capture a native variable.
 * The variable needs to be defined using @CaptureState.
 * @see CaptureState
 */
@LibraryComponent
public @interface Capture {
    /**
     * The native variable name that will be used for capturing.
     * @return The native variable name.
     */
    String value();
}
