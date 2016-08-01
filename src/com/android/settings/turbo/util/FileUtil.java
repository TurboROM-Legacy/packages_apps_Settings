/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.turbo.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

/**
 * A helper class for file related operations
 */
public class FileUtil {

    private static final String LOG_TAG = "FileUtil";

    /**
     * Recursively set read and exec (if folder) permissions for given file.
     */
    public static void setReadableRecursive(File file) {
        file.setReadable(true);
        if (file.isDirectory()) {
            file.setExecutable(true);
            File[] children = file.listFiles();
            if (children != null) {
                for (File childFile : file.listFiles()) {
                    setReadableRecursive(childFile);
                }
            }
        }
    }

    /**
     * Helper function to create a temp directory in the system default temporary file directory.
     *
     * @param prefix The prefix string to be used in generating the file's name; must be at least
     *            three characters long
     * @return the created directory
     * @throws IOException if file could not be created
     */
    public static File createTempDir(String prefix) throws IOException {
        return createTempDir(prefix, null);
    }

    /**
     * Helper function to create a temp directory.
     *
     * @param prefix The prefix string to be used in generating the file's name; must be at least
     *            three characters long
     * @param parentDir The parent directory in which the directory is to be created. If
     *            <code>null</code> the system default temp directory will be used.
     * @return the created directory
     * @throws IOException if file could not be created
     */
    public static File createTempDir(String prefix, File parentDir) throws IOException {
        // create a temp file with unique name, then make it a directory
        File tmpDir = File.createTempFile(prefix, "", parentDir);
        return deleteFileAndCreateDirWithSameName(tmpDir);
    }

    private static File deleteFileAndCreateDirWithSameName(File tmpDir) throws IOException {
        tmpDir.delete();
        return createDir(tmpDir);
    }

    private static File createDir(File tmpDir) throws IOException {
        if (!tmpDir.mkdirs()) {
            throw new IOException("unable to create directory");
        }
        return tmpDir;
    }

    /**
     * Helper function to create a named directory inside your temp folder.
     * <p/>
     * This directory will not have it's name randomized. If the directory already exists it will
     * be returned.
     *
     * @param name The name of the directory to create in your tmp folder.
     * @return the created directory
     */
    public static File createNamedTempDir(String name) throws IOException {
        File namedTmpDir = new File(System.getProperty("java.io.tmpdir"), name);
        if (!namedTmpDir.exists()) {
            createDir(namedTmpDir);
        }
        return namedTmpDir;
    }

    /**
     * A helper method that copies a file's contents to a local file
     *
     * @param origFile the original file to be copied
     * @param destFile the destination file
     * @throws IOException if failed to copy file
     */
    public static void copyFile(File origFile, File destFile) throws IOException {
        writeToFile(new FileInputStream(origFile), destFile);
    }

    /**
     * Recursively copy folder contents.
     * <p/>
     * Only supports copying of files and directories - symlinks are not copied.
     *
     * @param sourceDir the folder that contains the files to copy
     * @param destDir the destination folder
     * @throws IOException
     */
    public static void recursiveCopy(File sourceDir, File destDir) throws IOException {
        File[] childFiles = sourceDir.listFiles();
        if (childFiles == null) {
            throw new IOException(String.format(
                    "Failed to recursively copy. Could not determine contents for directory '%s'",
                    sourceDir.getAbsolutePath()));
        }
        for (File childFile : childFiles) {
            File destChild = new File(destDir, childFile.getName());
            if (childFile.isDirectory()) {
                if (!destChild.mkdir()) {
                    throw new IOException(String.format("Could not create directory %s",
                            destChild.getAbsolutePath()));
                }
                recursiveCopy(childFile, destChild);
            } else if (childFile.isFile()) {
                copyFile(childFile, destChild);
            }
        }
    }

    /**
     * A helper method for reading string data from a file
     *
     * @param sourceFile the file to read from
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static String readStringFromFile(File sourceFile) throws IOException {
        FileInputStream is = null;
        try {
            // no need to buffer since StreamUtil does
            is = new FileInputStream(sourceFile);
            return StreamUtil.getStringFromStream(is);
        } finally {
            StreamUtil.close(is);
        }
    }



    /**
     * A helper method for writing string data to file
     *
     * @param inputString the input {@link String}
     * @param destFile the dest file to write to
     */
    public static void writeToFile(String inputString, File destFile) throws IOException {
        writeToFile(new ByteArrayInputStream(inputString.getBytes()), destFile);
    }

