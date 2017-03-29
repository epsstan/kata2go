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

package com.goldmansachs.kata2go.executor.app;

import com.goldmansachs.kata2go.executor.resource.KataExecutionResource;
import com.goldmansachs.kata2go.tools.store.KataStore;
import com.goldmansachs.kata2go.tools.store.LocalDiskKataStore;
import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class ExecutorApplication extends Application<ExecutorConfiguration>
{
    public static void main(String[] args) throws Exception
    {
        new ExecutorApplication().run(args);
    }

    @Override
    public String getName()
    {
        return "kata2go-executor";
    }

    @Override
    public void initialize(Bootstrap<ExecutorConfiguration> bootstrap)
    {
        super.initialize(bootstrap);
        bootstrap.addBundle(new MultiPartBundle());
    }

    @Override
    public void run(ExecutorConfiguration executorConfiguration, Environment environment) throws Exception
    {
        String kataStoreBaseDirectory = executorConfiguration.getKataStoreBaseDirectory();
        KataStore kataStore = new LocalDiskKataStore(kataStoreBaseDirectory);

        final ExecutorHealthCheck executorHealthCheck = new ExecutorHealthCheck();
        final KataExecutionResource kataExecutionResource = new KataExecutionResource(kataStore);

        environment.healthChecks().register("kata", executorHealthCheck);
        environment.jersey().register(kataExecutionResource);
    }
}
