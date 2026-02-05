package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.Library;
import io.github.nowipi.ffm.processor.annotations.Struct;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes({
        "io.github.nowipi.ffm.processor.annotations.Library",
        "io.github.nowipi.ffm.processor.annotations.Function",
        "io.github.nowipi.ffm.processor.annotations.Capture",
        "io.github.nowipi.ffm.processor.annotations.CaptureState",
        "io.github.nowipi.ffm.processor.annotations.Value",
        "io.github.nowipi.ffm.processor.annotations.Struct",
})
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public final class FFMProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        this.filer = env.getFiler();
        this.messager = env.getMessager();
    }

    private boolean processed;
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (processed || roundEnv.processingOver()) {
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Struct.class)) {
            if (element instanceof TypeElement typed) {
                processStruct(typed, element.getAnnotation(Struct.class));
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Library.class)) {
            if (element instanceof TypeElement typed) {
                processLibrary(typed, element.getAnnotation(Library.class));

            }
        }
        processed = true;
        return true;
    }

    private void processStruct(TypeElement annotatedElement, Struct structAnnotation) {
        var writer = new StructWriter(new StructFileData(annotatedElement, structAnnotation, processingEnv));
        try {
            writer.createStructClass(filer);
        } catch (IOException e) {
            messager.printError("Failed to generate struct: " + structAnnotation.value() + " " + e.getMessage());
        }
    }

    private void processLibrary(TypeElement annotatedElement, Library libraryAnnotation) {

        NativeLibrary nativeLibrary = switch (annotatedElement.getKind()) {
            case CLASS -> new NativeLibraryClass(annotatedElement, libraryAnnotation, processingEnv);
            case INTERFACE -> new NativeLibraryInterface(annotatedElement, libraryAnnotation, processingEnv);
            default -> throw new IllegalStateException("Only an abstract class or interface can be a native library, this is a: " + annotatedElement.getKind());
        };

        var writer = new NativeLibraryImplementationWriter(nativeLibrary);
        try {
            writer.createImplementationClass(filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Code generation failed: " + e.getMessage());
        }
    }
}
