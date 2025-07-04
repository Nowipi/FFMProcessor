package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.Function;

import javax.lang.model.element.ExecutableElement;
import java.util.stream.Collectors;

sealed class NativeFunction permits CapturingNativeFunction, NativeMethod {

    protected final NativeLibrary library;
    private final Function annotation;
    protected final ExecutableElement javaDeclaration;

    public NativeFunction(NativeLibrary library, Function annotation, ExecutableElement javaDeclaration) {
        this.library = library;
        this.annotation = annotation;
        this.javaDeclaration = javaDeclaration;
    }

    public String handleName() {
        return nativeName() + "Handle";
    }

    public String nativeName() {
        String nativeName = annotation.value();
        if (nativeName.isEmpty()) {
            nativeName = javaDeclaration.getSimpleName().toString();
        }
        return nativeName;
    }

    public String functionDescriptorLayout() {
        if (javaDeclaration.getParameters().isEmpty()) {
            return library.typeToValueLayout(javaDeclaration.getReturnType());
        }
        String parameters = javaDeclaration.getParameters().stream()
                .map(e -> library.typeToValueLayout(e.asType()))
                .collect(Collectors.joining(", "));
        return library.typeToValueLayout(javaDeclaration.getReturnType()) + ", " + parameters;
    }

    public String javaReturnType() {
        return javaDeclaration.getReturnType().toString();
    }

    public String javaParameterNames() {
        return javaDeclaration.getParameters().toString();
    }

    public String javaName() {
        return javaDeclaration.getSimpleName().toString();
    }

    public String javaParameterTypedNames() {
        return javaDeclaration.getParameters().stream()
                .map(v -> v.asType().toString() + " " + v.getSimpleName().toString())
                .collect(Collectors.joining(", "));
    }
}
