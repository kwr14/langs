.PHONY: help compile test clean assembly native-image install run-dashboard run-list docker-build docker-run

# Default target
help:
	@echo "GitHub Actions CLI - Build Targets"
	@echo "==================================="
	@echo ""
	@echo "Development:"
	@echo "  make compile        - Compile all modules"
	@echo "  make test           - Run all tests"
	@echo "  make clean          - Clean build artifacts"
	@echo ""
	@echo "Packaging:"
	@echo "  make assembly       - Build fat JAR"
	@echo "  make native-image   - Build GraalVM native image"
	@echo ""
	@echo "Installation:"
	@echo "  make install        - Install to ~/.local/bin"
	@echo ""
	@echo "Running:"
	@echo "  make run-dashboard  - Run dashboard (requires OWNER and REPO env vars)"
	@echo "  make run-list       - List workflow runs (requires OWNER and REPO env vars)"
	@echo ""
	@echo "Docker:"
	@echo "  make docker-build   - Build Docker image"
	@echo "  make docker-run     - Run in Docker container"

# Development targets
compile:
	sbt compile

test:
	sbt test

clean:
	sbt clean
	rm -rf target
	rm -rf */target
	rm -rf */*/target

# Packaging targets
assembly:
	sbt "cli/assembly"
	@echo ""
	@echo "✅ Fat JAR created at: cli/target/scala-3.5.0/github-actions-cli.jar"
	@echo ""
	@echo "Run with: java -jar cli/target/scala-3.5.0/github-actions-cli.jar"

native-image:
	sbt "cli/nativeImage"
	@echo ""
	@echo "✅ Native image created at: cli/target/native-image/gh-actions"
	@echo ""
	@echo "Run with: ./cli/target/native-image/gh-actions"

# Installation target
install: assembly
	@mkdir -p ~/.local/bin
	@cp cli/target/scala-3.5.0/github-actions-cli.jar ~/.local/bin/
	@echo '#!/usr/bin/env bash' > ~/.local/bin/gh-actions
	@echo 'exec java -jar "$$HOME/.local/bin/github-actions-cli.jar" "$$@"' >> ~/.local/bin/gh-actions
	@chmod +x ~/.local/bin/gh-actions
	@echo ""
	@echo "✅ Installed to ~/.local/bin/gh-actions"
	@echo ""
	@echo "Make sure ~/.local/bin is in your PATH:"
	@echo "  export PATH=\"\$$PATH:\$$HOME/.local/bin\""

install-native: native-image
	@mkdir -p ~/.local/bin
	@cp cli/target/native-image/gh-actions ~/.local/bin/
	@chmod +x ~/.local/bin/gh-actions
	@echo ""
	@echo "✅ Installed native binary to ~/.local/bin/gh-actions"

# Running targets
run-dashboard:
	@if [ -z "$(OWNER)" ] || [ -z "$(REPO)" ]; then \
		echo "Error: OWNER and REPO environment variables required"; \
		echo "Usage: make run-dashboard OWNER=octocat REPO=Hello-World"; \
		exit 1; \
	fi
	sbt "cli/run dashboard -o $(OWNER) -r $(REPO)"

run-list:
	@if [ -z "$(OWNER)" ] || [ -z "$(REPO)" ]; then \
		echo "Error: OWNER and REPO environment variables required"; \
		echo "Usage: make run-list OWNER=octocat REPO=Hello-World"; \
		exit 1; \
	fi
	sbt "cli/run list -o $(OWNER) -r $(REPO)"

# Docker targets
docker-build:
	docker build -t gh-actions-cli:latest -f Dockerfile .

docker-run:
	docker run -it --rm \
		-e GITHUB_TOKEN=$(GITHUB_TOKEN) \
		gh-actions-cli:latest \
		dashboard -o $(OWNER) -r $(REPO)

# Format code
format:
	sbt scalafmtAll

# Check formatting
format-check:
	sbt scalafmtCheck

# Release targets
release-jar: clean test assembly
	@echo "✅ Release JAR ready at: cli/target/scala-3.5.0/github-actions-cli.jar"

release-native: clean test native-image
	@echo "✅ Release native binary ready at: cli/target/native-image/gh-actions"

# CI targets
ci: clean compile test assembly
	@echo "✅ CI build complete"

