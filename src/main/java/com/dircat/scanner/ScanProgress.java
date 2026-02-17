package com.dircat.scanner;

/**
 * Callback interface for reporting scan progress.
 */
public interface ScanProgress {
    
    /**
     * Called when a file or directory is scanned.
     *
     * @param path         Current path being scanned
     * @param filesScanned Total number of files scanned so far
     * @param totalSize    Total size in bytes scanned so far
     */
    void onProgress(String path, int filesScanned, long totalSize);

    /**
     * Called when scanning is complete.
     *
     * @param filesScanned Total number of files scanned
     * @param totalSize    Total size in bytes
     */
    void onComplete(int filesScanned, long totalSize);

    /**
     * Called when an error occurs during scanning.
     *
     * @param path  Path where error occurred
     * @param error Error that occurred
     */
    void onError(String path, Exception error);
}
