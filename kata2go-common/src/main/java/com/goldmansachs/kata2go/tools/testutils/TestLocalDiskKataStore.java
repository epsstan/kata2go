package com.goldmansachs.kata2go.tools.testutils;

import com.goldmansachs.kata2go.tools.store.LocalDiskKataStore;
import com.goldmansachs.kata2go.tools.utils.TarGz;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class TestLocalDiskKataStore extends LocalDiskKataStore
{
    public TestLocalDiskKataStore(String kataStorBaseDirectory)
    {
        super(kataStorBaseDirectory);
    }

    public void stageKata(String kataTarGz, String id) throws Exception
    {
        Path kataTarGzPath = loadFileFromClassPath("/" + kataTarGz).toPath();
        Path kataDir = kataStoreBaseDirectory.toPath().resolve(id);
        kataDir.toFile().mkdirs();
        TarGz.decompress(kataTarGzPath, kataDir);

    }

    private File loadFileFromClassPath(String name) throws URISyntaxException
    {
        URL resource = TestLocalDiskKataStore.class.getResource(name);
        return new File(resource.toURI());
    }
}
