package io.github.nowipi.ffm;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class NativeLibraryImplementationWriter {

    private final String implementationClassName;
    private final NativeLibraryInterface nativeLibraryInterface;

    public NativeLibraryImplementationWriter(NativeLibraryInterface nativeLibraryInterface) {
        this.nativeLibraryInterface = nativeLibraryInterface;
        this.implementationClassName = nativeLibraryInterface.className() + "Impl";
    }

    public void createImplementationClass(Filer filer) throws IOException {
        JavaFileObject file = filer.createSourceFile(nativeLibraryInterface.packageName() + "." + implementationClassName);
        writeImplementationClass(file);
    }

    private void writeImplementationClass(JavaFileObject file) {
        try (Writer writer = file.openWriter()) {
            writePackage(writer);
            writeImports(writer);
            writeImplementation(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writePackage(Writer writer) throws IOException {
        writer.write("package " + nativeLibraryInterface.packageName() + ";\n\n");
    }

    private void writeImports(Writer writer) throws IOException {
        writer.write("import java.lang.foreign.*;\nimport java.lang.invoke.MethodHandle;\n\n");
    }

    private void writeImplementation(Writer writer) throws IOException {

        writeClassDeclaration(writer);
        writer.write(" {\n");

        writeClassBody(writer);

        writer.write("}\n");
    }

    private void writeClassDeclaration(Writer writer) throws IOException {
        writer.write("public class ");
        writer.write(implementationClassName);
        writer.write(" implements ");
        writer.write(nativeLibraryInterface.className());
    }

    private void writeClassBody(Writer writer) throws IOException {

        writeAttributes(writer);

        switch (nativeLibraryInterface.loadMethod()) {
            case STATIC:
                writeStaticHandles(writer);
                break;
            case EXPLICIT:
                writeExplicitHandles(writer);
        }
        writeMethodImplementations(writer);
    }

    private void writeAttributes(Writer writer) throws IOException {
        writer.write("  private static final Arena arena = Arena.global();\n  private static final Linker linker = Linker.nativeLinker();\n  private static final SymbolLookup lookup = ");
        writeLibraryLookupInitialization(writer);
        writer.write(";\n\n");
    }

    private void writeLibraryLookupInitialization(Writer writer) throws IOException {
        Optional<String> nativeLibraryName = nativeLibraryInterface.nativeLibraryName();
        if (nativeLibraryName.isEmpty()) {
            writer.write("linker.defaultLookup()");
        } else {
            writer.write("SymbolLookup.libraryLookup(\"");
            writer.write(nativeLibraryName.get());
            writer.write("\", arena)");
        }
    }

    private void writeExplicitHandles(Writer writer) throws IOException {

        //handle definitions
        for (NativeFunction nativeFunction : nativeLibraryInterface.functions()) {
            writer.write("  private static MethodHandle ");
            writer.write(nativeFunction.handleName());
            writer.write(";\n");
        }

        writer.write("\n  public static void loadHandles() {\n");
        writeHandleInitializers(writer);
        writer.write("  }\n");
    }

    private void writeStaticHandles(Writer writer) throws IOException {

        //handle definitions
        for (NativeFunction nativeFunction : nativeLibraryInterface.functions()) {
            writer.write("  private static final MethodHandle ");
            writer.write(nativeFunction.handleName());
            writer.write(";\n");
        }

        //static block (static initializer)
        writer.write("\n  static {\n");
        writeHandleInitializers(writer);
        writer.write("  }\n");
    }

    private void writeHandleInitializers(Writer writer) throws IOException {

        String captureNameList = "";
        List<VariableCapture> captures = nativeLibraryInterface.captures();
        if (!captures.isEmpty()) {
            captureNameList = captures.stream().map(c -> c.annotation().value()).collect(Collectors.joining(", "));
        }

        for (NativeFunction nativeFunction : nativeLibraryInterface.functions()) {

            writer.write("      FunctionDescriptor ");
            String descriptorName = nativeFunction.nativeName() + "Descriptor";
            writer.write(descriptorName);
            writer.write(" = FunctionDescriptor.of(");
            writer.write(nativeLibraryInterface.functionDescriptorLayout(nativeFunction.javaDeclaration()));
            writer.write(");\n      ");

            writer.write(nativeFunction.handleName());
            writer.write(" = linker.downcallHandle(lookup.find(\"");
            writer.write(nativeFunction.nativeName());
            writer.write("\").orElseThrow(), ");
            writer.write(descriptorName);

            if (!captures.isEmpty()) {
                writer.write(", Linker.Option.captureCallState(\"");
                writer.write(captureNameList);
                writer.write("\")");
            }

            writer.write(");\n");
        }
    }

    private void writeMethodImplementations(Writer writer) throws IOException {
        for (NativeFunction nativeFunction : nativeLibraryInterface.functions()) {

            ExecutableElement method = nativeFunction.javaDeclaration();

            writer.write("  @Override\n  public ");
            writer.write(method.getReturnType().toString());
            writer.write(" ");
            writer.write(method.getSimpleName().toString());

            writer.write("(");
            String typedAndNamedParameters = method.getParameters().stream()
                    .map(v -> v.asType().toString() + " " + v.getSimpleName().toString())
                    .collect(Collectors.joining(", "));
            writer.write(typedAndNamedParameters);

            writer.write(") {\n     ");

            writer.write("  try { return (");
            writer.write(method.getReturnType().toString());
            writer.write(") ");
            writer.write(nativeFunction.handleName());
            writer.write(".invokeExact(");
            writer.write(method.getParameters().toString());

            writer.write(");} catch (Throwable e) { throw new RuntimeException(e); }\n");
            writer.write("  }\n");
        }
    }



}
