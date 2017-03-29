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

package com.goldmansachs.kata2go.backend.app;

import com.goldmansachs.kata2go.backend.resource.KataResource;
import com.goldmansachs.kata2go.tools.processor.KataProcessor;
import com.goldmansachs.kata2go.tools.store.KataStore;
import com.goldmansachs.kata2go.tools.store.LocalDiskKataStore;
import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class BackendApplication extends Application<BackendConfiguration>
{
    public static void main(String[] args) throws Exception
    {
        new BackendApplication().run(args);
    }

    @Override
    public String getName()
    {
        return "kata2go-Backend";
    }

    @Override
    public void initialize(Bootstrap<BackendConfiguration> bootstrap)
    {
        super.initialize(bootstrap);
        bootstrap.addBundle(new MultiPartBundle());
    }

    @Override
    public void run(BackendConfiguration backendConfiguration, Environment environment) throws Exception
    {
        String kataStoreBaseDirectory = backendConfiguration.getKataStoreBaseDirectory();
        String kataStagingBaseDirectory = backendConfiguration.getKataStagingBaseDirectory();

        final KataStore kataStore = new LocalDiskKataStore(kataStoreBaseDirectory);
        final KataProcessor kataProcessor = new KataProcessor(kataStagingBaseDirectory);
        final KataResource kataResource = new KataResource(kataStore, kataProcessor);
        environment.jersey().register(kataResource);

        final BackendHealthCheck BackendHealthCheck = new BackendHealthCheck(kataStoreBaseDirectory);
        environment.healthChecks().register("kata", BackendHealthCheck);

    }
}
