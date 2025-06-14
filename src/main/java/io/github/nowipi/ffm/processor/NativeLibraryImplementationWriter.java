package io.github.nowipi.ffm.processor;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;

final class NativeLibraryImplementationWriter {

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
        writer.write("import java.lang.foreign.*;\nimport java.lang.invoke.MethodHandle;\nimport java.lang.invoke.VarHandle;\n\n");
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
                writeStaticHandleInitialization(writer);
                break;
            case EXPLICIT:
                writeExplicitHandleInitialization(writer);
        }

        writeMethodImplementations(writer);
        writeCaptureImplementations(writer);
    }

    private void writeAttributes(Writer writer) throws IOException {
        writer.write("  private static final Arena arena = Arena.global();\n  private static final Linker linker = Linker.nativeLinker();\n  private static final SymbolLookup lookup = ");
        writeLibraryLookupInitialization(writer);
        writer.write(";\n\n");

        if (!nativeLibraryInterface.captures().isEmpty())
            writeCaptureAttributes(writer);

        writeMethodHandleAttributes(writer);
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

    private void writeCaptureAttributes(Writer writer) throws IOException {
        writer.write("  private static final MemorySegment capturedState;\n");
        writeCaptureStateAttributes(writer);
    }

    private void writeCaptureStateAttributes(Writer writer) throws IOException {
        for (VariableCapture capture : nativeLibraryInterface.captures()) {
            writer.write("  private static final VarHandle ");
            writer.write(capture.handleName());
            writer.write(";\n");
        }
    }

    private void writeMethodHandleAttributes(Writer writer) throws IOException {

        //handle definitions
        for (NativeFunction nativeFunction : nativeLibraryInterface.functions()) {
            writer.write("  private static final MethodHandle ");
            writer.write(nativeFunction.handleName());
            writer.write(";\n");
        }

    }

    private void writeExplicitHandleInitialization(Writer writer) throws IOException {

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

    private void writeStaticHandleInitialization(Writer writer) throws IOException {
        writer.write("\n  static {\n");
        writeHandleInitializers(writer);
        writer.write("  }\n");
    }

    private void writeHandleInitializers(Writer writer) throws IOException {

        List<VariableCapture> captures = nativeLibraryInterface.captures();
        if (!captures.isEmpty()) {
            writer.write("      var captureStateLayout = Linker.Option.captureStateLayout();\n      capturedState = arena.allocate(captureStateLayout);\n");
            for (VariableCapture capture : nativeLibraryInterface.captures()) {
                writer.write("      ");
                writer.write(capture.handleName());
                writer.write(" = captureStateLayout.varHandle(MemoryLayout.PathElement.groupElement(\"");
                writer.write(capture.nativeName());
                writer.write("\"));\n");
            }
        }


        for (NativeFunction nativeFunction : nativeLibraryInterface.functions()) {

            writer.write("      FunctionDescriptor ");
            String descriptorName = nativeFunction.nativeName() + "Descriptor";
            writer.write(descriptorName);
            writer.write(" = FunctionDescriptor.of(");
            writer.write(nativeFunction.functionDescriptorLayout());
            writer.write(");\n      ");

            writer.write(nativeFunction.handleName());
            writer.write(" = linker.downcallHandle(lookup.find(\"");
            writer.write(nativeFunction.nativeName());
            writer.write("\").orElseThrow(), ");
            writer.write(descriptorName);

            if (nativeFunction instanceof CaptureStateNativeFunction captured) {
                writer.write(", Linker.Option.captureCallState(\"");
                writer.write(captured.capturedStateName());
                writer.write("\")");
            }

            writer.write(");\n");
        }
    }

    private void writeMethodImplementations(Writer writer) throws IOException {
        for (NativeFunction nativeFunction : nativeLibraryInterface.functions()) {


            var javaReturnType = nativeFunction.javaReturnType();
            writer.write("  @Override\n  public ");
            writer.write(javaReturnType);
            writer.write(" ");
            writer.write(nativeFunction.javaName());

            writer.write("(");
            writer.write(nativeFunction.javaParameterTypedNames());

            writer.write(") {\n     ");

            writer.write("  try { return (");
            writer.write(javaReturnType);
            writer.write(") ");
            writer.write(nativeFunction.handleName());
            writer.write(".invokeExact(");
            if (nativeFunction instanceof CaptureStateNativeFunction) {
                writer.write("capturedState,");
            }
            writer.write(nativeFunction.javaParameterNames());

            writer.write(");} catch (Throwable e) { throw new RuntimeException(e); }\n");
            writer.write("  }\n");
        }
    }

    private void writeCaptureImplementations(Writer writer) throws IOException {
        for (VariableCapture capture : nativeLibraryInterface.captures()) {


            var javaType = capture.javaType();
            writer.write("  @Override\n  public ");
            writer.write(javaType);
            writer.write(" ");
            writer.write(capture.javaName());

            writer.write("() {\n        return (");
            writer.write(javaType);
            writer.write(") ");
            writer.write(capture.handleName());
            writer.write(".get(");
            writer.write("capturedState, 0);\n  }\n");
        }
    }



}
