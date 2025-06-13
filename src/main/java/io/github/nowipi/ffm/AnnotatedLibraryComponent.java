package io.github.nowipi.ffm;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;

interface AnnotatedLibraryComponent<A extends Annotation> {

    A annotation();

    ExecutableElement javaDeclaration();

    String handleName();

    String nativeName();
}
