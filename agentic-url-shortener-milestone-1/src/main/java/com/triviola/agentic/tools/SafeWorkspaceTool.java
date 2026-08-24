package com.triviola.agentic.tools;

import com.triviola.agentic.agent.output.FileChange;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Component
public class SafeWorkspaceTool {
    private final Path root = Path.of("managed-workspace", "url-shortener").toAbsolutePath().normalize();

    public List<Path> applyUpserts(List<FileChange> changes) throws IOException {
        Files.createDirectories(root);
        for (FileChange change : changes) {
            if (!"UPSERT".equals(change.operation())) {
                throw new SecurityException("Only UPSERT is permitted");
            }
            Path target = resolveSafe(change.path());
            Files.createDirectories(target.getParent());
            Files.writeString(target, change.content(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        return changes.stream().map(change -> resolveSafe(change.path())).toList();
    }

    public String read(String relativePath) throws IOException {
        return Files.readString(resolveSafe(relativePath), StandardCharsets.UTF_8);
    }

    private Path resolveSafe(String relativePath) {
        if (relativePath == null || relativePath.isBlank() || Path.of(relativePath).isAbsolute()) {
            throw new SecurityException("Workspace path must be a non-empty relative path");
        }
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) throw new SecurityException("Path escapes managed workspace");
        Path current = root;
        for (Path segment : root.relativize(target)) {
            current = current.resolve(segment);
            if (Files.isSymbolicLink(current)) {
                throw new SecurityException("Symbolic links are not permitted in managed paths");
            }
        }
        return target;
    }
}
