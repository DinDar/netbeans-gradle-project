package org.netbeans.gradle.project.java.query;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.gradle.model.java.JavaSourceGroup;
import org.netbeans.gradle.model.java.JavaSourceSet;
import org.netbeans.gradle.project.java.JavaExtension;
import org.netbeans.gradle.project.java.model.NbJavaModel;
import org.netbeans.gradle.project.java.model.NbJavaModule;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class GradleUnitTestFinder implements MultipleRootsUnitTestForSourceQueryImplementation {
    private final JavaExtension javaExt;

    public GradleUnitTestFinder(JavaExtension javaExt) {
        if (javaExt == null) throw new NullPointerException("javaExt");
        this.javaExt = javaExt;
    }

    private static boolean hasSource(NbJavaModule module, FileObject source) {
        for (JavaSourceGroup sourceGroup: module.getMainSourceSet().getSourceGroups()) {
            for (File sourceRoot: sourceGroup.getSourceRoots()) {
                FileObject sourceRootObj = FileUtil.toFileObject(sourceRoot);
                if (sourceRootObj != null && FileUtil.getRelativePath(sourceRootObj, source) != null) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean hasTest(NbJavaModule module, FileObject source) {
        for (JavaSourceGroup sourceGroup: module.getTestSourceSet().getSourceGroups()) {
            for (File sourceRoot: sourceGroup.getSourceRoots()) {
                FileObject sourceRootObj = FileUtil.toFileObject(sourceRoot);
                if (sourceRootObj != null && FileUtil.getRelativePath(sourceRootObj, source) != null) {
                    return true;
                }
            }
        }

        return false;
    }

    private static URL[] urlsFromSourceSet(JavaSourceSet sourceSet) {
        List<URL> result = new LinkedList<URL>();
        for (JavaSourceGroup sourceGroup: sourceSet.getSourceGroups()) {
            for (File sourceRoot: sourceGroup.getSourceRoots()) {
                URL url = FileUtil.urlForArchiveOrDir(sourceRoot);
                if (url != null) {
                    result.add(url);
                }
            }
        }

        return result.toArray(new URL[result.size()]);
    }

    private static URL[] getSourceRoots(NbJavaModule module) {
        return urlsFromSourceSet(module.getMainSourceSet());
    }

    private static URL[] getTestRoots(NbJavaModule module) {
        return urlsFromSourceSet(module.getTestSourceSet());
    }

    @Override
    public URL[] findUnitTests(FileObject source) {
        NbJavaModel projectModel = javaExt.getCurrentModel();

        NbJavaModule mainModule = projectModel.getMainModule();
        if (hasSource(mainModule, source)) {
            return getTestRoots(mainModule);
        }

        return null;
    }

    @Override
    public URL[] findSources(FileObject unitTest) {
        NbJavaModel projectModel = javaExt.getCurrentModel();

        NbJavaModule mainModule = projectModel.getMainModule();
        if (hasTest(mainModule, unitTest)) {
            return getSourceRoots(mainModule);
        }

        return null;
    }
}
