package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.Library;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

public final class NativeLibraryClass extends NativeLibrary {

    private ExecutableElement constructor;

    public NativeLibraryClass(TypeElement typeElement, Library annotation, ProcessingEnvironment env) {
        super(typeElement, annotation, env);
        for(Element element : typeElement.getEnclosedElements()) {
            if(element.getKind() == ElementKind.CONSTRUCTOR) {
                constructor = (ExecutableElement) element;
                break;
            }
        }
    }

    public Optional<ExecutableElement> constructor() {
        return Optional.ofNullable(constructor);
    }
}
