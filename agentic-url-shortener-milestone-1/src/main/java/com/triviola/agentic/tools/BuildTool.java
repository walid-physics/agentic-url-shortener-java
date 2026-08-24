package com.triviola.agentic.tools;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class BuildTool {
    private static final Duration TIMEOUT = Duration.ofMinutes(2);
    private final Path workspace = Path.of("managed-workspace", "url-shortener").toAbsolutePath().normalize();

    public boolean isAvailable() {
        return Files.isRegularFile(workspace.resolve("mvnw"));
    }

    public BuildResult runTests() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("./mvnw", "test", "--batch-mode", "--no-transfer-progress");
        builder.directory(workspace.toFile());
        builder.redirectErrorStream(true);
        Process process = builder.start();
        boolean finished = process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return new BuildResult(false, -1, "Test execution timed out");
        }
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new BuildResult(process.exitValue() == 0, process.exitValue(), output);
    }

    public record BuildResult(boolean successful, int exitCode, String output) {}
}
