package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.Function;
import io.github.nowipi.ffm.processor.annotations.Value;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import java.util.stream.Collectors;

sealed class NativeMethod extends NativeFunction permits CapturingNativeMethod {

    public NativeMethod(NativeLibrary library, Function annotation, ExecutableElement javaDeclaration) {
        super(library, annotation, javaDeclaration);
        if (javaDeclaration.getReturnType().getKind() != TypeKind.VOID) {
            throw new IllegalArgumentException("Native methods can not return anything. Expected return type: void, but got: " +  javaDeclaration.getReturnType());
        }
    }

    @Override
    public String functionDescriptorLayout() {
        return javaDeclaration.getParameters().stream()
                .map(e -> library.typeToValueLayout(e.asType(), e.getAnnotation(Value.class)))
                .collect(Collectors.joining(", "));
    }

    @Override
    public String javaReturnTypeName() {
        return "void";
    }

}
