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

package com.goldmansachs.kata2go.executor.resource;

import com.goldmansachs.kata2go.executor.domain.ExecutorResult;
import com.goldmansachs.kata2go.executor.domain.ImmutableExecutorResult;
import com.goldmansachs.kata2go.executor.domain.ImmutableRunResult;
import com.goldmansachs.kata2go.tools.compiler.CompilationResult;
import com.goldmansachs.kata2go.tools.compiler.KataCompiler;
import com.goldmansachs.kata2go.tools.domain.KataDefinition;
import com.goldmansachs.kata2go.tools.domain.KataExercise;
import com.goldmansachs.kata2go.tools.runner.KataJunitTestResult;
import com.goldmansachs.kata2go.tools.runner.StdOutStdErrCapturingKataRunner;
import com.goldmansachs.kata2go.tools.store.KataStore;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.Optional;

@Path("execute")
@Produces(MediaType.APPLICATION_JSON)
public class KataExecutionResource
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KataExecutionResource.class);

    private final KataStore kataStore;

    public KataExecutionResource(KataStore kataStore)
    {
        this.kataStore = kataStore;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public ExecutorResult execute(
            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("exercise") KataExercise exercise) throws Exception
    {
        String kataId = kataStore.store(inputStream);
        Optional<KataDefinition> kataDetails = kataStore.getKataDefinition(kataId);
        return compileAndExecute(kataDetails.get(), exercise);
    }

    private ExecutorResult compileAndExecute(KataDefinition kata, KataExercise exercise)
    {
        //clobber the source exercise with that supplied by the user
        MutableMap<String, String> sourcesForExercise = Maps.mutable.empty();
        sourcesForExercise.putAll(kata.sources());
        sourcesForExercise.put(exercise.sourceFileName(), exercise.source());

        CompilationResult compilationResult = new KataCompiler()
                .compile(sourcesForExercise.toImmutable(), kata.jars());
        if (!compilationResult.successful())
        {
            ImmutableRunResult runResult = ImmutableRunResult.builder()
                    .successful(false)
                    .build();

            return ImmutableExecutorResult.builder()
                    .compilationResult(compilationResult)
                    .runResult(runResult)
                    .build();
        }

        KataJunitTestResult junitResult = StdOutStdErrCapturingKataRunner.run(compilationResult.compiledClasses(), exercise.className());

        ImmutableRunResult runResult = ImmutableRunResult.builder()
                .successful(junitResult.junitResult().wasSuccessful())
                .stdout(Lists.immutable.of(junitResult.stdOut()))
                .stderr(Lists.immutable.of(junitResult.stdErr().orElse("")))
                .build();

        return ImmutableExecutorResult.builder()
                .compilationResult(compilationResult)
                .runResult(runResult)
                .build();
    }
}
