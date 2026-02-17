package com.dircat.scanner;

import com.dircat.model.CatalogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileSystemScanner.
 */
class FileSystemScannerTest {

    @TempDir
    Path tempDir;

    private FileSystemScanner scanner;

    @BeforeEach
    void setUp() {
        scanner = new FileSystemScanner();
    }

    @Test
    void testScanEmptyDirectory() throws IOException {
        CatalogEntry entry = scanner.scan(tempDir);
        
        assertNotNull(entry);
        assertTrue(entry.isDirectory());
        assertEquals(0, entry.getChildren().size());
        assertEquals(0, scanner.getFilesScanned());
    }

    @Test
    void testScanDirectoryWithFiles() throws IOException {
        // Create test files
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file2.txt"));
        Files.write(tempDir.resolve("file1.txt"), "Hello World".getBytes());
        
        CatalogEntry entry = scanner.scan(tempDir);
        
        assertNotNull(entry);
        assertTrue(entry.isDirectory());
        assertEquals(2, entry.getChildren().size());
        assertEquals(2, scanner.getFilesScanned());
        assertTrue(scanner.getTotalSize() > 0);
    }

    @Test
    void testScanNestedDirectories() throws IOException {
        // Create nested structure
        Path subDir1 = Files.createDirectory(tempDir.resolve("subdir1"));
        Path subDir2 = Files.createDirectory(tempDir.resolve("subdir2"));
        Files.createFile(subDir1.resolve("file1.txt"));
        Files.createFile(subDir2.resolve("file2.txt"));
        Files.createFile(tempDir.resolve("root.txt"));
        
        CatalogEntry entry = scanner.scan(tempDir);
        
        assertNotNull(entry);
        assertEquals(3, entry.getChildren().size()); // 2 subdirs + 1 file
        assertEquals(3, scanner.getFilesScanned()); // Only counts files, not dirs
        
        // Verify subdirectories have children
        boolean foundSubDir1 = false;
        for (CatalogEntry child : entry.getChildren()) {
            if (child.getName().equals("subdir1")) {
                foundSubDir1 = true;
                assertEquals(1, child.getChildren().size());
            }
        }
        assertTrue(foundSubDir1);
    }

    @Test
    void testScanSingleFile() throws IOException {
        Path file = Files.createFile(tempDir.resolve("test.txt"));
        Files.write(file, "Test content".getBytes());
        
        CatalogEntry entry = scanner.scan(file);
        
        assertNotNull(entry);
        assertFalse(entry.isDirectory());
        assertEquals("test.txt", entry.getName());
        assertTrue(entry.getSize() > 0);
    }

    @Test
    void testScanNonExistentPath() {
        Path nonExistent = tempDir.resolve("nonexistent");
        
        assertThrows(IOException.class, () -> {
            scanner.scan(nonExistent);
        });
    }

    @Test
    void testHiddenFilesExcluded() throws IOException {
        // Create a hidden file (starts with dot)
        Files.createFile(tempDir.resolve(".hidden"));
        Files.createFile(tempDir.resolve("visible.txt"));
        
        FileSystemScanner scanner = new FileSystemScanner(false, Integer.MAX_VALUE);
        CatalogEntry entry = scanner.scan(tempDir);
        
        // Should only count visible file
        assertEquals(1, scanner.getFilesScanned());
        assertEquals(1, entry.getChildren().size());
    }

    @Test
    void testMaxDepthLimit() throws IOException {
        // Create nested structure with depth > 1
        Path level1 = Files.createDirectory(tempDir.resolve("level1"));
        Path level2 = Files.createDirectory(level1.resolve("level2"));
        Files.createFile(level2.resolve("deep.txt"));
        Files.createFile(level1.resolve("mid.txt"));
        Files.createFile(tempDir.resolve("top.txt"));
        
        // Scan with max depth of 1
        FileSystemScanner limitedScanner = new FileSystemScanner(false, 1);
        CatalogEntry entry = limitedScanner.scan(tempDir);
        
        // Should scan top.txt and mid.txt, but not deep.txt
        assertTrue(limitedScanner.getFilesScanned() <= 2);
    }

    @Test
    void testProgressCallback() throws IOException {
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file2.txt"));
        
        final int[] progressCallCount = {0};
        final boolean[] completeCallMade = {false};
        
        scanner.setProgressCallback(new ScanProgress() {
            @Override
            public void onProgress(String path, int filesScanned, long totalSize) {
                progressCallCount[0]++;
                assertTrue(filesScanned >= 0);
                assertTrue(totalSize >= 0);
            }

            @Override
            public void onComplete(int filesScanned, long totalSize) {
                completeCallMade[0] = true;
                assertEquals(2, filesScanned);
            }

            @Override
            public void onError(String path, Exception error) {
                fail("Should not have errors in this test");
            }
        });
        
        scanner.scan(tempDir);
        
        assertTrue(progressCallCount[0] > 0);
        assertTrue(completeCallMade[0]);
    }

    @Test
    void testGetFileCount() throws IOException {
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(subDir.resolve("file2.txt"));
        
        CatalogEntry entry = scanner.scan(tempDir);
        
        assertEquals(2, entry.getFileCount());
    }

    @Test
    void testGetDirectoryCount() throws IOException {
        Path subDir1 = Files.createDirectory(tempDir.resolve("subdir1"));
        Path subDir2 = Files.createDirectory(tempDir.resolve("subdir2"));
        
        CatalogEntry entry = scanner.scan(tempDir);
        
        // Root + 2 subdirectories = 3
        assertEquals(3, entry.getDirectoryCount());
    }

    @Test
    void testGetTotalSize() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("file1.txt"));
        Path file2 = Files.createFile(tempDir.resolve("file2.txt"));
        
        Files.write(file1, new byte[100]);
        Files.write(file2, new byte[200]);
        
        CatalogEntry entry = scanner.scan(tempDir);
        
        assertEquals(300, entry.getTotalSize());
    }
}
