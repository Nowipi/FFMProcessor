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

    public abstract sealed static class StructMember {

        private final String name;
        private final String offsetName;
        private final String typeName;


        public StructMember(ExecutableElement functionElement) {
            name = functionElement.getSimpleName().toString();
            offsetName = name + "Offset";
            typeName = functionElement.getReturnType().toString();
        }

        public String getOffsetName() {
            return offsetName;
        }

        public String getName() {
            return name;
        }

        public abstract String getLayout();

        public String getTypeName() {
            return typeName;
        }

        private static boolean isPointer(TypeMirror type, Types types, TypeMirror pointerClassType) {

            if (type.getKind() != TypeKind.DECLARED) {
                return false;
            }
            return types.isSubtype(types.erasure(type), types.erasure(pointerClassType));
        }

        public static StructMember from(ExecutableElement element, Types types, Elements elements, TypeMirror pointerClassType) {
            TypeMirror returnType = element.getReturnType();
            if (isPointer(returnType, types, pointerClassType)) {
                return new PointerMember(element, types, elements);
            }

            if (returnType.getKind().isPrimitive()) {
                return new PrimitiveValueMember(element);
            }

            return new StructValueMember(element);
        }
    }

    public static final class PointerMember extends StructMember {

        private final String pointsToLayout;
        private final boolean pointsToStruct;
        private final String pointerClassImplementation;
        private final String pointsToTypeName;

        public PointerMember(ExecutableElement functionElement, Types types, Elements elements) {
            super(functionElement);
            TypeMirror genericType = ((DeclaredType)functionElement.getReturnType()).getTypeArguments().get(0);

            pointsToTypeName = ((DeclaredType)genericType).asElement().getSimpleName().toString();

            boolean struct = false;
            if (genericType.getKind() == TypeKind.ERROR) {
                struct = true;
                pointsToLayout = genericType + ".LAYOUT";
                pointerClassImplementation = "io.github.nowipi.ffm.processor.pointer.StructPointer";
            } else if (types.isSubtype(genericType, elements.getTypeElement("java.lang.Number").asType())) {
                pointsToLayout = switch (pointsToTypeName) {
                    case "Integer" -> "ValueLayout.JAVA_INT";
                    case "Double" -> "ValueLayout.JAVA_DOUBLE";
                    case "Float" -> "ValueLayout.JAVA_FLOAT";
                    case "Short" -> "ValueLayout.JAVA_SHORT";
                    case "Long" -> "ValueLayout.JAVA_LONG";
                    case "Character" -> "ValueLayout.JAVA_CHAR";
                    case "Byte" -> "ValueLayout.JAVA_BYTE";
                    case "Boolean" -> "ValueLayout.JAVA_BOOLEAN";
                    default -> throw new IllegalStateException("Unexpected value: " + genericType);
                };
                pointerClassImplementation = "io.github.nowipi.ffm.processor.pointer." + ((DeclaredType)genericType).asElement().getSimpleName() + "Pointer";
            } else if (types.isSameType(genericType, elements.getTypeElement("java.lang.Void").asType())) {
                pointerClassImplementation = "io.github.nowipi.ffm.processor.pointer.VoidPointer";
                pointsToLayout = "ValueLayout.JAVA_BYTE";
            } else {
                throw new IllegalArgumentException("Not a valid member " + functionElement);
            }

            pointsToStruct = struct;

        }

        @Override
        public String getLayout() {
            return "ValueLayout.ADDRESS";
        }

        public boolean isPointsToStruct() {
            return pointsToStruct;
        }

        public String getPointsToLayout() {
            return pointsToLayout;
        }

        public String getPointerClassImplementation() {
            return pointerClassImplementation;
        }

        public String getPointsToTypeName() {
            return pointsToTypeName;
        }
    }

    public abstract sealed static class ValueMember extends StructMember {

        public ValueMember(ExecutableElement functionElement) {
            super(functionElement);
        }
    }

    public static final class PrimitiveValueMember extends ValueMember {

        private final String layout;

        public PrimitiveValueMember(ExecutableElement functionElement) {
            super(functionElement);
            layout = "ValueLayout.JAVA_" + (functionElement.getReturnType().getKind().name());
        }

        @Override
        public String getLayout() {
            return layout;
        }
    }

    public static final class StructValueMember extends ValueMember {
        private final String layout;

        public StructValueMember(ExecutableElement functionElement) {
            super(functionElement);
            layout = functionElement.getReturnType() + ".LAYOUT";
        }

        @Override
        public String getLayout() {
            return layout;
        }
    }

    private final List<StructMember> members;
    private final String className;
    private final String packageName;
    private final boolean padded;

    public StructFileData(TypeElement annotatedElement, Struct annotation, ProcessingEnvironment env) {

        if (annotatedElement.getKind() != ElementKind.INTERFACE) {
            throw new IllegalArgumentException("Types annotated with Struct need to be of the kind: INTERFACE");
        }

        Elements elements = env.getElementUtils();
        TypeMirror pointerType = elements.getTypeElement("io.github.nowipi.ffm.processor.pointer.Pointer").asType();
        Types types = env.getTypeUtils();
        packageName = elements.getPackageOf(annotatedElement).getQualifiedName().toString();
        className = annotation.value();
        padded = annotation.autoPadded();


        members = new ArrayList<>();

        for (Element member : annotatedElement.getEnclosedElements()) {

            ExecutableElement functionElement = (ExecutableElement) member;

            if (!functionElement.getParameters().isEmpty()) {
                throw new IllegalArgumentException("Function cannot have parameters " + functionElement.getEnclosingElement() + "#" + functionElement);
            }

            members.add(StructMember.from(functionElement, types, elements, pointerType));
        }
    }

    public List<StructMember> getMembers() {
        return members;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public boolean isPadded() {
        return padded;
    }
}
