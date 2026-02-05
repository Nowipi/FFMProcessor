package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.Address;
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
import java.util.ArrayList;
import java.util.List;

public class StructFileData {

    public record MemberData(String offsetName, String name, String layout, String javaTypeName) {
        public boolean isStruct() {
            return layout.contains(".LAYOUT");
        }
    }

    private final List<MemberData> members;
    private final String className;
    private final String packageName;

    public StructFileData(TypeElement annotatedElement, Struct annotation, ProcessingEnvironment env) {

        if (annotatedElement.getKind() != ElementKind.INTERFACE) {
            throw new IllegalArgumentException("Types annotated with Struct need to be of the kind: INTERFACE");
        }

        Elements elements = env.getElementUtils();

        packageName = elements.getPackageOf(annotatedElement).getQualifiedName().toString();
        className = annotation.value();


        members = new ArrayList<>();

        for (Element member : annotatedElement.getEnclosedElements()) {

            Address address = member.getAnnotation(Address.class);
            ExecutableElement functionElement = (ExecutableElement) member;

            if (!functionElement.getParameters().isEmpty()) {
                throw new IllegalArgumentException("Function cannot have parameters " + functionElement.getEnclosingElement() + "#" + functionElement);
            }

            String name = functionElement.getSimpleName().toString();

            String layout;

            if (address != null) {
                layout = "ValueLayout.ADDRESS";
            } else {
                layout = typeToValueLayout(functionElement.getReturnType());
            }


            members.add(new MemberData(name+ "Offset", name, layout, functionElement.getReturnType().toString()));
        }
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
