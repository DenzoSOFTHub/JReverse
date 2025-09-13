package it.denzosoft.jreverse.core.model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Value object representing the location of a JAR file.
 * Immutable and validates file existence and extension.
 */
public final class JarLocation {
    
    private final Path jarPath;
    
    public JarLocation(Path jarPath) {
        this.jarPath = validateJarPath(jarPath);
    }
    
    public JarLocation(String jarPath) {
        this(Paths.get(jarPath));
    }
    
    public Path getPath() {
        return jarPath;
    }
    
    public String getAbsolutePath() {
        return jarPath.toAbsolutePath().toString();
    }
    
    public String getFileName() {
        Path fileName = jarPath.getFileName();
        return fileName != null ? fileName.toString() : "";
    }
    
    public long getFileSize() {
        File file = jarPath.toFile();
        return file.exists() ? file.length() : 0L;
    }
    
    public boolean exists() {
        return jarPath.toFile().exists();
    }
    
    public boolean isReadable() {
        File file = jarPath.toFile();
        return file.exists() && file.canRead();
    }
    
    public boolean isJarFile() {
        String fileName = getFileName().toLowerCase();
        return fileName.endsWith(".jar") || fileName.endsWith(".war");
    }
    
    private Path validateJarPath(Path path) {
        Objects.requireNonNull(path, "JAR path cannot be null");
        
        String fileName = path.getFileName() != null ? path.getFileName().toString() : "";
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("JAR path must point to a file");
        }
        
        String lowerFileName = fileName.toLowerCase();
        if (!lowerFileName.endsWith(".jar") && !lowerFileName.endsWith(".war")) {
            throw new IllegalArgumentException("File must have .jar or .war extension: " + fileName);
        }
        
        return path.toAbsolutePath().normalize();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        JarLocation that = (JarLocation) obj;
        return Objects.equals(jarPath, that.jarPath);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jarPath);
    }
    
    @Override
    public String toString() {
        return "JarLocation{" +
                "path='" + jarPath + '\'' +
                ", exists=" + exists() +
                ", size=" + getFileSize() + " bytes" +
                '}';
    }
}