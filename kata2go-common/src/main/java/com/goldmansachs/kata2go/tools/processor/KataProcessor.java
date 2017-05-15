/*
 Copyright 2017 Goldman Sachs.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package com.goldmansachs.kata2go.tools.processor;

import com.goldmansachs.kata2go.tools.domain.ImmutableKataExercise;
import com.goldmansachs.kata2go.tools.domain.ImmutableKataMetadata;
import com.goldmansachs.kata2go.tools.domain.KataExercise;
import com.goldmansachs.kata2go.tools.domain.KataMetadata;
import com.goldmansachs.kata2go.tools.parser.JavaSourceParser;
import com.goldmansachs.kata2go.tools.utils.TarGz;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.eclipse.collections.api.RichIterable;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.goldmansachs.kata2go.tools.utils.FileUtils.readFile;

/*
    Class to process a raw kata targz and produce a new targz with the main kata
    broken down into individual exercises.
 */
public class KataProcessor
{
    private static final String KATA_FILE_NAME = "exploded-kata.tar.gz";
    private static final String SRC_DIR = "src";
    private static final String KATA_METADATA = "kata.metadata";
    
    private final Path tempDir;

    public KataProcessor(Path tempDir)
    {
        this.tempDir = tempDir;
    }

    public KataProcessor(String tempDir)
    {
        this.tempDir = Paths.get(tempDir);
    }

    public Path process(InputStream inputStream) throws Exception
    {
        Path kataTempDirectory = Files.createTempDirectory(tempDir, String.valueOf(System.currentTimeMillis()));
        TarGz.decompress(inputStream, kataTempDirectory);
        processKata(kataTempDirectory);
        Path outTarGz = kataTempDirectory.resolve(KATA_FILE_NAME);
        compress(kataTempDirectory, outTarGz);
        //TODO : when should this file be deleted ??
        return outTarGz;
    }

    public Path process(Path kataTarGz) throws Exception
    {
        return process(new FileInputStream(kataTarGz.toFile()));
    }

    private void compress(Path kataDir, Path outputTarGz) throws Exception
    {
        TarGz.compress(kataDir,outputTarGz);
    }

    private KataExercise buildExercise(String kataPackageName, String kataClassName, String methodName, String methodBody, String importsBlock)
    {
        String exerciseClassName = kataClassName + "_" + methodName;
        String exerciseFullClassName = kataPackageName + "." + exerciseClassName;
        JavaFile exerciseJavaFile = buildExerciseJavaFile(kataPackageName, exerciseClassName, methodName, methodBody);
        String exerciseJavaSource = addImports(exerciseJavaFile, importsBlock);
        String exerciseJavaFileName = exerciseClassName + ".java";
        return ImmutableKataExercise.builder()
                .sourceFileName(exerciseJavaFileName)
                .className(exerciseFullClassName)
                .source(exerciseJavaSource)
                .build();
    }

    private void processKata(Path kataDir) throws Exception
    {
        JavaSourceParser parser = parseKataSource(kataDir);

        String kataClassName = parser.getKataClassName();
        String kataPackageName = parser.getKataPackageName();
        String importsBlock = parser.getImports().makeString("\n");

        RichIterable<KataExercise> exercises = parser.getMethods().keyValuesView()
                .collect(pair ->
                        buildExercise(kataPackageName, kataClassName, 
                                pair.getOne(), 
                                pair.getTwo(), 
                                importsBlock));
        exercises.forEach(ex -> writeExerciseJavaSource(ex, kataDir));

        KataMetadata metadata = ImmutableKataMetadata.builder()
                .name("no name")
                .description("no description")
                .exerciseMetaData(exercises.toMap(KataExercise::sourceFileName, KataExercise::className))
                .build();

        metadata.writeTo(kataDir.resolve(KATA_METADATA));
    }

    private JavaSourceParser parseKataSource(Path kataDir) throws Exception
    {
        File kataClassFile = findKataClass(kataDir);
        JavaSourceParser parser = new JavaSourceParser();
        parser.parse(readFile(kataClassFile));
        return parser;
    }
    
    private void writeExerciseJavaSource(KataExercise exercise, Path kataDir)
    {
        try
        {
            String classText = exercise.source();
            Path filePath = kataDir.resolve(SRC_DIR).resolve(exercise.sourceFileName());
            Files.write(filePath, classText.getBytes(), StandardOpenOption.CREATE_NEW);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String addImports(JavaFile javaFile, String importsBlock)
    {
        try
        {
            StringBuilder builder = new StringBuilder();
            javaFile.writeTo(builder);

            String text = builder.toString();
            int endOfPackageStatement = text.indexOf(";");
            String prefix = text.substring(0, endOfPackageStatement+1);
            String suffix = text.substring(endOfPackageStatement+1);
            return prefix + "\n" + importsBlock +  suffix;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private File findKataClass(Path kataDir) throws Exception
    {
        File[] kataClassFiles = kataDir.resolve(SRC_DIR).toFile().listFiles(f -> f.getName().endsWith("Kata.java"));
        if (kataClassFiles.length > 1)
        {
            throw new Exception("Found too many kata files");
        }
        return kataClassFiles[0];
    }

    private JavaFile buildExerciseJavaFile(String packageName, String exerciseClassName, String methodName, String methodBodyText)
    {
        AnnotationSpec junitTestAnnotation = AnnotationSpec.builder(Test.class)
                .build();

        MethodSpec methodSpec = MethodSpec.methodBuilder(methodName)
                .addAnnotation(junitTestAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .addCode(methodBodyText)
                .build();

        TypeSpec typeSpec = TypeSpec.classBuilder(exerciseClassName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpec)
                .build();

        return JavaFile.builder(packageName, typeSpec).build();
    }
}
