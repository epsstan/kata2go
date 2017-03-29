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

package com.goldmansachs.kata2go.backend.app;

import com.codahale.metrics.health.HealthCheck;
import com.goldmansachs.kata2go.tools.store.LocalDiskKataStore;

import java.util.List;

public class BackendHealthCheck extends HealthCheck
{
    private final String kataStoreBaseDirectory;

    public BackendHealthCheck(String kataStoreBaseDirectory)
    {
        this.kataStoreBaseDirectory = kataStoreBaseDirectory;
    }

    @Override
    protected Result check() throws Exception
    {
        try
        {
            LocalDiskKataStore kataStore = new LocalDiskKataStore(kataStoreBaseDirectory);
            List<String> allKataIds = kataStore.getAllKataIds();
            if (allKataIds.isEmpty())
            {
                return Result.unhealthy("No katas found");
            }
            return Result.healthy("%d katas found", allKataIds.size());
        }
        catch (Exception e)
        {
            return Result.unhealthy(e);
        }
    }
}
