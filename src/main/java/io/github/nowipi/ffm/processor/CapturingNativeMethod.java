package io.github.nowipi.ffm.processor;

import javax.lang.model.element.ExecutableElement;

final class CapturingNativeMethod extends NativeMethod implements Capturing {

    private final Capture captureStateAnnotation;

    public CapturingNativeMethod(NativeLibraryInterface library, Function annotation, ExecutableElement javaDeclaration, Capture captureStateAnnotation) {
        super(library, annotation, javaDeclaration);
        this.captureStateAnnotation = captureStateAnnotation;
    }

    @Override
    public String capturedStateName() {
        return captureStateAnnotation.value();
    }
}
