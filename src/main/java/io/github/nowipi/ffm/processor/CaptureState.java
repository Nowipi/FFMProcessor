package io.github.nowipi.ffm.processor;

/**
 * Represents a variable that will be captured when using @Capture.
 * @see Capture
 */
@LibraryComponent
public @interface CaptureState {

    /**
     * The native name of the captured variable.
     * @return The native name.
     */
    String value();
}
