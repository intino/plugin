package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.diagnostic.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tara.dsl.ProteoConstants.*;

public class ArtifactoryConnector {
    private static final Logger LOG = Logger.getInstance(ArtifactoryConnector.class.getName());

    private final String languageRepository;
    private final Map<String, String> releaseRepositories;
    private final String snapshotRepository;

    public ArtifactoryConnector(Map<String, String> releaseRepositories, String snapshotRepository, String languageRepository) {
        this.releaseRepositories = releaseRepositories;
        this.snapshotRepository = snapshotRepository;
        this.languageRepository = languageRepository;
    }

    public List<String> versions(String dsl) throws IOException {
        if (dsl.equals(PROTEO) || dsl.equals(VERSO)) return proteoVersions();
        URL url = new URL(languageRepository + "/" + "tara/dsl" + "/" + dsl + "/maven-metadata.xml");
        final String mavenMetadata = new String(read(url.openStream()).toByteArray());
        return extractVersions(mavenMetadata);
    }

    private List<String> proteoVersions() throws IOException {
        List<String> versions = new ArrayList<>();
        for (String repo : releaseRepositories.keySet()) {
            URL url = new URL(repo + "/" + PROTEO_GROUP_ID.replace(".", "/") + "/" + PROTEO_ARTIFACT_ID + "/maven-metadata.xml");
            final String mavenMetadata = new String(read(url.openStream()).toByteArray());
            versions.addAll(extractVersions(mavenMetadata));
        }

        URL url = new URL(snapshotRepository + "/" + PROTEO_GROUP_ID.replace(".", "/") + "/" + PROTEO_ARTIFACT_ID + "/maven-metadata.xml");
        final String mavenMetadata = new String(read(url.openStream()).toByteArray());
        versions.addAll(extractVersions(mavenMetadata));

        return versions;
    }

    private List<String> extractVersions(String metadata) {
        metadata = metadata.substring(metadata.indexOf("<versions>")).substring("<versions>".length() + 1);
        metadata = metadata.substring(0, metadata.indexOf("</versions>"));
        metadata = metadata.replace("<version>", "").replace("</version>", "");
        return Arrays.stream(metadata.trim().split("\n")).map(String::trim).collect(Collectors.toList());
    }

    private ByteArrayOutputStream read(InputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
            int n;
            while ((n = stream.read(byteChunk)) > 0)
                baos.write(byteChunk, 0, n);
        } finally {
            stream.close();
        }
        return baos;
    }

}
