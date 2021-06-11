package io.jenkins.plugins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.model.Result;
import hudson.scm.ChangeLogSet;

import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import javax.annotation.CheckForNull;

public class PathignorePlugin extends BuildWrapper {

    private boolean invertIgnore;
    private @CheckForNull String ignoredPaths;

    @DataBoundSetter
    public void setInvertIgnore(boolean invertIgnore) {
        this.invertIgnore = invertIgnore;
    }

    public boolean isInvertIgnore() {
        return invertIgnore;
    }

    @DataBoundSetter
    public void setIgnoredPaths(String ignoredPaths) {
        this.ignoredPaths = ignoredPaths;
    }

    public String getIgnoredPaths() {
        return ignoredPaths;
    }


    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Do not build if only specified paths have changed";
        }
    }

    @DataBoundConstructor
    public PathignorePlugin(boolean invertIgnore, String ignoredPaths)
    {
        this.invertIgnore = invertIgnore;
        this.ignoredPaths = ignoredPaths;
    }

    @Override
    public Environment setUp(
      AbstractBuild build,
      Launcher launcher,
      BuildListener listener)
    {
        final PrintStream logger = listener.getLogger();
        List<String> patterns = new ArrayList<>();
        String mode;

        if (ignoredPaths != null && ignoredPaths.isEmpty() == false)
            patterns = Arrays.asList(ignoredPaths.split("\\s*,\\s*"));

        if (invertIgnore) {
            mode = "Including";
        } else {
            mode = "Ignoring";
        }
        logger.println(mode + " paths matching patterns: " + patterns.toString());

        // XXX: Can there be files in the changeset if it's manually triggered?
        // If so, how do we check for manual trigger?
        ChangeLogSet<ChangeLogSet.Entry> changeset = build.getChangeSet();

        if (changeset != null && changeset.isEmptySet()) {
            logger.println("Empty changeset, running build");
            return new Environment(){};
        }

        List<String> paths = new ArrayList<>();
        for (ChangeLogSet.Entry entry : changeset) {
            Collection<? extends ChangeLogSet.AffectedFile> files = entry.getAffectedFiles();
            for (ChangeLogSet.AffectedFile file : files) {
                String path = file.getPath();
                //logger.println(path);
                paths.add(path);
            }
        }

        for (int i = 0; i < paths.size(); i++)
        {
            String path = paths.get(i);
            FileSystem fs = FileSystems.getDefault();
            boolean pattern_matched = false;
            for (int j = 0; j < patterns.size(); j++)
            {
                String pattern = patterns.get(j);
                PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);

                boolean matched = matcher.matches(Paths.get(path));
                if (invertIgnore && matched)
                {
                    logger.println("File " + path + " passed filter, running build");
                    return new Environment(){};
                }

                pattern_matched |= matched;
            }

            if (!invertIgnore && !pattern_matched)
            {
                logger.println("File " + path + " passed filter, running build");
                return new Environment(){};
            }
        }

        // We only get here if no unignored or included file was touched, so skip this build
        build.setResult(Result.NOT_BUILT);
        logger.println("No paths passed filter, skipping build.");
        logger.println("Changed paths: " + paths.toString());
        logger.println("Build not needed");
        return null;

    }
}