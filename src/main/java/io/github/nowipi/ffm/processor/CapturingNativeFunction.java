package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.Capture;
import io.github.nowipi.ffm.processor.annotations.Function;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;

final class CapturingNativeFunction extends NativeFunction implements Capturing {

    private final Capture captureStateAnnotation;

    public CapturingNativeFunction(NativeLibrary library, Function functionAnnotation, ExecutableElement javaDeclaration, Capture captureStateAnnotation, ProcessingEnvironment env) {
        super(library, functionAnnotation, javaDeclaration, env);
        this.captureStateAnnotation = captureStateAnnotation;
    }

    @Override
    public String capturedStateName() {
        return captureStateAnnotation.value();
    }
}
