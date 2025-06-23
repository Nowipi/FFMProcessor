package io.github.nowipi.ffm.processor;

import io.github.nowipi.ffm.processor.annotations.Library;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes({
        "io.github.nowipi.ffm.processor.annotations.Library",
        "io.github.nowipi.ffm.processor.annotations.Function",
        "io.github.nowipi.ffm.processor.annotations.Capture",
        "io.github.nowipi.ffm.processor.annotations.CaptureState"
})
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

    private boolean processed;
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (processed) {return false;}

        for (Element element : roundEnv.getElementsAnnotatedWith(Library.class)) {
            if (element instanceof TypeElement typed) {
                processLibrary(typed, element.getAnnotation(Library.class));

            }
        }
        processed = true;
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
