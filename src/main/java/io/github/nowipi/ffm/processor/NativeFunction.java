package io.github.nowipi.ffm.processor;

import javax.lang.model.element.ExecutableElement;
import java.util.stream.Collectors;

sealed class NativeFunction permits CapturingNativeFunction, NativeMethod {

    protected final NativeLibraryInterface library;
    private final Function annotation;
    protected final ExecutableElement javaDeclaration;

    public NativeFunction(NativeLibraryInterface library, Function annotation, ExecutableElement javaDeclaration) {
        this.library = library;
        this.annotation = annotation;
        this.javaDeclaration = javaDeclaration;
    }

    public String handleName() {
        return annotation.value() + "Handle";
    }

    public String nativeName() {
        return annotation.value();
    }

    public String functionDescriptorLayout() {
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
