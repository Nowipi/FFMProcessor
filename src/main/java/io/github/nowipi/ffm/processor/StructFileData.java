package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.Struct;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

public class StructFileData {

    public record MemberData(String offsetName, String name, String layout, String javaTypeName, TypeMirror type) {
        public boolean isStruct() {
            return layout.contains(".LAYOUT");
        }
        public boolean isPointer() {
            return layout.contains(".ADDRESS");
        }
    }

    private final List<MemberData> members;
    private final String className;
    private final String packageName;
    private final Types types;
    private final Elements elements;
    private final TypeMirror pointerType;

    public StructFileData(TypeElement annotatedElement, Struct annotation, ProcessingEnvironment env) {

        if (annotatedElement.getKind() != ElementKind.INTERFACE) {
            throw new IllegalArgumentException("Types annotated with Struct need to be of the kind: INTERFACE");
        }

        elements = env.getElementUtils();
        pointerType = elements.getTypeElement("io.github.nowipi.ffm.processor.pointer.Pointer").asType();
        types = env.getTypeUtils();
        packageName = elements.getPackageOf(annotatedElement).getQualifiedName().toString();
        className = annotation.value();


        members = new ArrayList<>();

        for (Element member : annotatedElement.getEnclosedElements()) {

            ExecutableElement functionElement = (ExecutableElement) member;

            if (!functionElement.getParameters().isEmpty()) {
                throw new IllegalArgumentException("Function cannot have parameters " + functionElement.getEnclosingElement() + "#" + functionElement);
            }

            String name = functionElement.getSimpleName().toString();

            String layout;

            if (isPointer(functionElement.getReturnType())) {
                layout = "ValueLayout.ADDRESS";
            } else {
                layout = typeToValueLayout(functionElement.getReturnType());
            }


            members.add(new MemberData(name+ "Offset", name, layout, functionElement.getReturnType().toString(), functionElement.getReturnType()));
        }
    }

    //TODO factor out to share same logic with NativeFunction
    private boolean isPointer(TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        return types.isSubtype(types.erasure(type), types.erasure(pointerType));
    }

    private boolean isStruct(TypeMirror type) {
        return type.getKind() == TypeKind.ERROR;
    }

    public DeclaredType getPointerClass(TypeMirror pointerInterfaceType) {
        if (pointerInterfaceType.getKind() != TypeKind.DECLARED)
            return null;

        TypeMirror genericType = ((DeclaredType)pointerInterfaceType).getTypeArguments().get(0);
        if (genericType.getKind() == TypeKind.VOID || types.isSameType(genericType, elements.getTypeElement("java.lang.Void").asType())) {
            return (DeclaredType) elements.getTypeElement("io.github.nowipi.ffm.processor.pointer.VoidPointer").asType();
        }
        if (types.isSubtype(genericType, elements.getTypeElement("java.lang.Number").asType())) {
            return (DeclaredType) elements.getTypeElement("io.github.nowipi.ffm.processor.pointer." + ((DeclaredType)genericType).asElement().getSimpleName() + "Pointer").asType();
        }
        if (isStruct(genericType)) {
            return (DeclaredType) elements.getTypeElement("io.github.nowipi.ffm.processor.pointer.StructPointer").asType();
        }

        IO.println(genericType);
        return null;
    }

    private static String typeToValueLayout(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return "ValueLayout.JAVA_" + (type.getKind().name());
        } else if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) type;
            return declaredType.asElement().getSimpleName() + ".LAYOUT";
        } else {
            return type + ".LAYOUT";
        }
    }

    public List<MemberData> getMembers() {
        return members;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }
}
