package io.github.nowipi.ffm;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

final class NativeLibraryInterface {

    private final TypeElement interfaceElement;
    private final Library annotation;
    private final Elements elements;
    private final Types types;
    private final List<NativeFunction> functions;
    private final List<VariableCapture> captures;

    public NativeLibraryInterface(TypeElement interfaceElement, Library annotation, ProcessingEnvironment env) {
        elements = env.getElementUtils();
        types = env.getTypeUtils();
        this.interfaceElement = interfaceElement;
        this.annotation = annotation;

        functions = new ArrayList<>();
        captures = new ArrayList<>();
        findLibraryComponents();
    }

    private void findLibraryComponents() {
        for (Element enclosed : interfaceElement.getEnclosedElements()) {

            {
                Function fn = enclosed.getAnnotation(Function.class);
                if (fn != null) {
                    functions.add(new NativeFunction(fn, (ExecutableElement) enclosed));
                }
            }


            {
                Capture c = enclosed.getAnnotation(Capture.class);
                if (c != null) {
                    captures.add(new VariableCapture(c, (ExecutableElement) enclosed));
                }
            }

        }
    }

    public String packageName() {
        return elements.getPackageOf(interfaceElement).getQualifiedName().toString();
    }

    public String className() {
        return interfaceElement.getSimpleName().toString();
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

    public String functionDescriptorLayout(ExecutableElement method) {
        String parameters = method.getParameters().stream()
                .map(e -> typeToValueLayout(e.asType()))
                .collect(Collectors.joining(", "));
        return typeToValueLayout(method.getReturnType()) + ", " + parameters;
    }

    private String typeToValueLayout(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return "ValueLayout.JAVA_" + (type.getKind().name());
        } else if (types.isSameType(type,  elements.getTypeElement("java.lang.foreign.MemorySegment").asType())) {
            return "ValueLayout.ADDRESS";
        }
        throw new IllegalArgumentException("Unsupported kind: " + type);
    }
}
