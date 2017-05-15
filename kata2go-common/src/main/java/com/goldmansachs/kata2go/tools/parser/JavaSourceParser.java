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

package com.goldmansachs.kata2go.tools.parser;

import com.goldmansachs.kata2go.antlr.JavaLexer;
import com.goldmansachs.kata2go.antlr.JavaParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;

/*
    Parser for Java source files.
 */

public class JavaSourceParser
{
    private Listener extractor;

    public void parse(String javaSource)
    {
        CharStream stream = CharStreams.fromString(javaSource);
        JavaLexer lexer = new JavaLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        JavaParser.CompilationUnitContext tree = parser.compilationUnit();

        this.extractor = new Listener(javaSource);
        ParseTreeWalker.DEFAULT.walk(extractor, tree);
    }

    public ImmutableMap<String, String> getMethods()
    {
        return extractor.methods.toImmutable();
    }

    public String getKataClassName()
    {
        return extractor.className;
    }

    public String getKataPackageName()
    {
        return extractor.packageName;
    }

    public String getKataFullClassName()
    {
        return extractor.packageName + "." + extractor.className;
    }

    public ImmutableList<String> getImports() {
        return extractor.imports.toImmutable();
    }

    static class Listener extends AbstractJavaListener
    {
        private MutableMap<String, String> methods = Maps.mutable.empty();
        private String javaSource;
        private String packageName = "";
        private String className;
        private MutableList<String> imports = Lists.mutable.empty();

        public Listener(String javaSource)
        {
            this.javaSource = javaSource;
        }

        @Override
        public void enterPackageDeclaration(@NotNull JavaParser.PackageDeclarationContext ctx)
        {
            this.packageName = ctx.qualifiedName().getText();
        }

        @Override
        public void enterClassDeclaration(@NotNull JavaParser.ClassDeclarationContext ctx)
        {
            this.className = ctx.Identifier().getText();
        }

        @Override
        public void enterMethodBody(@NotNull JavaParser.MethodBodyContext ctx)
        {
            JavaParser.MethodDeclarationContext methodDeclarationContext = (JavaParser.MethodDeclarationContext) ctx.getParent();
            String methodName = methodDeclarationContext.Identifier().getText();

            JavaParser.BlockContext block = (JavaParser.BlockContext)ctx.getChild(0);

            /*
            JavaParser.BlockStatementContext statementContext = (JavaParser.BlockStatementContext) block.getChild(1);
            int start = statementContext.getStart().getStartIndex();
            int stop = statementContext.getStop().getStopIndex();
            */

            /*
                Antlr by default skips comments in the Java source.
                It does not seem to be straightforward to parse while preserving comments.
                Hence the hack below.
             */

            //skip past the method's open {
            int start = block.getStart().getStartIndex() + 1;
            //skip to the char just before the method's closing }
            int stop = block.getStop().getStopIndex() - 1;
            String methodBody = javaSource.substring(start, stop + 1);

            methods.put(methodName, methodBody);
        }

        @Override
        public void enterImportDeclaration(@NotNull JavaParser.ImportDeclarationContext ctx)
        {
            int start = ctx.getStart().getStartIndex();
            int stop = ctx.getStop().getStopIndex();
            imports.add(javaSource.substring(start, stop+1));
        }
    }
}
