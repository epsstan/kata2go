package com.goldmansachs.kata2go.tools.store;

import com.goldmansachs.kata2go.tools.domain.KataDefinition;
import com.goldmansachs.kata2go.tools.domain.KataExercise;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface KataStore
{
    List<String> getAllKataIds();

    String store(Path kataTarGz) throws Exception;

    String store(InputStream kataTarGzStream) throws Exception;

    Optional<InputStream> fetch(String kataId) throws Exception;

    Optional<KataDefinition> getKataDefinition(String kataId) throws Exception;

    Optional<KataExercise> getKataExercise(String kataId, String id) throws Exception;

    /*
        Needs to be synchronized if id generation maintains state (either in this class or outside)/
    */
    String generateKataId();
}
