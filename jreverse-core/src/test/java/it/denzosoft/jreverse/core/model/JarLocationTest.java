package it.denzosoft.jreverse.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class JarLocationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCreateValidJarLocation() throws IOException {
        Path jarFile = tempDir.resolve("test.jar");
        Files.createFile(jarFile);
        
        JarLocation location = new JarLocation(jarFile);
        
        assertEquals(jarFile.toAbsolutePath(), location.getPath());
        assertEquals("test.jar", location.getFileName());
        assertTrue(location.exists());
        assertTrue(location.isReadable());
        assertTrue(location.isJarFile());
    }

    @Test
    void shouldCreateValidWarLocation() throws IOException {
        Path warFile = tempDir.resolve("test.war");
        Files.createFile(warFile);
        
        JarLocation location = new JarLocation(warFile);
        
        assertTrue(location.isJarFile());
        assertEquals("test.war", location.getFileName());
    }

    @Test
    void shouldThrowExceptionForNullPath() {
        assertThrows(NullPointerException.class, () -> {
            new JarLocation((Path) null);
        });
    }

    @Test
    void shouldThrowExceptionForInvalidExtension() {
        Path txtFile = tempDir.resolve("test.txt");
        
        assertThrows(IllegalArgumentException.class, () -> {
            new JarLocation(txtFile);
        });
    }

    @Test
    void shouldThrowExceptionForEmptyFileName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new JarLocation(tempDir);
        });
    }

    @Test
    void shouldCreateFromStringPath() throws IOException {
        Path jarFile = tempDir.resolve("test.jar");
        Files.createFile(jarFile);
        
        JarLocation location = new JarLocation(jarFile.toString());
        
        assertEquals(jarFile.toAbsolutePath(), location.getPath());
        assertTrue(location.exists());
    }

    @Test
    void shouldReturnCorrectFileSize() throws IOException {
        Path jarFile = tempDir.resolve("test.jar");
        byte[] content = "test content".getBytes();
        Files.write(jarFile, content);
        
        JarLocation location = new JarLocation(jarFile);
        
        assertEquals(content.length, location.getFileSize());
    }

    @Test
    void shouldReturnZeroSizeForNonExistentFile() {
        Path nonExistentJar = tempDir.resolve("nonexistent.jar");
        
        JarLocation location = new JarLocation(nonExistentJar);
        
        assertFalse(location.exists());
        assertEquals(0L, location.getFileSize());
        assertFalse(location.isReadable());
    }

    @Test
    void shouldBeEqualBasedOnPath() throws IOException {
        Path jarFile = tempDir.resolve("test.jar");
        Files.createFile(jarFile);
        
        JarLocation location1 = new JarLocation(jarFile);
        JarLocation location2 = new JarLocation(jarFile.toString());
        
        assertEquals(location1, location2);
        assertEquals(location1.hashCode(), location2.hashCode());
    }

    @Test
    void shouldNormalizePath() throws IOException {
        Path jarFile = tempDir.resolve("subdir/../test.jar");
        Path parentDir = jarFile.getParent().getParent();
        Files.createDirectories(parentDir.resolve("subdir"));
        Files.createFile(parentDir.resolve("test.jar"));
        
        JarLocation location = new JarLocation(jarFile);
        
        // Path should be normalized (no ../subdir)
        assertFalse(location.getAbsolutePath().contains(".."));
        assertTrue(location.getAbsolutePath().endsWith("test.jar"));
    }
}