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

import javax.tools.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ByteJavaFileManager implements JavaFileManager
{
    private final DiagnosticCollector diagnosticCollector = new DiagnosticCollector();
    private final Map<String, ByteArrayFileObject> compiledClasses = new HashMap<>();
    private final StandardJavaFileManager delegate;

    public ByteJavaFileManager(JavaCompiler javaCompiler)
    {
        this.delegate = javaCompiler.getStandardFileManager(diagnosticCollector, null, null);
    }

    public ByteJavaFileManager(StandardJavaFileManager delegate)
    {
        this.delegate = delegate;
    }


    @Override
    public ClassLoader getClassLoader(Location location)
    {
        return delegate.getClassLoader(location);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException
    {
        return delegate.list(location, packageName, kinds, recurse);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file)
    {
        return delegate.inferBinaryName(location, file);
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b)
    {
        return delegate.isSameFile(a, b);
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining)
    {
        return delegate.handleOption(current, remaining);
    }

    @Override
    public boolean hasLocation(Location location)
    {
        return delegate.hasLocation(location);
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException
    {
        return delegate.getJavaFileForInput(location, className, kind);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException
    {
        try
        {
            ByteArrayFileObject byteArrayFileObject = new ByteArrayFileObject(className, kind);
            compiledClasses.put(className, byteArrayFileObject);
            return byteArrayFileObject;
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException
    {
        return delegate.getFileForInput(location, packageName, relativeName);
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException
    {
        return delegate.getFileForOutput(location, packageName, relativeName, sibling);
    }

    @Override
    public void flush() throws IOException
    {

    }

    @Override
    public void close() throws IOException
    {

    }

    @Override
    public int isSupportedOption(String option)
    {
        return delegate.isSupportedOption(option);
    }

    public DiagnosticCollector getDiagnostics()
    {
        return diagnosticCollector;
    }

    public Map<String, ByteArrayFileObject> getCompiledClasses()
    {
        return compiledClasses;
    }
}
