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

    /**
     * Converts all information which this provider stores into String object.
     */
    @Override
    public @NotNull String toString() {
        Set<Path> paths = new HashSet<>();
        paths.addAll(currentCommitEntities.keySet());
        paths.addAll(indexEntities.keySet());
        paths.addAll(workingDirectoryEntities.keySet());

        StringBuilder builder = new StringBuilder();

        builder.append("Changes to be committed:\n");
        for (Path path : paths) {
            FileStatistics statistics = getFileStatistics(path);

            if (statistics.isStagedForAddition()) {
                builder.append("new file: ").append(path.toString()).append('\n');
            }

            if (statistics.isStagedForModification()) {
                builder.append("modified: ").append(path.toString()).append('\n');
            }

            if (statistics.isStagedForDeletion()) {
                builder.append("deleted: ").append(path.toString()).append('\n');
            }
        }

        builder.append("\nChanges not staged for commit:\n");
        for (Path path : paths) {
            FileStatistics statistics = getFileStatistics(path);

            if (statistics.isUnstagedForModification()) {
                builder.append("modified: ").append(path.toString()).append('\n');
            }

            if (statistics.isUnstagedForDeletion()) {
                builder.append("deleted: ").append(path.toString()).append('\n');
            }
        }

        builder.append("\nUntracked files:\n");
        for (Path path : paths) {
            if (getFileStatistics(path).isUntracked()) {
                builder.append(path.toString());
                builder.append('\n');
            }
        }

        return builder.toString();
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
            return presentedInWorkingDirectory && !presentedInCurrentCommit && !presentedInIndex;
        }

        /**
         * Returns true if file was deleted and this action was registered in repository.
         */
        public boolean isStagedForDeletion() {
            return presentedInCurrentCommit && !presentedInIndex;
        }

        /**
         * Returns true if file was added and this action was registered in repository.
         */
        public boolean isStagedForAddition() {
            return !presentedInCurrentCommit && presentedInIndex;
        }

        /**
         * Returns true if file was modified and this action was registered in repository.
         */
        public boolean isStagedForModification() {
            return presentedInCurrentCommit && presentedInIndex && !equalInCurrentCommitAndIndex;
        }

        /**
         * Returns true if file was deleted and this action was not registered in repository.
         */
        public boolean isUnstagedForDeletion() {
            return !presentedInWorkingDirectory && presentedInIndex;
        }

        /**
         * Returns true if file was modified and this action was not registered in repository.
         */
        public boolean isUnstagedForModification() {
            return presentedInWorkingDirectory && presentedInIndex && !equalInIndexAndWorkingDirectory;
        }
    }
}
