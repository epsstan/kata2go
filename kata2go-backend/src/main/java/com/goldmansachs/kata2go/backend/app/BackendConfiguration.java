package com.goldmansachs.kata2go.backend.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class BackendConfiguration extends Configuration
{
    @NotEmpty
    private String kataStoreBaseDirectory;

    @NotEmpty
    private String kataStagingBaseDirectory;

    @JsonProperty
    public String getKataStoreBaseDirectory()
    {
        return kataStoreBaseDirectory;
    }

    @JsonProperty
    public void setKataStoreBaseDirectory(String kataStoreBaseDirectory)
    {
        this.kataStoreBaseDirectory = kataStoreBaseDirectory;
    }

    @JsonProperty
    public String getKataStagingBaseDirectory()
    {
        return kataStagingBaseDirectory;
    }

    @JsonProperty
    public void setKataStagingBaseDirectory(String kataStagingBaseDirectory)
    {
        this.kataStagingBaseDirectory = kataStagingBaseDirectory;
    }
}
