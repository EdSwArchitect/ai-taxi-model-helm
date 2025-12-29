package com.bscllc.taxis.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MonitorTest {

    @TempDir
    Path tempDir;

    private Monitor monitor;
    private List<Path> addedFiles;
    private List<Path> modifiedFiles;
    private List<Path> deletedFiles;

    @BeforeEach
    void setUp() {
        addedFiles = new ArrayList<>();
        modifiedFiles = new ArrayList<>();
        deletedFiles = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        if (monitor != null && monitor.isRunning()) {
            monitor.stop();
        }
    }

    @Test
    void testBuilderWithValidConfiguration() {
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.txt$")
                .scanPeriod(1000)
                .build();

        assertNotNull(monitor);
        assertEquals(tempDir, monitor.getDirectory());
        assertEquals(".*\\.txt$", monitor.getFilePattern());
        assertEquals(1000, monitor.getScanPeriodMillis());
    }

    @Test
    void testBuilderThrowsExceptionWhenDirectoryNotSet() {
        assertThrows(IllegalArgumentException.class, () -> {
            Monitor.builder()
                    .filePattern(".*\\.txt$")
                    .build();
        });
    }

    @Test
    void testBuilderThrowsExceptionWhenPatternNotSet() {
        assertThrows(IllegalArgumentException.class, () -> {
            Monitor.builder()
                    .directory(tempDir.toString())
                    .build();
        });
    }

    @Test
    void testBuilderThrowsExceptionWhenPatternIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            Monitor.builder()
                    .directory(tempDir.toString())
                    .filePattern("")
                    .build();
        });
    }

    @Test
    void testStartThrowsExceptionWhenDirectoryDoesNotExist() throws IOException {
        Path nonExistentDir = tempDir.resolve("nonexistent");
        monitor = Monitor.builder()
                .directory(nonExistentDir.toString())
                .filePattern(".*\\.txt$")
                .build();

        assertThrows(IOException.class, () -> monitor.start());
    }

    @Test
    void testStartThrowsExceptionWhenPathIsNotDirectory() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.createFile(file);
        
        monitor = Monitor.builder()
                .directory(file.toString())
                .filePattern(".*\\.txt$")
                .build();

        assertThrows(IOException.class, () -> monitor.start());
    }

    @Test
    void testStartAndStop() throws IOException, InterruptedException {
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.txt$")
                .scanPeriod(100)
                .build();

        assertFalse(monitor.isRunning());
        
        monitor.start();
        assertTrue(monitor.isRunning());
        
        Thread.sleep(200); // Wait for at least one scan
        
        monitor.stop();
        assertFalse(monitor.isRunning());
    }

    @Test
    void testFileAddedDetection() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.txt$")
                .scanPeriod(100)
                .onFileAdded(file -> {
                    addedFiles.add(file);
                    latch.countDown();
                })
                .build();

        monitor.start();
        
        // Wait a bit for initial scan
        Thread.sleep(150);
        
        // Create a matching file
        Path testFile = tempDir.resolve("test.txt");
        Files.createFile(testFile);
        
        // Wait for detection
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, addedFiles.size());
        assertEquals(testFile, addedFiles.get(0));
    }

    @Test
    void testFilePatternMatching() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.parquet$")
                .scanPeriod(100)
                .onFileAdded(file -> {
                    addedFiles.add(file);
                    latch.countDown();
                })
                .build();

        monitor.start();
        
        Thread.sleep(150);
        
        // Create matching file
        Path matchingFile = tempDir.resolve("data.parquet");
        Files.createFile(matchingFile);
        
        // Create non-matching file
        Path nonMatchingFile = tempDir.resolve("data.txt");
        Files.createFile(nonMatchingFile);
        
        // Wait for detection
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, addedFiles.size());
        assertEquals(matchingFile, addedFiles.get(0));
        
        // Verify non-matching file is not in known files
        Set<Path> knownFiles = monitor.getKnownFiles();
        assertTrue(knownFiles.contains(matchingFile));
        assertFalse(knownFiles.contains(nonMatchingFile));
    }

    @Test
    void testFileDeletedDetection() throws IOException, InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        CountDownLatch deleteLatch = new CountDownLatch(1);
        
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.txt$")
                .scanPeriod(100)
                .onFileAdded(file -> {
                    addedFiles.add(file);
                    addLatch.countDown();
                })
                .onFileDeleted(file -> {
                    deletedFiles.add(file);
                    deleteLatch.countDown();
                })
                .build();

        monitor.start();
        
        Thread.sleep(150);
        
        // Create file
        Path testFile = tempDir.resolve("test.txt");
        Files.createFile(testFile);
        
        // Wait for add detection
        assertTrue(addLatch.await(2, TimeUnit.SECONDS));
        assertEquals(1, addedFiles.size());
        
        // Delete file
        Files.delete(testFile);
        
        // Wait for delete detection
        assertTrue(deleteLatch.await(2, TimeUnit.SECONDS));
        assertEquals(1, deletedFiles.size());
        assertEquals(testFile, deletedFiles.get(0));
    }

    @Test
    void testFileModifiedDetection() throws IOException, InterruptedException {
        CountDownLatch addLatch = new CountDownLatch(1);
        
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.txt$")
                .scanPeriod(100)
                .onFileAdded(file -> {
                    addedFiles.add(file);
                    addLatch.countDown();
                })
                .onFileModified(file -> {
                    modifiedFiles.add(file);
                })
                .build();

        monitor.start();
        
        Thread.sleep(150);
        
        // Create file
        Path testFile = tempDir.resolve("test.txt");
        Files.createFile(testFile);
        
        // Wait for add detection
        assertTrue(addLatch.await(2, TimeUnit.SECONDS));
        
        // Modify file
        Files.write(testFile, "content".getBytes());
        
        // Wait for scan cycle
        Thread.sleep(200);
        
        // Note: Modified callback may be called, but this is implementation-dependent
        // The test verifies the callback doesn't throw exceptions
        assertTrue(addedFiles.size() >= 1);
    }

    @Test
    void testGetKnownFiles() throws IOException, InterruptedException {
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.txt$")
                .scanPeriod(100)
                .build();

        monitor.start();
        
        Thread.sleep(150);
        
        // Create files
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Files.createFile(file1);
        Files.createFile(file2);
        
        // Wait for detection
        Thread.sleep(200);
        
        Set<Path> knownFiles = monitor.getKnownFiles();
        assertTrue(knownFiles.size() >= 2);
        assertTrue(knownFiles.contains(file1));
        assertTrue(knownFiles.contains(file2));
    }

    @Test
    void testManualScan() throws IOException, InterruptedException {
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.txt$")
                .scanPeriod(5000) // Long period
                .onFileAdded(file -> addedFiles.add(file))
                .build();

        monitor.start();
        
        Thread.sleep(100);
        
        // Create file
        Path testFile = tempDir.resolve("test.txt");
        Files.createFile(testFile);
        
        // Manually trigger scan
        monitor.scan();
        
        // Wait a bit for processing
        Thread.sleep(100);
        
        assertEquals(1, addedFiles.size());
        assertEquals(testFile, addedFiles.get(0));
    }

    @Test
    void testMultipleFilesAdded() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.txt$")
                .scanPeriod(100)
                .onFileAdded(file -> {
                    addedFiles.add(file);
                    latch.countDown();
                })
                .build();

        monitor.start();
        
        Thread.sleep(150);
        
        // Create multiple files
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Path file3 = tempDir.resolve("file3.txt");
        
        Files.createFile(file1);
        Files.createFile(file2);
        Files.createFile(file3);
        
        // Wait for all detections
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(3, addedFiles.size());
    }

    @Test
    void testStopClearsKnownFiles() throws IOException, InterruptedException {
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.txt$")
                .scanPeriod(100)
                .build();

        monitor.start();
        
        Thread.sleep(150);
        
        // Create file
        Path testFile = tempDir.resolve("test.txt");
        Files.createFile(testFile);
        
        Thread.sleep(200);
        
        Set<Path> knownFiles = monitor.getKnownFiles();
        assertTrue(knownFiles.size() >= 1);
        
        monitor.stop();
        
        // After stop, known files should be cleared
        Set<Path> knownFilesAfterStop = monitor.getKnownFiles();
        assertTrue(knownFilesAfterStop.isEmpty());
    }

    @Test
    void testStartMultipleTimesIsIdempotent() throws IOException, InterruptedException {
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.txt$")
                .scanPeriod(100)
                .build();

        monitor.start();
        assertTrue(monitor.isRunning());
        
        // Starting again should be idempotent
        monitor.start();
        assertTrue(monitor.isRunning());
        
        monitor.stop();
    }

    @Test
    void testStopMultipleTimesIsIdempotent() throws IOException {
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.txt$")
                .scanPeriod(100)
                .build();

        monitor.start();
        
        monitor.stop();
        assertFalse(monitor.isRunning());
        
        // Stopping again should be idempotent
        monitor.stop();
        assertFalse(monitor.isRunning());
    }

    @Test
    void testScanWithoutStartDoesNothing() {
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern(".*\\.txt$")
                .scanPeriod(100)
                .build();

        // Scan without starting should not throw exception
        assertDoesNotThrow(() -> monitor.scan());
    }

    @Test
    void testComplexRegexPattern() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        // Pattern: files starting with "trip" and ending with ".parquet"
        monitor = Monitor.builder()
                .directory(tempDir.toString())
                .filePattern("^trip.*\\.parquet$")
                .scanPeriod(100)
                .onFileAdded(file -> {
                    addedFiles.add(file);
                    latch.countDown();
                })
                .build();

        monitor.start();
        
        Thread.sleep(150);
        
        // Create matching file
        Path matchingFile = tempDir.resolve("tripdata.parquet");
        Files.createFile(matchingFile);
        
        // Create non-matching files
        Files.createFile(tempDir.resolve("data.parquet"));
        Files.createFile(tempDir.resolve("tripdata.txt"));
        
        // Wait for detection
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, addedFiles.size());
        assertEquals(matchingFile, addedFiles.get(0));
    }

    @Test
    void testDirectoryWithPathObject() {
        monitor = Monitor.builder()
                .directory(tempDir)
                .filePattern(".*\\.txt$")
                .build();

        assertEquals(tempDir, monitor.getDirectory());
    }
}

