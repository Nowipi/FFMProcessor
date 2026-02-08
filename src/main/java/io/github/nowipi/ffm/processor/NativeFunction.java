package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.Function;
import io.github.nowipi.ffm.processor.annotations.Value;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
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
            return library.typeToValueLayout(javaDeclaration.getReturnType(), javaDeclaration.getAnnotation(Value.class));
        }
        String parameters = javaDeclaration.getParameters().stream()
                .map(e -> library.typeToValueLayout(e.asType(), e.getAnnotation(Value.class)))
                .collect(Collectors.joining(", "));
        return library.typeToValueLayout(javaDeclaration.getReturnType(), javaDeclaration.getAnnotation(Value.class)) + ", " + parameters;
    }

    public String javaReturnTypeName() {
        TypeMirror returnType = javaDeclaration.getReturnType();
        return returnType.toString();
    }

    public TypeMirror javaReturnType() {
        return javaDeclaration.getReturnType();
    }

    public String javaParameterNames() {
        return javaDeclaration.getParameters().stream()
                .map(v -> {
                    if (v.asType().getKind() == TypeKind.ERROR) {
                        return v.getSimpleName().toString() + ".getNativeSegment()";
                    }
                    return v.getSimpleName().toString();
                })
                .collect(Collectors.joining(", "));
    }

    public String javaName() {
        return javaDeclaration.getSimpleName().toString();
    }

    public String javaParameterTypedNames() {
        return javaDeclaration.getParameters().stream()
                .map(v -> v.asType().toString() + " " + v.getSimpleName().toString())
                .collect(Collectors.joining(", "));
    }

    public boolean hasValueReturn() {
        return javaDeclaration.getAnnotation(Value.class) != null;
    }
}
