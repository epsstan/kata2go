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

import com.goldmansachs.kata2go.tools.domain.KataExercise;
import com.goldmansachs.kata2go.tools.store.KataStore;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Path("execute")
public class KataExecutionResource
{
    private final KataStore kataStore;
    private final Client client;
    @Context
    UriInfo uriInfo;

    private static final Logger LOGGER = LoggerFactory.getLogger(KataExecutionResource.class);

    public KataExecutionResource(KataStore kataStore, Client client)
    {
        this.kataStore = kataStore;
        this.client = client;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getKatas()
    {
        return kataStore.getAllKataIds();
    }

    @POST
    @Path("/kata/{kataId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addKata(
            @PathParam("kataId") String kataId,
            KataExercise kataExercise) throws Exception
    {
        LOGGER.info("Executing exercise : kata id={}", kataId);

        Optional<InputStream> kataStream = kataStore.fetch(kataId);
        if (kataStream.isPresent())
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        InputStream kataInputStream = kataStream.get();


        Entity<FormDataMultiPart> entity = makeMultiPartEntity(kataExercise, kataInputStream);
        Response response = client.target("fake").path("execute")
                .register(MultiPartFeature.class)
                .request()
                .post(entity);

        return Response.ok().build();
    }

    private Entity<FormDataMultiPart> makeMultiPartEntity(KataExercise kataExercise, InputStream inputStream)
    {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field("file", inputStream, MediaType.MULTIPART_FORM_DATA_TYPE);
        form.field("exercise", kataExercise, MediaType.APPLICATION_JSON_TYPE);
        return Entity.entity(form, form.getMediaType());
    }

}