    /**
     * A helper method for writing stream data to file
     *
     * @param input the unbuffered input stream
     * @param destFile the dest file to write to
     */
    public static void writeToFile(InputStream input, File destFile) throws IOException {
        InputStream origStream = null;
        OutputStream destStream = null;
        try {
            origStream = new BufferedInputStream(input);
            destStream = new BufferedOutputStream(new FileOutputStream(destFile));
            StreamUtil.copyStreams(origStream, destStream);
        } finally {
            StreamUtil.close(origStream);
            StreamUtil.flushAndCloseStream(destStream);
        }
    }

    /**
     * Write a string value to the specified file.
     * @param filename      The filename
     * @param value         The value
     */
    public static void writeValue(String filename, String value) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(filename));
            fos.write(value.getBytes());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readLine(String filename) {
        BufferedReader br = null;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(filename), 1024);
            line = br.readLine();
        } catch (IOException e) {
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return line;
    }

    /**
     * Recursively delete given file and all its contents
     */
    public static void recursiveDelete(File rootDir) {
        if (rootDir.isDirectory()) {
            File[] childFiles = rootDir.listFiles();
            if (childFiles != null) {
                for (File child : childFiles) {
                    recursiveDelete(child);
                }
            }
        }
        rootDir.delete();
    }

    /**
     * Gets the extension for given file name.
     *
     * @param fileName
     * @return the extension or empty String if file has no extension
     */
    public static String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return "";
        } else {
            return fileName.substring(index);
        }
    }

    /**
     * Gets the base name, without extension, of given file name.
     * <p/>
     * e.g. getBaseName("file.txt") will return "file"
     *
     * @param fileName
     * @return the base name
     */
    public static String getBaseName(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }

    /**
     * Try to delete a file. Intended for use when cleaning up
     * in {@code finally} stanzas. {@param file} may be null.
     */
    public static void deleteFile(File file) {
        if (file != null) {
            file.delete();
        }
    }

    /**
     * Helper method to build a system-dependent File
     *
     * @param parentDir the parent directory to use.
     * @param pathSegments the relative path segments to use
     * @return the {@link File} representing given path, with each <var>pathSegment</var>
     *         separated by {@link File#separatorChar}
     */
    public static File getFileForPath(File parentDir, String... pathSegments) {
        return new File(parentDir, getPath(pathSegments));
    }

    /**
     * Helper method to build a system-dependent relative path
     *
     * @param pathSegments the relative path segments to use
     * @return the {@link String} representing given path, with each <var>pathSegment</var>
     *         separated by {@link File#separatorChar}
     */
    public static String getPath(String... pathSegments) {
        StringBuilder pathBuilder = new StringBuilder();
        boolean isFirst = true;
        for (String path : pathSegments) {
            if (!isFirst) {
                pathBuilder.append(File.separatorChar);
            } else {
                isFirst = false;
            }
            pathBuilder.append(path);
        }
        return pathBuilder.toString();
    }

    /**
     * Recursively search given directory for first file with given name
     *
     * @param dir the directory to search
     * @param fileName the name of the file to search for
     * @return the {@link File} or <code>null</code> if it could not be found
     */
    public static File findFile(File dir, String fileName) {
        if (dir.listFiles() != null) {
            for (File file : dir.listFiles()) {
                if (file.getName().equals(fileName)) {
                    return file;
                } else if (file.isDirectory()) {
                    File result = findFile(file, fileName);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Recursively find all directories under the given {@code rootDir}
     *
     * @param rootDir the root directory to search in
     * @param relativeParent An optional parent for all {@link File}s returned. If not specified,
     *            all {@link File}s will be relative to {@code rootDir}.
     * @return An set of {@link File}s, representing all directories under {@code rootDir},
     *         including {@code rootDir} itself. If {@code rootDir} is null, an empty set is
     *         returned.
     */
    public static Set<File> findDirsUnder(File rootDir, File relativeParent) {
        Set<File> dirs = new HashSet<File>();
        if (rootDir != null) {
            if (!rootDir.isDirectory()) {
                throw new IllegalArgumentException("Can't find dirs under '" + rootDir
                        + "'. It's not a directory.");
            }
            File thisDir = new File(relativeParent, rootDir.getName());
            dirs.add(thisDir);
            for (File file : rootDir.listFiles()) {
                if (file.isDirectory()) {
                    dirs.addAll(findDirsUnder(file, thisDir));
                }
            }
        }
        return dirs;
    }

    /**
     * Returns all jar files found in given directory
     */
    public static List<File> collectJars(File dir) {
        List<File> list = new ArrayList<File>();
        File[] jarFiles = dir.listFiles(new JarFilter());
        if (jarFiles != null) {
            list.addAll(Arrays.asList(dir.listFiles(new JarFilter())));
        }
        return list;
    }

    private static class JarFilter implements FilenameFilter {
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    }

}
