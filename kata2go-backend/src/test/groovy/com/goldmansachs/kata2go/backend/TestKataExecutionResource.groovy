package com.goldmansachs.kata2go.backend

import com.fasterxml.jackson.core.JsonProcessingException
import com.goldmansachs.kata2go.backend.resource.KataExecutionResource
import com.goldmansachs.kata2go.tools.domain.ImmutableKataExercise
import com.goldmansachs.kata2go.tools.store.LocalDiskKataStore
import com.goldmansachs.kata2go.tools.testutils.TestLocalDiskKataStore
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.testing.junit.ResourceTestRule
import org.junit.ClassRule
import org.junit.Test

import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import java.nio.file.Files
import java.nio.file.Path

class TestKataExecutionResource {

    private static Path kataStoreDir = Files.createTempDirectory(null);
    private static TestLocalDiskKataStore testkataStore = new TestLocalDiskKataStore(kataStoreDir.toFile().getAbsolutePath());
    private static LocalDiskKataStore kataStore = testkataStore;

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new KataExecutionResource(kataStore, new JerseyClientBuilder().name("test")))
            .build()

    @Test
    public void test() throws JsonProcessingException {

        def ex = ImmutableKataExercise.builder()
        .sourceFileName("a")
        .source("a")
        .className("a")
        .build();

        Response response = resources.target("/execute/kata/1")
                .request()
                .post(Entity.entity(ex, MediaType.APPLICATION_JSON_TYPE))

        println response.status

    }
}