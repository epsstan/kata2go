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

package com.goldmansachs.kata2go.backend.resource;

import com.goldmansachs.kata2go.tools.domain.KataDefinition;
import com.goldmansachs.kata2go.tools.domain.KataSummary;
import com.goldmansachs.kata2go.tools.processor.KataProcessor;
import com.goldmansachs.kata2go.tools.store.KataStore;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Path("kata")
public class KataResource
{
    private final KataStore kataStore;
    private final KataProcessor kataProcessor;
    @Context
    UriInfo uriInfo;

    private static final Logger LOGGER = LoggerFactory.getLogger(KataResource.class);

    public KataResource(KataStore kataStore, KataProcessor kataProcessor)
    {
        this.kataStore = kataStore;
        this.kataProcessor = kataProcessor;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getKatas()
    {
        return kataStore.getAllKataIds();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addKata(
            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) throws Exception
    {
        LOGGER.info("Adding kata");
        java.nio.file.Path processedTarGz = kataProcessor.process(inputStream);
        String id = kataStore.store(processedTarGz);
        URI uri = uriInfo.getAbsolutePathBuilder().path(id).build();
        LOGGER.info("Added kata with id {}", id);
        return Response.created(uri).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKata(@PathParam("id") String id) throws Exception
    {
        Optional<KataDefinition> definition = kataStore.getKataDefinition(id);
        if (!definition.isPresent())
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        KataSummary summary = definition.get().toSummary();
        return Response.ok(summary).build();
    }
}
