/*
 Copyright 2016 Goldman Sachs.
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

package com.goldmansachs.kata2go.tools.compiler;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.utility.ListIterate;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*
    Wrapper around the Java compiler API to compile Java sources.
 */
public class KataCompiler
{
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public CompilationResult compile(ImmutableMap<String, String> sources, List<String> classPath)
    {
        RichIterable<StringSourceFileObject> stringSources = sources
                .keyValuesView()
                .collect(pair -> StringSourceFileObject.make(pair.getOne(), pair.getTwo()));

        ByteJavaFileManager fileManager = new ByteJavaFileManager(compiler);
        DiagnosticCollector diagnosticCollector = fileManager.getDiagnostics();

        ImmutableList<String> options = null;

        if (!classPath.isEmpty())
        {
            String fullClassPath = classPath.stream().collect(Collectors.joining(":"));
            options = Lists.immutable.of("-classpath", fullClassPath);
        }

        JavaCompiler.CompilationTask compilationTask = compiler.getTask(null, fileManager, diagnosticCollector,  options, null, stringSources);

        if (!compilationTask.call())
        {
            Function<Diagnostic<?>, String> makeString = d -> d.toString();
            MutableList errors = ListIterate.collect(diagnosticCollector.getDiagnostics(), makeString);

            return ImmutableCompilationResult.builder()
                    .successful(false)
                    .errors(errors)
                    .build();
        }
        return ImmutableCompilationResult.builder()
                .successful(true)
                .compiledClasses(fileManager.getCompiledClasses())
                .build();
    }

    public CompilationResult compile(ImmutableMap<String, String> sources)
    {
        return compile(sources, Collections.EMPTY_LIST);
    }
}
