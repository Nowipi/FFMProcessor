package io.github.nowipi.ffm.processor.library.function;

import io.github.nowipi.ffm.processor.annotations.CaptureState;

import javax.lang.model.element.ExecutableElement;

public record VariableCapture(CaptureState annotation, ExecutableElement javaDeclaration) {

    public String handleName() {
        return annotation.value() + "Handle";
    }

    public String nativeName() {
        return annotation.value();
    }

    public String javaType() {
        return javaDeclaration.getReturnType().toString();
    }

    public String javaName() {
        return javaDeclaration.getSimpleName().toString();
    }
}
