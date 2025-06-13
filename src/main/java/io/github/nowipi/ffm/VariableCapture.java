package io.github.nowipi.ffm;

import javax.lang.model.element.ExecutableElement;

record VariableCapture(Capture annotation, ExecutableElement javaDeclaration) implements AnnotatedLibraryComponent<Capture> {

    public String handleName() {
        return annotation.value() + "Handle";
    }

    public String nativeName() {
        return annotation.value();
    }
}
