package io.github.nowipi.ffm;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SupportedAnnotationTypes("io.github.nowipi.ffm.Library")
@SupportedSourceVersion(SourceVersion.RELEASE_24)
public final class FFMProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        this.filer = env.getFiler();
        this.messager = env.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Library.class)) {

            if (element.getKind() != ElementKind.INTERFACE) throw new RuntimeException("Only interfaces can be annotated with @Library");

            processLibrary((TypeElement) element, element.getAnnotation(Library.class));
        }
        return true;
    }

    private void processLibrary(TypeElement interfaceElement, Library annotation) {

        NativeLibraryImplementationWriter writer = new NativeLibraryImplementationWriter(new NativeLibraryInterface(interfaceElement, annotation, processingEnv));
        try {
            writer.createImplementationClass(filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Code generation failed: " + e.getMessage());
        }
    }
}
