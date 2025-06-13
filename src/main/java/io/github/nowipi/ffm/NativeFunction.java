package io.github.nowipi.ffm;

import javax.lang.model.element.ExecutableElement;

record NativeFunction(Function annotation, ExecutableElement javaDeclaration) implements AnnotatedLibraryComponent<Function> {

    @Override
    public String handleName() {
        return annotation.value() + "Handle";
    }

    @Override
    public String nativeName() {
        return annotation.value();
    }
}
