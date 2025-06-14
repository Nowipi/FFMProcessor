package io.github.nowipi.ffm.processor;

import javax.lang.model.element.ExecutableElement;

final class CaptureStateNativeFunction extends NativeFunction{

    private final Capture captureStateAnnotation;

    public CaptureStateNativeFunction(NativeLibraryInterface library, Function annotation, ExecutableElement javaDeclaration, Capture captureStateAnnotation) {
        super(library, annotation, javaDeclaration);
        this.captureStateAnnotation = captureStateAnnotation;
    }

    public String capturedStateName() {
        return captureStateAnnotation.value();
    }
}
