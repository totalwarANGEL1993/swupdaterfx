package twa.tools.updaterfx.controller.git;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Controller for handling Git repositories.
 */
@Component
public class GitController {
    private static final Logger LOG = LoggerFactory.getLogger(GitController.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${target.repository}")
    private String targetRepositoryUrl;
    @Value("${target.branch}")
    private String targetBranch;
    @Value("${target.directory}")
    private String targetDirectory;

    /**
     * Returns a new Git instance with the repository loaded.
     * @return Git instance with repository
     * @throws IOException
     */
    public Git getRepository() throws IOException {
        final File directory = new File(System.getProperty("user.dir") + File.separator + targetDirectory);
        return Git.open(directory);
    }

    /**
     * Loads the latest version of the specified file.
     * @param path Path of file in project
     * @return File content
     * @throws GitException File not found
     */
    public OutputStream getFile(final String path, boolean local) throws GitException {
        OutputStream os = new ByteArrayOutputStream();
        try (Git git = getRepository())
        {
            String refName = "refs/remotes/origin/master";
            if (local) {
                refName = "refs/heads/master";
            }
            final ObjectId lastCommitId = git.getRepository().resolve(refName);

            try (RevWalk revWalk = new RevWalk(git.getRepository()))
            {
                RevCommit commit = revWalk.parseCommit(lastCommitId);
                RevTree tree = commit.getTree();
                try (TreeWalk treeWalk = new TreeWalk(git.getRepository()))
                {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create(path));
                    if (!treeWalk.next())
                    {
                        throw new GitException("Did not find expected file '" +path+ "'!");
                    }
                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = git.getRepository().open(objectId);

                    loader.copyTo(os);
                }
                revWalk.dispose();
            }
        }
        catch (Exception e)
        {
            throw new GitException("Failed to obtain file!", e);
        }
        return os;
    }

    /**
     * Clones the repository from the remote.
     * @throws GitException Cloning has failed
     */
    public void cloneRepository() throws GitException {
        try
        {
            Git.cloneRepository()
                .setURI(targetRepositoryUrl)
                .setDirectory(new File(System.getProperty("user.dir") + File.separator + targetDirectory))
                .setBranchesToClone(Collections.singletonList("refs/heads/" + targetBranch))
                .setBranch("refs/heads/" +targetBranch)
                .call()
                .close();
        }
        catch (InvalidRemoteException e) {
            throw new GitException("Can not find remote repository!", e);
        }
        catch (TransportException e) {
            throw new GitException("Failed to connect to repository", e);
        }
        catch (GitAPIException e) {
            throw new GitException("Failed to clone repository!", e);
        }
    }

    /**
     * Checks if the currently checked out version is the current version.
     * @return Local version is current version
     * @throws GitException Checking changes failed
     */
    public boolean isCurrentVersion() throws GitException {
        try (Git git = getRepository())
        {
            git.fetch().call();
            AbstractTreeIterator oldTreeParser = prepareTreeParser(git.getRepository(), "refs/heads/master");
            AbstractTreeIterator newTreeParser = prepareTreeParser(git.getRepository(), "refs/remotes/origin/master");
            List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).call();
            return diff.isEmpty();
        }
        catch (RepositoryNotFoundException e) {
            purgeRepository();
            cloneRepository();
            return isCurrentVersion();
        }
        catch (Exception e)
        {
            LOG.error("Failed to fetch changes on branch {}!", targetBranch, e);
            throw new GitException("Failed to fetch changes on branch " +targetBranch+ "!", e);
        }
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String ref) throws IOException {
        Ref head = repository.exactRef(ref);
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(head.getObjectId());
            RevTree tree = walk.parseTree(commit.getTree().getId());
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }
            walk.dispose();
            return treeParser;
        }
    }

    public void resetRepository() throws GitException {
        try (Git git = getRepository()) {
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
        }
        catch (Exception e) {
            LOG.error("Failed to open repository!", e);
            throw new GitException("Failed to reset repositoty!", e);
        }
    }

    /**
     * Rebases the directory to the tip of tbe branch.
     * @throws GitException Rebasing has failed
     */
    public void rebaseRepository() throws GitException {
        try (Git git = getRepository())
        {
            resetRepository();
            git.pull().setRebase(true).call();
        }
        catch (IOException e) {
            LOG.error("Failed to open repository!", e);
        }
        catch (GitAPIException e) {
            LOG.error("Failed to pull changes on branch {}!", targetBranch, e);
            throw new GitException("Failed to pull changes on branch " +targetBranch+ "!", e);
        }
    }

    /**
     * Deletes the directory the repository is in.
     * @throws GitException Deletion has failed
     */
    public void purgeRepository() throws GitException {
        try
        {
            final File directory = new File(System.getProperty("user.dir") + File.separator + targetDirectory);
            if (directory.exists()) {
                FileUtils.deleteDirectory(directory);
            }
        }
        catch (Exception e) {
            LOG.error("Failed to purge repository!", e);
            throw new GitException("\"Failed to purge repository!", e);
        }
    }
}
