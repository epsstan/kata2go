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

package com.goldmansachs.kata2go.executor

import com.goldmansachs.kata2go.executor.domain.ExecutorResult
import com.goldmansachs.kata2go.executor.resource.KataExecutionResource
import com.goldmansachs.kata2go.tools.domain.ImmutableKataExercise
import com.goldmansachs.kata2go.tools.store.KataStore
import com.goldmansachs.kata2go.tools.store.LocalDiskKataStore
import io.dropwizard.testing.junit.ResourceTestRule
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import org.glassfish.jersey.media.multipart.FormDataMultiPart
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart
import org.junit.ClassRule
import org.junit.Test

import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import java.nio.file.Files
import java.nio.file.Path

class TestKataExecution {

    private static final Path TEMP_DIR = Files.createTempDirectory(null);

    private static final KataStore kataStore = new LocalDiskKataStore(TEMP_DIR.toFile().absolutePath);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(MultiPartFeature.class)
            .addResource(new KataExecutionResource(kataStore))
            .build();

    private File loadFileFromClassPath(String name) {
        def resource = TestKataExecution.class.getResource(name)
        return new File(resource.toURI())
    }

    @Test
    public void executionSuccessful() {
        def kataTarGzFile = loadFileFromClassPath("/exploded-hello-world-kata.tar.gz")

        FileDataBodyPart filePart = new FileDataBodyPart("kataFile", kataTarGzFile);
        filePart.setContentDisposition(
                FormDataContentDisposition.name("kataFile")
                        .fileName(kataTarGzFile.getName()).build());

        def userSource = """
package com.goo;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.Test;

public class HelloWorldKata_ex1 {
  @Test
  public void ex1() {
    assertEquals("hello",  "hello");
  }
}
"""
        def kataExercise = ImmutableKataExercise.builder()
                .sourceFileName("HelloWorldKata_ex1.java")
                .className("com.goo.HelloWorldKata_ex1")
                .source(userSource)
                .build();

        def form = new FormDataMultiPart()
        form.field("file", kataTarGzFile, MediaType.MULTIPART_FORM_DATA_TYPE)
        form.field("exercise", kataExercise, MediaType.APPLICATION_JSON_TYPE)

        Entity<Void> e = Entity.entity(form, form.getMediaType())

        Response response = resources.target("/execute")
                .register(MultiPartFeature.class)
                .request()
                .post(e)

        assert response.status == 200

        ExecutorResult executorResult = response.readEntity(ExecutorResult.class)

        assert executorResult.compilationResult().successful()
        assert executorResult.runResult().successful()

    }

}