package io.github.nowipi.ffm.processor;

import javax.lang.model.element.ExecutableElement;

final class CapturingNativeFunction extends NativeFunction implements Capturing {

    private final Capture captureStateAnnotation;

    public CapturingNativeFunction(NativeLibraryInterface library, Function functionAnnotation, ExecutableElement javaDeclaration, Capture captureStateAnnotation) {
        super(library, functionAnnotation, javaDeclaration);
        this.captureStateAnnotation = captureStateAnnotation;
    }

    @Override
    public String capturedStateName() {
        return captureStateAnnotation.value();
    }
}
