package ru.spbau.bachelor2015.veselov.hw02;

import org.apache.commons.io.FileUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// TODO: reconsider tests one more time
public class RepositoryTest {
    @Rule
    public final @NotNull TemporaryFolder rootDirectory = new TemporaryFolder();

    // TODO: Maybe add some kind of a special rule
    private VCSManager.Repository repository;

    @Before
    public void initializeEnvironment() throws Exception {
        repository = VCSManager.initializeVCS(rootDirectory.getRoot().toPath());
    }

    @Test
    public void blobConstruction() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        Files.write(pathToFile, new byte[] {1, 2, 3});

        VCSManager.Repository.Blob blob = repository.new Blob(pathToFile);

        // TODO: Find or write matcher
        assertThat(FileUtils.contentEquals(pathToFile.toFile(), blob.getPathInStorage().toFile()), is(true));
    }

    @Test(expected = RegularFileExpected.class)
    public void blobConstructionFromFolder() throws Exception {
        File folder = rootDirectory.newFolder();

        repository.new Blob(folder.toPath());
    }

    @Test
    public void multipleBlobsForSingleFile() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        Files.write(pathToFile, new byte[] {1, 2, 3});

        VCSManager.Repository.Blob blob1 = repository.new Blob(pathToFile);
        VCSManager.Repository.Blob blob2 = repository.new Blob(pathToFile);

        assertThat(blob2, is(similarTo(blob1)));
        // TODO: Find or write matcher
        assertThat(FileUtils.contentEquals(pathToFile.toFile(), blob2.getPathInStorage().toFile()), is(true));
        assertFilesInFolder(repository.getObjectsDirectory(), 1);
    }

    @Test
    public void blobConstructionFromHash() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        VCSManager.Repository.Blob blob1 = repository.new Blob(pathToFile);

        VCSManager.Repository.Blob blob2 = repository.new Blob(blob1.getSha1Hash());

        assertThat(blob2, is(similarTo(blob1)));
        assertFilesInFolder(repository.getObjectsDirectory(), 1);
    }

    @Test(expected = NoSuchElement.class)
    public void blobConstructionFromNonExistingHash() throws Exception {
        repository.new Blob("hash");
    }

    @Test(expected = FileFromWorkingDirectoryExpected.class)
    public void blobConstructionFromVCSFile() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        VCSManager.Repository.Blob blob = repository.new Blob(pathToFile);

        repository.new Blob(blob.getPathInStorage());
    }

    @Test
    public void treeConstruction() throws Exception {
        repository.new Tree(Collections.emptyList(),
                            Collections.singletonList(mockedNamed(mockedBlob("hash"), "name")));

        assertFilesInFolder(repository.getObjectsDirectory(), 1);
    }

    @Test
    public void multipleTreesForSingleFolder() throws Exception {
        repository.new Tree(Collections.emptyList(), Collections.emptyList());
        repository.new Tree(Collections.emptyList(), Collections.emptyList());

        assertFilesInFolder(repository.getObjectsDirectory(), 1);
    }

    @Test(expected = NamesContainsDuplicates.class)
    public void testTreeConstructionWithNameDuplicates() throws Exception {
        final String name = "name";

        repository.new Tree(Collections.emptyList(),
                Arrays.asList(mockedNamed(mockedBlob("0"), name), mockedNamed(mockedBlob("1"), name)));
    }

    @Test
    public void treeConstructionFromHash() throws Exception {
        VCSManager.Repository.Tree tree1 = repository.new Tree(Collections.emptyList(), Collections.emptyList());
        VCSManager.Repository.Tree tree2 = repository.new Tree(tree1.getSha1Hash());

        assertThat(tree2, is(similarTo(tree1)));
        assertFilesInFolder(repository.getObjectsDirectory(), 1);
    }

    @Test(expected = NoSuchElement.class)
    public void treeConstructionFromNonExistingHash() throws Exception {
        repository.new Tree("hash");
    }

    @Test
    public void treeTreeChildrenIterable() throws Exception {
        final String treeName = "name";

        VCSManager.Repository.Tree tree1 = repository.new Tree(Collections.emptyList(), Collections.emptyList());
        VCSManager.Repository.Tree tree2 = repository.new Tree(Collections.singletonList(mockedNamed(tree1, treeName)),
                                                               Collections.emptyList());

        assertThat(tree2.treeChildren(), contains(allOf(name(is(equalTo(treeName))),
                                                        underlyingObject(is(similarTo(tree1))))));
    }

    @Test
    public void treeBlobChildrenIterable() throws Exception {
        final String blobName = "name";
        Path pathToFile = rootDirectory.newFile().toPath();
        Files.write(pathToFile, new byte[] {1, 2, 3});
        VCSManager.Repository.Blob blob = repository.new Blob(pathToFile);

        VCSManager.Repository.Tree tree = repository.new Tree(Collections.emptyList(),
                                                              Collections.singletonList(mockedNamed(blob, blobName)));

        assertThat(tree.blobChildren(), contains(allOf(name(is(equalTo(blobName))),
                                                 underlyingObject(is(similarTo(blob))))));
    }

    @Test
    public void commitConstruction() throws Exception {
        repository.new Commit("author", "message", Collections.emptyList(), mockedTree("hash"));

        assertFilesInFolder(repository.getObjectsDirectory(), 1);
    }

    @Test
    public void commitConstructionFromHash() throws Exception {
        VCSManager.Repository.Commit commit1 = repository.new Commit("author",
                                                                     "message",
                                                                      Collections.emptyList(),
                                                                      mockedTree("hash"));

        VCSManager.Repository.Commit commit2 = repository.new Commit(commit1.getSha1Hash());

        assertThat(commit2, is(similarTo(commit1)));
        assertFilesInFolder(repository.getObjectsDirectory(), 1);
    }

    @Test
    public void commitGetTree() throws Exception {
        VCSManager.Repository.Tree tree = repository.new Tree(Collections.emptyList(), Collections.emptyList());
        VCSManager.Repository.Commit commit = repository.new Commit("author",
                                                                    "message",
                                                                     Collections.emptyList(),
                                                                     tree);

        assertThat(commit.getTree(), is(similarTo(tree)));
    }

    @Test
    public void commitParentCommits() throws Exception {
        VCSManager.Repository.Commit parentCommit = repository.new Commit("author1",
                                                                          "message1",
                                                                           Collections.emptyList(),
                                                                           mockedTree("hash1"));

        VCSManager.Repository.Commit commit = repository.new Commit("author2",
                                                                    "message2",
                                                                     Collections.singletonList(parentCommit),
                                                                     mockedTree("hash2"));

        assertThat(commit.parentCommits(), contains(similarTo(parentCommit)));
    }

    @Test
    public void referenceConstruction() throws Exception {
        repository.new Reference("reference", mockedCommit("hash"));

        assertFilesInFolder(repository.getHeadsDirectory(), 1);
    }

    @Test
    public void referenceConstructionFromName() throws Exception {
        final String name = "name";

        VCSManager.Repository.Reference reference1 = repository.new Reference(name, mockedCommit("hash"));
        VCSManager.Repository.Reference reference2 = repository.new Reference(name);

        assertThat(reference2, is(similarTo(reference1)));
        assertFilesInFolder(repository.getHeadsDirectory(), 1);
    }

    @Test
    public void referenceGetCommit() throws Exception {
        VCSManager.Repository.Commit commit = repository.new Commit("author",
                                                                    "message",
                                                                     Collections.emptyList(),
                                                                     mockedTree("hash"));

        VCSManager.Repository.Reference referenece = repository.new Reference("name", commit);

        assertThat(referenece.getCommit(), is(similarTo(commit)));
    }

    private void assertFilesInFolder(final @NotNull Path pathToFolder, final int expected) {
        File[] filesInFolder = pathToFolder.toFile().listFiles();
        assertThat(filesInFolder, notNullValue());

        // NPE is checked on a previous line
        assertThat(filesInFolder.length, is(equalTo(expected)));
    }

    private @NotNull VCSManager.Repository.Blob mockedBlob(final @NotNull String hash) {
        VCSManager.Repository.Blob blob = mock(VCSManager.Repository.Blob.class);
        when(blob.getSha1Hash()).thenReturn(hash);

        return blob;
    }

    private @NotNull VCSManager.Repository.Tree mockedTree(final @NotNull String hash) {
        VCSManager.Repository.Tree tree = mock(VCSManager.Repository.Tree.class);
        when(tree.getSha1Hash()).thenReturn(hash);

        return tree;
    }

    private @NotNull VCSManager.Repository.Commit mockedCommit(final @NotNull String hash) {
        VCSManager.Repository.Commit commit = mock(VCSManager.Repository.Commit.class);
        when(commit.getSha1Hash()).thenReturn(hash);

        return commit;
    }

    private <T> @NotNull Named<T> mockedNamed(final @NotNull T object, final @NotNull String name) {
        Named<T> named = mock(Named.class);
        when(named.getName()).thenReturn(name);
        when(named.getObject()).thenReturn(object);

        return named;
    }

    /* Hamcrest matchers */

    private Matcher<VCSManager.Repository.StoredObject> similarTo(VCSManager.Repository.StoredObject expected) {
        return new StoredObjectMatcher<>(expected);
    }

    private Matcher<VCSManager.Repository.Commit> similarTo(VCSManager.Repository.Commit expected) {
        return new StoredObjectMatcher<VCSManager.Repository.Commit>(expected) {
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof VCSManager.Repository.Commit)) {
                    return false;
                }

                VCSManager.Repository.Commit actual = (VCSManager.Repository.Commit) o;

                return super.matches(o) &&
                       actual.getAuthor().equals(expected.getAuthor()) &&
                       actual.getMessage().equals(expected.getMessage()) &&
                       actual.getDate().equals(expected.getDate());
            }

            @Override
            public void describeTo(Description description) {
                super.describeTo(description);

                description.appendText(", expected author: ").appendValue(expected.getAuthor())
                           .appendText(", expected message:" ).appendValue(expected.getMessage())
                           .appendText(", expected date: ").appendValue(expected.getDate());
            }
        };
    }

    private Matcher<VCSManager.Repository.Reference> similarTo(VCSManager.Repository.Reference expected) {
        return new BaseMatcher<VCSManager.Repository.Reference>() {
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof VCSManager.Repository.Reference)) {
                    return false;
                }

                VCSManager.Repository.Reference actual = (VCSManager.Repository.Reference) o;

                return actual.getName().equals(expected.getName()) &&
                       actual.getPathInStorage().equals(expected.getPathInStorage());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected name: ").appendValue(expected.getName())
                           .appendText(", expected pathInStorage: ").appendValue(expected.getPathInStorage());
            }
        };
    }

    private FeatureMatcher<Named, String> name(final @NotNull Matcher<? super String> subMatcher) {
        return new FeatureMatcher<Named, String>(subMatcher,
                                                "name field of Named class",
                                                "name") {
            @Override
            protected String featureValueOf(final @NotNull Named actual) {
                return actual.getName();
            }
        };
    }

    private <T> FeatureMatcher<Named<T>, T> underlyingObject(final @NotNull Matcher<? super T> subMatcher) {
        return new FeatureMatcher<Named<T>, T>(subMatcher,
                                              "object field of Named class",
                                              "underlying object") {
            @Override
            protected T featureValueOf(final @NotNull Named<T> actual) {
                return actual.getObject();
            }
        };
    }

    private class StoredObjectMatcher<T extends VCSManager.Repository.StoredObject> extends BaseMatcher<T> {
        private final @NotNull VCSManager.Repository.StoredObject expected;

        public StoredObjectMatcher(final @NotNull VCSManager.Repository.StoredObject expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object o) {
            if (!(o instanceof VCSManager.Repository.StoredObject)) {
                return false;
            }

            VCSManager.Repository.StoredObject actual = (VCSManager.Repository.StoredObject) o;

            return actual.getSha1Hash().equals(expected.getSha1Hash()) &&
                    actual.getPathInStorage().equals(expected.getPathInStorage());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("expected sha1Hash: ").appendValue(expected.getSha1Hash())
                       .appendText(", expected pathInStorage: ").appendValue(expected.getPathInStorage());
        }
    }
}