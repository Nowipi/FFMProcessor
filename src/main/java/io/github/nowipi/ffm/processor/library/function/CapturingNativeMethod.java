package io.github.nowipi.ffm.processor.library.function;

import io.github.nowipi.ffm.processor.annotations.Capture;
import io.github.nowipi.ffm.processor.annotations.Function;
import io.github.nowipi.ffm.processor.library.NativeLibrary;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;

public final class CapturingNativeMethod extends NativeMethod implements Capturing {

    private final Capture captureStateAnnotation;

    public CapturingNativeMethod(NativeLibrary library, Function annotation, ExecutableElement javaDeclaration, Capture captureStateAnnotation, ProcessingEnvironment env) {
        super(library, annotation, javaDeclaration, env);
        this.captureStateAnnotation = captureStateAnnotation;
    }

    @Override
    public String capturedStateName() {
        return captureStateAnnotation.value();
    }
}
