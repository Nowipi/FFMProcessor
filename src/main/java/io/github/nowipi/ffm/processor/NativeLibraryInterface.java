package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.Library;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

public final class NativeLibraryInterface extends NativeLibrary {

    public NativeLibraryInterface(TypeElement typeElement, Library annotation, ProcessingEnvironment env) {
        super(typeElement, annotation, env);
    }
}
