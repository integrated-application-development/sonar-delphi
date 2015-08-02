/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.delphi.core.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.BatchExtension;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.project.DelphiWorkgroup;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import com.google.common.collect.Lists;

/**
 * Class that helps get the maven/ant configuration from .xml file
 */
// TODO Replace inheritance by composition
public class DelphiProjectHelper extends DelphiFileHelper implements BatchExtension {

    private final Configuration configuration;
    private final RuleFinder ruleFinder;
    private final FileSystem fs;
    private List<File> excludedSources;

    /**
     * ctor used by Sonar
     *
     * @param configuration
     * @param ruleFinder
     */
    public DelphiProjectHelper(Configuration configuration, RuleFinder ruleFinder, FileSystem fs) {
        super(configuration, fs);
        this.configuration = configuration;
        this.ruleFinder = ruleFinder;
        this.fs = fs;
        DelphiUtils.LOG.info("Delphi Project Helper creation!!!");
        this.excludedSources = detectExcludedSources();
    }

    /**
     * @return Rule finder
     */
    public RuleFinder getRuleFinder() {
        return ruleFinder;
    }

    /**
     * Should includes be copy-pasted to a file which tries to include them
     *
     * @return True if so, false otherwise
     */
    public boolean shouldExtendIncludes() {
        if (configuration == null) {
            return true; // process includes
        }
        String str = configuration.getString(DelphiPlugin.INCLUDE_EXTEND_KEY, "true");
        return "true".equals(str);
    }

    /**
     * Gets the include directories (directories that are looked for include
     * files)
     *
     * @param fileSystem Project file system
     * @return List of include directories
     */
    public List<File> getIncludeDirectories() {
        List<File> result = new ArrayList<File>();
        if (configuration == null) {
            return result;
        }
        String[] includedDirs = configuration.getStringArray(DelphiPlugin.INCLUDED_DIRECTORIES_KEY);
        if (includedDirs != null && includedDirs.length > 0) {
            for (String path : includedDirs) {
                if (StringUtils.isEmpty(path)) {
                    continue;
                }
                File included = DelphiUtils.resolveAbsolutePath(fs.baseDir().getAbsolutePath(), path.trim());
                if (!included.exists()) {
                    DelphiUtils.LOG.warn("Include directory does not exist: " + included.getAbsolutePath());
                } else if (!included.isDirectory()) {
                    DelphiUtils.LOG.warn("Include path is not a directory: " + included.getAbsolutePath());
                } else {
                    result.add(included);
                }
            }
        } else {
            DelphiUtils.LOG.info("No include directories found in project configuration.");
        }
        return result;
    }

    /**
     * Gets the list of excluded source files and directories
     *
     * @return List of excluded source files and directories
     */
    public List<File> getExcludedSources() {
        return this.excludedSources;
    }

    private List<File> detectExcludedSources() {
        List<File> result = new ArrayList<File>();
        if (configuration == null) {
            return result;
        }
        String[] excludedNames = configuration.getStringArray(DelphiPlugin.EXCLUDED_DIRECTORIES_KEY);
        if (excludedNames != null && excludedNames.length > 0) {
            for (String path : excludedNames) {
                if (StringUtils.isEmpty(path)) {
                    continue;
                }
                File excluded = DelphiUtils.resolveAbsolutePath(fs.baseDir().getAbsolutePath(), path.trim());
                result.add(excluded);
                if (!excluded.exists()) {
                    DelphiUtils.LOG.warn("Exclude directory does not exist: " + excluded.getAbsolutePath());
                }
            }
        } else {
            DelphiUtils.LOG.info("No exclude directories found in project configuration.");
        }
        return result;
    }

    /**
     * Gets the project file (.dproj)
     *
     * @return Path to project file
     */
    public String getProjectFile() {
        if (configuration == null) {
            return null;
        }
        return configuration.getString(DelphiPlugin.PROJECT_FILE_KEY);
    }

    /**
     * Gets the workgroup (.groupproj) file
     *
     * @return Path to workgroup file
     */
    public String getWorkgroupFile() {
        if (configuration == null) {
            return null;
        }
        return configuration.getString(DelphiPlugin.WORKGROUP_FILE_KEY);
    }

    /**
     * Should we import sources or not
     *
     * @return True if so, false otherwise
     */
    public boolean getImportSources() {
        if (configuration == null) {
            return CoreProperties.CORE_IMPORT_SOURCES_DEFAULT_VALUE;
        }
        return configuration.getBoolean(CoreProperties.CORE_IMPORT_SOURCES_PROPERTY,
                CoreProperties.CORE_IMPORT_SOURCES_DEFAULT_VALUE);
    }

    /**
     * Create list of DelphiLanguage projects in a current workspace
     *
     * @return List of DelphiLanguage projects
     */
    public List<DelphiProject> getWorkgroupProjects() {
        List<DelphiProject> list = new ArrayList<DelphiProject>();

        String dprojPath = getProjectFile();
        String gprojPath = getWorkgroupFile();

        if (!StringUtils.isEmpty(gprojPath)) // Single workgroup file,
                                             // containing list of .dproj files
        {
            try {
                DelphiUtils.LOG.debug(".groupproj file found: " + gprojPath);
                DelphiWorkgroup workGroup = new DelphiWorkgroup(new File(gprojPath));
                for (DelphiProject newProject : workGroup.getProjects()) {
                    list.add(newProject);
                }
            } catch (IOException e) {
                DelphiUtils.LOG.error(e.getMessage());
                DelphiUtils.LOG.error("Skipping .groupproj reading, default configuration assumed.");
                DelphiProject newProject = new DelphiProject("Default Project");
                newProject.setIncludeDirectories(getIncludeDirectories());
                newProject.setSourceFiles(mainFiles());
                list.clear();
                list.add(newProject);
            }
        }

        else if (!StringUtils.isEmpty(dprojPath)) // Single .dproj file
        {
            File dprojFile = DelphiUtils.resolveAbsolutePath(fs.baseDir().getAbsolutePath(), dprojPath);
            DelphiUtils.LOG.info(".dproj file found: " + gprojPath);
            DelphiProject newProject = new DelphiProject(dprojFile);
            list.add(newProject);
        }

        else // No .dproj files, create default project
        {
            DelphiProject newProject = new DelphiProject("Default Project");
            newProject.setIncludeDirectories(getIncludeDirectories());
            newProject.setSourceFiles(mainFiles());
            list.add(newProject);
        }

        return list;
    }

    public List<InputFile> mainFiles() {
        FilePredicates p = fs.predicates();
        Iterable<InputFile> inputFiles = fs.inputFiles(p.and(p.hasLanguage(DelphiLanguage.KEY),
                p.hasType(InputFile.Type.MAIN)));
        return Lists.newArrayList(inputFiles);
    }

    public boolean shouldExecuteOnProject() {
        return fs.hasFiles(fs.predicates().hasLanguage(DelphiLanguage.KEY));
    }

    public InputFile getFile(String path) {
        return fs.inputFile(fs.predicates().is(new File(path)));
    }

    public InputFile findFileInDirectories(String fileName) throws FileNotFoundException {
        for (InputFile inputFile : mainFiles()) {
            if (inputFile.file().getName().equalsIgnoreCase(fileName)) {
                return inputFile;
            }
        }

        throw new FileNotFoundException(fileName);
    }

    public File baseDir() {
        return this.fs.baseDir();
    }

    public File workDir() {
        return fs.workDir();
    }

}
