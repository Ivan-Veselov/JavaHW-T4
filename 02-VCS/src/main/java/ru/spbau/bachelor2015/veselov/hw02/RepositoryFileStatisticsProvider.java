package ru.spbau.bachelor2015.veselov.hw02;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

/**
 * Repository statistics provider which can create file statistics for a specified file. This statistic contains
 * information about current file state and how repository treats this state.
 */
public class RepositoryFileStatisticsProvider {
    private final @NotNull Map<Path, SHA1Hash> currentCommitEntities;

    private final @NotNull Map<Path, SHA1Hash> indexEntities;

    private final @NotNull Map<Path, SHA1Hash> workingDirectoryEntities;

    RepositoryFileStatisticsProvider(final @NotNull List<FileEntity> commitEntitiesList,
                                     final @NotNull List<FileEntity> indexEntitiesList,
                                     final @NotNull List<FileEntity> workingDirectoryEntitiesList) {
        currentCommitEntities = getMap(commitEntitiesList);
        indexEntities = getMap(indexEntitiesList);
        workingDirectoryEntities = getMap(workingDirectoryEntitiesList);
    }

    /**
     * Returns a file statistics object for a given file.
     *
     * @param path a path to file.
     */
    public @NotNull FileStatistics getFileStatistics(final @NotNull Path path) {
        return new FileStatistics(this, path);
    }

    private @NotNull Map<Path, SHA1Hash> getMap(final @NotNull List<FileEntity> list) {
        Map<Path, SHA1Hash> hashMap = new HashMap<>();

        for (FileEntity entity : list) {
            hashMap.put(entity.getPathToFile(), entity.getContentHash());
        }

        return hashMap;
    }

    /**
     * Objects of this class holds a repository statistics for particular file.
     */
    public static class FileStatistics {
        public final boolean presentedInCurrentCommit;

        public final boolean presentedInIndex;

        public final boolean presentedInWorkingDirectory;

        public final boolean equalInCurrentCommitAndIndex;

        public final boolean equalInCurrentCommitAndWorkingDirectory;

        public final boolean equalInIndexAndWorkingDirectory;

        private FileStatistics(final @NotNull RepositoryFileStatisticsProvider provider, final @NotNull Path path) {
            presentedInCurrentCommit = provider.currentCommitEntities.containsKey(path);
            presentedInIndex = provider.indexEntities.containsKey(path);
            presentedInWorkingDirectory = provider.workingDirectoryEntities.containsKey(path);

            if (presentedInCurrentCommit) {
                equalInCurrentCommitAndIndex = provider.currentCommitEntities.get(path).equals(
                                                            provider.indexEntities.get(path));

                equalInCurrentCommitAndWorkingDirectory = provider.currentCommitEntities.get(path).equals(
                                                                    provider.workingDirectoryEntities.get(path));
            } else {
                equalInCurrentCommitAndIndex = false;
                equalInCurrentCommitAndWorkingDirectory = false;
            }

            if (presentedInIndex) {
                equalInIndexAndWorkingDirectory = provider.indexEntities.get(path).equals(
                                                            provider.workingDirectoryEntities.get(path));
            } else {
                equalInIndexAndWorkingDirectory = false;
            }
        }

        /**
         * Returns true if this file isn't presented in current commit and isn't added to index.
         */
        public boolean isUntracked() {
            return !presentedInCurrentCommit && !presentedInIndex;
        }
    }
}
