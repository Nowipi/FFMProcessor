package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.Function;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.stream.Collectors;

sealed class NativeFunction permits CapturingNativeFunction, NativeMethod {

    protected final NativeLibrary library;
    private final Function annotation;
    protected final ExecutableElement javaDeclaration;
    protected final Types types;
    protected final Elements elements;
    protected final TypeMirror pointerType;

    public NativeFunction(NativeLibrary library, Function annotation, ExecutableElement javaDeclaration, ProcessingEnvironment env) {
        elements = env.getElementUtils();
        types = env.getTypeUtils();
        pointerType = elements.getTypeElement("io.github.nowipi.ffm.processor.pointer.Pointer").asType();
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
            return typeToValueLayout(javaDeclaration.getReturnType());
        }
        String parameters = javaDeclaration.getParameters().stream()
                .map(e -> typeToValueLayout(e.asType()))
                .collect(Collectors.joining(", "));
        return typeToValueLayout(javaDeclaration.getReturnType()) + ", " + parameters;
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
                    if (isStruct(v.asType()) || !isValue(v.asType())) {
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

    public String typeToValueLayout(TypeMirror type) {

        boolean isValue = isValue(type);

        if (isValue) {
            if (type.getKind().isPrimitive()) {
                return "ValueLayout.JAVA_" + (type.getKind().name());
            }
            return type + ".LAYOUT";
        }

        return "ValueLayout.ADDRESS";
    }

    private boolean isValue(TypeMirror type) {

        if (type.getKind().isPrimitive()) {
            return true;
        }
        if (type.getKind() != TypeKind.DECLARED) {
            return true;
        }
        return !types.isSubtype(types.erasure(type), types.erasure(pointerType));
    }

    public String getPointerClass(DeclaredType pointerInterfaceType) {
        TypeMirror genericType = pointerInterfaceType.getTypeArguments().get(0);
        if (isStruct(genericType)) {
            TypeElement found = elements.getTypeElement("io.github.nowipi.ffm.processor.pointer.StructPointer");
            return found.getQualifiedName().toString() + "<>(" + genericType + "::getNativeSegment, " + genericType + "::from, ";
        }
        if (genericType.getKind() == TypeKind.VOID || types.isSameType(genericType, elements.getTypeElement("java.lang.Void").asType())) {
            return elements.getTypeElement("io.github.nowipi.ffm.processor.pointer.VoidPointer").asType().toString() + "(";
        }
        if (types.isSubtype(genericType, elements.getTypeElement("java.lang.Number").asType())) {
            return elements.getTypeElement("io.github.nowipi.ffm.processor.pointer." + ((DeclaredType)genericType).asElement().getSimpleName() + "Pointer").asType().toString() + "(";
        }

        IO.println(genericType);
        return null;
    }

    private boolean isStruct(TypeMirror type) {
        return type.getKind() == TypeKind.ERROR;
    }

    public boolean hasStructReturn() {
        return isStruct(javaDeclaration.getReturnType());
    }

    public boolean hasPointerReturn() {
        return !isValue(javaDeclaration.getReturnType());
    }
}
