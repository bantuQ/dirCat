package com.dircat.scanner;

import com.dircat.model.CatalogEntry;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Stack;

/**
 * Scanner for recursively traversing file system and building catalog entries.
 * Uses java.nio.file.Files.walkFileTree() for efficient directory traversal.
 */
public class FileSystemScanner {

    private final boolean includeHidden;
    private final int maxDepth;
    private ScanProgress progressCallback;
    private int filesScanned;
    private long totalSize;

    /**
     * Creates a new file system scanner with default settings.
     */
    public FileSystemScanner() {
        this(false, Integer.MAX_VALUE);
    }

    /**
     * Creates a new file system scanner with custom settings.
     *
     * @param includeHidden Whether to include hidden files and directories
     * @param maxDepth      Maximum depth to scan (use Integer.MAX_VALUE for unlimited)
     */
    public FileSystemScanner(boolean includeHidden, int maxDepth) {
        this.includeHidden = includeHidden;
        this.maxDepth = maxDepth;
        this.filesScanned = 0;
        this.totalSize = 0;
    }

    /**
     * Sets the progress callback for scan updates.
     *
     * @param callback Progress callback
     */
    public void setProgressCallback(ScanProgress callback) {
        this.progressCallback = callback;
    }

    /**
     * Scans the specified directory and returns a CatalogEntry.
     *
     * @param path Path to scan
     * @return CatalogEntry representing the scanned directory
     * @throws IOException If an I/O error occurs
     */
    public CatalogEntry scan(Path path) throws IOException {
        filesScanned = 0;
        totalSize = 0;

        if (!Files.exists(path)) {
            throw new IOException("Path does not exist: " + path);
        }

        CatalogEntry rootEntry = createEntry(path);

        if (Files.isDirectory(path)) {
            scanDirectory(path, rootEntry, 0);
        }

        if (progressCallback != null) {
            progressCallback.onComplete(filesScanned, totalSize);
        }

        return rootEntry;
    }

    /**
     * Recursively scans a directory and populates the catalog entry.
     *
     * @param dir         Directory to scan
     * @param parentEntry Parent catalog entry
     * @param depth       Current depth
     * @throws IOException If an I/O error occurs
     */
    private void scanDirectory(Path dir, CatalogEntry parentEntry, int depth) throws IOException {
        if (depth >= maxDepth) {
            return;
        }

        final int currentDepth = depth;
        final Stack<CatalogEntry> entryStack = new Stack<>();
        entryStack.push(parentEntry);

        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            private int level = 0;

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (level > 0) { // Skip root as it's already added
                    if (!includeHidden && isHidden(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    if (currentDepth + level > maxDepth) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    CatalogEntry dirEntry = createEntry(dir);
                    CatalogEntry parent = entryStack.peek();
                    parent.addChild(dirEntry);
                    entryStack.push(dirEntry);

                    reportProgress(dir);
                }
                level++;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!includeHidden && isHidden(file)) {
                    return FileVisitResult.CONTINUE;
                }

                CatalogEntry fileEntry = createEntry(file);
                CatalogEntry parent = entryStack.peek();
                parent.addChild(fileEntry);

                filesScanned++;
                totalSize += fileEntry.getSize();

                reportProgress(file);

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                level--;
                if (level > 0) {
                    entryStack.pop();
                }
                if (exc != null) {
                    handleError(dir, exc);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                handleError(file, exc);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a CatalogEntry from a Path.
     *
     * @param path Path to create entry from
     * @return CatalogEntry
     * @throws IOException If an I/O error occurs
     */
    private CatalogEntry createEntry(Path path) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        
        String name = path.getFileName() != null ? path.getFileName().toString() : path.toString();
        String pathStr = path.toAbsolutePath().toString();
        long size = attrs.isDirectory() ? 0 : attrs.size();
        LocalDateTime modified = LocalDateTime.ofInstant(
                attrs.lastModifiedTime().toInstant(),
                ZoneId.systemDefault()
        );
        boolean isDirectory = attrs.isDirectory();

        return new CatalogEntry(name, pathStr, size, modified, isDirectory);
    }

    /**
     * Checks if a path is hidden.
     *
     * @param path Path to check
     * @return True if hidden, false otherwise
     */
    private boolean isHidden(Path path) {
        try {
            return Files.isHidden(path) || path.getFileName().toString().startsWith(".");
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Reports progress to the callback.
     *
     * @param path Current path being processed
     */
    private void reportProgress(Path path) {
        if (progressCallback != null) {
            progressCallback.onProgress(path.toString(), filesScanned, totalSize);
        }
    }

    /**
     * Handles errors during scanning.
     *
     * @param path Path where error occurred
     * @param exc  Exception that occurred
     */
    private void handleError(Path path, IOException exc) {
        if (progressCallback != null) {
            progressCallback.onError(path.toString(), exc);
        }
    }

    /**
     * Gets the total number of files scanned.
     *
     * @return Number of files scanned
     */
    public int getFilesScanned() {
        return filesScanned;
    }

    /**
     * Gets the total size of files scanned.
     *
     * @return Total size in bytes
     */
    public long getTotalSize() {
        return totalSize;
    }
}
