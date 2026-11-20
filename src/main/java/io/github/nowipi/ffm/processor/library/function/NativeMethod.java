package io.github.nowipi.ffm.processor.library.function;

import io.github.nowipi.ffm.processor.annotations.Function;
import io.github.nowipi.ffm.processor.library.NativeLibrary;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import java.util.stream.Collectors;

sealed public class NativeMethod extends NativeFunction permits CapturingNativeMethod {

    public NativeMethod(NativeLibrary library, Function annotation, ExecutableElement javaDeclaration, ProcessingEnvironment env) {
        super(library, annotation, javaDeclaration, env);
        if (javaDeclaration.getReturnType().getKind() != TypeKind.VOID) {
            throw new IllegalArgumentException("Native methods can not return anything. Expected return type: void, but got: " +  javaDeclaration.getReturnType());
        }
    }

    @Override
    public String functionDescriptorLayout() {
        return javaDeclaration.getParameters().stream()
                .map(e -> typeToValueLayout(e.asType()))
                .collect(Collectors.joining(", "));
    }

    @Override
    public String javaReturnTypeName() {
        return "void";
    }

}
