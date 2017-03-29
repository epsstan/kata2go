package com.goldmansachs.kata2go.backend

import com.fasterxml.jackson.core.JsonProcessingException
import com.goldmansachs.kata2go.backend.resource.KataResource
import com.goldmansachs.kata2go.tools.domain.ImmutableKataSummary
import com.goldmansachs.kata2go.tools.jackson.JsonUtils
import com.goldmansachs.kata2go.tools.processor.KataProcessor
import com.goldmansachs.kata2go.tools.store.LocalDiskKataStore
import com.goldmansachs.kata2go.tools.testutils.TestLocalDiskKataStore
import com.goldmansachs.kata2go.tools.utils.FileUtils
import io.dropwizard.testing.junit.ResourceTestRule
import org.glassfish.jersey.media.multipart.FormDataMultiPart
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.junit.ClassRule
import org.junit.Test

import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import java.nio.file.Files
import java.nio.file.Path

class TestKataResource {

    private static Path kataStoreDir = Files.createTempDirectory(null);
    private static TestLocalDiskKataStore testkataStore = new TestLocalDiskKataStore(kataStoreDir.toFile().getAbsolutePath());
    private static LocalDiskKataStore kataStore = testkataStore;

    private static Path processorTempDir = Files.createTempDirectory(null);
    private static KataProcessor kataProcessor = new KataProcessor(processorTempDir)

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(MultiPartFeature.class)
            .addResource(new KataResource(kataStore, kataProcessor))
            .build()

    @Test
    public void addKata() throws JsonProcessingException {
        def kataTarGzFile = FileUtils.loadFileFromClassPath("/hello-world-kata.tar.gz")

        def form = new FormDataMultiPart()
        form.field("file", kataTarGzFile, MediaType.MULTIPART_FORM_DATA_TYPE)
        Entity<Void> entity = Entity.entity(form, form.getMediaType())

        Response response = resources.target("/kata")
                .register(MultiPartFeature.class)
                .request()
                .post(entity)

        assert 201 == response.status
        def kataId = response.location.toString().split("/").last()
        def kataNames = resources.target("/kata").request().get().readEntity(List.class)
        assert kataNames == [kataId]
    }

    @Test
    public void getKataDetails() throws JsonProcessingException {

        testkataStore.stageKata("stored-kata.tar.gz", "id2");

        def response = resources.target("/kata/id2").request().get()

        assert response.status == 200

        def summary = JsonUtils.prettyPrinter().writeValueAsString(response.readEntity(ImmutableKataSummary.class))

        def expectedSummary = """{
  "name" : "no name",
  "description" : "no description",
  "exercises" : [ {
    "sourceFileName" : "HelloWorldKata_ex2.java",
    "className" : "com.goo.HelloWorldKata_ex2",
    "source" : "package com.goo;\\nimport org.junit.ClassRule;\\nimport org.junit.Test;\\nimport static org.junit.Assert.*;\\n\\nimport org.junit.Test;\\n\\npublic class HelloWorldKata_ex2 {\\n  @Test\\n  public void ex2() {\\n\\n            // assertEquals(\\"world\\", ...);\\n        }\\n}\\n"
  }, {
    "sourceFileName" : "HelloWorldKata_ex1.java",
    "className" : "com.goo.HelloWorldKata_ex1",
    "source" : "package com.goo;\\nimport org.junit.ClassRule;\\nimport org.junit.Test;\\nimport static org.junit.Assert.*;\\n\\nimport org.junit.Test;\\n\\npublic class HelloWorldKata_ex1 {\\n  @Test\\n  public void ex1() {\\n\\n           // assertEquals(\\"hello\\",  ...);\\n        }\\n}\\n"
  } ],
  "sources" : {
    "HelloWorldKata.java" : "package com.goo;\\n\\nimport org.junit.ClassRule;\\nimport org.junit.Test;\\nimport static org.junit.Assert.*;\\n\\npublic class HelloWorldKata\\n{\\n    @Test\\n    public void ex1()\\n    {\\n       // assertEquals(\\"hello\\",  ...);\\n    }\\n\\n    @Test\\n    public void ex2()\\n    {\\n        // assertEquals(\\"world\\", ...);\\n    }\\n}\\n"
  }
}"""
        assert expectedSummary == summary
    }

    @Test
    public void getKataThatDoesNotExist() throws JsonProcessingException {

        def response = resources.target("/kata/" + System.currentTimeMillis()).request().get()
        assert response.status == 404
    }
}