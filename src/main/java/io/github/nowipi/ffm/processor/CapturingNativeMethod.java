package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.Capture;
import io.github.nowipi.ffm.processor.annotations.Function;

import javax.lang.model.element.ExecutableElement;

final class CapturingNativeMethod extends NativeMethod implements Capturing {

    private final Capture captureStateAnnotation;

    public CapturingNativeMethod(NativeLibrary library, Function annotation, ExecutableElement javaDeclaration, Capture captureStateAnnotation) {
        super(library, annotation, javaDeclaration);
        this.captureStateAnnotation = captureStateAnnotation;
    }

    @Override
    public String capturedStateName() {
        return captureStateAnnotation.value();
    }
}
