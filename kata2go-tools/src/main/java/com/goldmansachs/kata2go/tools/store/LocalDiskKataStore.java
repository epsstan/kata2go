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

package com.goldmansachs.kata2go.tools.store;

import com.goldmansachs.kata2go.tools.domain.*;
import com.goldmansachs.kata2go.tools.utils.FileUtils;
import com.goldmansachs.kata2go.tools.utils.TarGz;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.partition.list.PartitionFastList;
import org.eclipse.collections.impl.utility.ArrayIterate;
import org.eclipse.collections.impl.utility.ListIterate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.eclipse.collections.impl.utility.ArrayIterate.collect;

public class LocalDiskKataStore implements KataStore
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDiskKataStore.class);
    public static final String KATA_METADATA = "kata.metadata";
    public static final String KATA_TAR_GZ = "kata-exploded.tar.gz";
    public static final String SRC_DIR = "src";
    public static final String JARS_DIR = "jars";

    protected final File kataStoreBaseDirectory;

    public LocalDiskKataStore(String kataStorBaseDirectory)
    {
        this.kataStoreBaseDirectory = new File(kataStorBaseDirectory);
    }

    @Override
    public List<String> getAllKataIds()
    {
        File[] kataDirs = kataStoreBaseDirectory.listFiles(File::isDirectory);
        if (kataDirs == null)
        {
            return Lists.immutable.<String>empty().toList();
        }
        return collect(kataDirs, File::getName).toList();
    }

    @Override
    public String store(Path kataTarGz) throws Exception
    {
        String id = generateKataId();
        Path kataStoreDir = createDir(id);
        TarGz.decompress(kataTarGz, kataStoreDir, true);
        Files.copy(kataTarGz, kataStoreDir.resolve(KATA_TAR_GZ));
        return id;
    }

    @Override
    public String store(InputStream kataTarGzStream) throws Exception
    {
        String id = generateKataId();
        Path kataStoreDir = createDir(id);
        Path kataTarGz = kataStoreDir.resolve(KATA_TAR_GZ);
        TarGz.write(kataTarGzStream, kataTarGz);
        Files.copy(kataTarGz, kataStoreDir);
        return id;
    }

    @Override
    public Optional<InputStream> fetch(String kataId) throws Exception
    {
        File kataStoreDir = getKataStoreDir(kataId);
        if (!kataStoreDir.exists())
        {
            return Optional.empty();
        }
        Path kataTarGz = kataStoreDir.toPath().resolve(KATA_TAR_GZ);
        return Optional.of(new FileInputStream(kataTarGz.toFile()));
    }

    private Path createDir(String id) throws IOException
    {
        String dirName = String.valueOf(id);
        Path dirPath = kataStoreBaseDirectory.toPath().resolve(String.valueOf(id));
        if (dirPath.toFile().exists())
        {
            throw new FileAlreadyExistsException(dirName);
        }
        Files.createDirectory(dirPath);
        return dirPath;
    }

    private KataExercise makeExercise(File file, KataMetadata kataMetadata)
    {
        return ImmutableKataExercise.builder()
                .sourceFileName(file.getName())
                .className(kataMetadata.exerciseMetaData().get(file.getName()))
                .source(FileUtils.readFile(file))
                .build();
    }

    @Override
    public Optional<KataExercise> getKataExercise(String kataId, String kataExerciseId) throws Exception
    {
        Optional<KataDefinition> definition = getKataDefinition(kataId);
        if (!definition.isPresent())
        {
            return Optional.empty();
        }
        KataDefinition kataDefinition = definition.get();
        Optional<KataExercise> exDefinition = ListIterate
                .detectOptional(kataDefinition.exercises(),
                        ex -> ex.sourceFileName().equals(kataExerciseId));
        if (!exDefinition.isPresent())
        {
            return Optional.empty();
        }

        return exDefinition;
    }


    private File getKataStoreDir(String kataId)
    {
        File kataDirectory = kataStoreBaseDirectory.toPath().resolve(kataId).toFile();
        return kataDirectory;
    }

    @Override
    public Optional<KataDefinition> getKataDefinition(String kataId) throws Exception
    {
        File kataDirectory = getKataStoreDir(kataId);
        if (!kataDirectory.exists())
        {
            return Optional.empty();
        }

        KataMetadata kataMetadata = KataMetadata.readFrom(kataDirectory.toPath().resolve(KATA_METADATA));

        File[] allSources = kataDirectory.toPath().resolve(SRC_DIR).toFile().listFiles();
        PartitionFastList<File> partition = ArrayIterate.partition(allSources, file -> file.getName().matches(".*ex\\d+\\.java"));
        MutableList<File> exerciseSourceFiles = partition.getSelected();
        MutableList<File> otherSourceFiles = partition.getRejected();

        MutableMap<String, KataExercise> kataExercises = exerciseSourceFiles
                .collect(file -> makeExercise(file, kataMetadata))
                .zipWithIndex().toMap(pair -> String.valueOf(pair.getTwo()), pair -> pair.getOne());

        MutableMap<String, String> otherSources = otherSourceFiles
                .toMap(File::getName, FileUtils::readFile);

        ImmutableList<String> jars = collect(
                kataDirectory.toPath().resolve(JARS_DIR).toFile().listFiles(),
                File::getAbsolutePath
        ).toImmutable();

        ImmutableKataDefinition definition = ImmutableKataDefinition.builder()
                .metadata(kataMetadata)
                .jars(jars)
                .exercises(kataExercises)
                .sources(otherSources)
                .build();

        return Optional.of(definition);
    }

    /*
        Needs to be synchronized if id generation maintains state (either in this class or outside)/
     */
    @Override
    public String generateKataId()
    {
        return UUID.randomUUID().toString();
    }
}
