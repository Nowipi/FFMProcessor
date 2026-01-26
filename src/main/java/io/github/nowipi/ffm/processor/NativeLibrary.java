package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.stream.Collectors;

public sealed class NativeLibrary permits NativeLibraryInterface, NativeLibraryClass {

    private final TypeElement typeElement;
    private final Library annotation;
    private final Elements elements;
    private final Types types;
    private final List<NativeFunction> functions;
    private final List<VariableCapture> captures;
    private final boolean hasVirtualMethods;

    public NativeLibrary(TypeElement typeElement, Library annotation, ProcessingEnvironment env) {
        elements = env.getElementUtils();
        types = env.getTypeUtils();
        this.typeElement = typeElement;
        this.annotation = annotation;

        functions = new ArrayList<>();
        captures = new ArrayList<>();

        findLibraryComponents(typeElement);
        for (TypeMirror extended : typeElement.getInterfaces()) {
            if (extended instanceof DeclaredType t) {
                findLibraryComponents((TypeElement) t.asElement());
            }
        }
        hasVirtualMethods = hasUnimplementedMethods(typeElement, types);
    }

    private boolean hasUnimplementedMethods(TypeElement typeElement, Types typeUtils) {
        TypeMirror typeMirror = typeElement.asType();

        Set<ExecutableElement> allAbstractMethods = new LinkedHashSet<>();

        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.METHOD && enclosed.getModifiers().contains(Modifier.ABSTRACT)) {
                allAbstractMethods.add((ExecutableElement) enclosed);
            }
        }

        for (TypeMirror supertype : typeUtils.directSupertypes(typeMirror)) {
            TypeElement superElement = (TypeElement)typeUtils.asElement(supertype);
            for (Element enclosed : superElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.METHOD && enclosed.getModifiers().contains(Modifier.ABSTRACT)) {
                    allAbstractMethods.add((ExecutableElement) enclosed);
                }
            }
        }

        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.METHOD) continue;
            ExecutableElement method = (ExecutableElement) enclosed;

            if (
                    (method.getAnnotation(Function.class) == null && method.getModifiers().contains(Modifier.ABSTRACT)) &&
                    (method.getAnnotation(CaptureState.class) == null && method.getModifiers().contains(Modifier.ABSTRACT))
            )  {
                continue;
            }
            allAbstractMethods.remove(method);
        }

        return !allAbstractMethods.isEmpty();
    }


    private void findLibraryComponents(TypeElement parentElement) {
        for (Element enclosed : parentElement.getEnclosedElements()) {
            {
                Function functionAnnotation = enclosed.getAnnotation(Function.class);
                Capture captureAnnotation = enclosed.getAnnotation(Capture.class);

                if (functionAnnotation != null) {

                    ExecutableElement functionElement = (ExecutableElement) enclosed;
                    boolean isMethod = functionElement.getReturnType().getKind() == TypeKind.VOID;

                    if (captureAnnotation != null) {
                        if (isMethod) {
                            functions.add(new CapturingNativeMethod(this, functionAnnotation, (ExecutableElement) enclosed, captureAnnotation));
                        } else {
                            functions.add(new CapturingNativeFunction(this, functionAnnotation, (ExecutableElement) enclosed, captureAnnotation));
                        }

                    } else {
                        if (isMethod) {
                            functions.add(new NativeMethod(this, functionAnnotation, (ExecutableElement) enclosed));
                        } else {
                            functions.add(new NativeFunction(this, functionAnnotation, (ExecutableElement) enclosed));
                        }
                    }
                }
            }


            {
                CaptureState annotation = enclosed.getAnnotation(CaptureState.class);
                if (annotation != null) {
                    captures.add(new VariableCapture(annotation, (ExecutableElement) enclosed));
                }
            }

        }
    }

    public String packageName() {
        return elements.getPackageOf(typeElement).getQualifiedName().toString();
    }

    public String className() {
        return typeElement.getSimpleName().toString();
    }


    public Optional<String> nativeLibraryName() {
        String libraryName = annotation.value();
        if (libraryName.equals(Library.DEFAULT_LIBRARY)) {
            return Optional.empty();
        } else {
            return Optional.of(libraryName);
        }
    }

    public Library.LoadMethod loadMethod() {
        return annotation.loadMethod();
    }

    public List<NativeFunction> functions() {
        return Collections.unmodifiableList(functions);
    }

    public List<VariableCapture> captures() {
        return Collections.unmodifiableList(captures);
    }

    public String typeToValueLayout(TypeMirror type, Value value) {
        if (type.getKind().isPrimitive()) {
            return "ValueLayout.JAVA_" + (type.getKind().name());
        } else {
            if (value == null) {
                return "ValueLayout.ADDRESS";
            }
            return type + ".LAYOUT";

        }
    }

    public boolean hasVirtualMethods() {
        return hasVirtualMethods;
    }
}
