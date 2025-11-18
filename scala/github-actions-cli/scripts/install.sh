#!/usr/bin/env bash
# GitHub Actions CLI Installation Script
# This script installs the GitHub Actions CLI tool

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
INSTALL_DIR="${INSTALL_DIR:-$HOME/.local/bin}"
REPO="kwr14/langs"
BINARY_NAME="gh-actions"

# Detect OS and architecture
OS="$(uname -s)"
ARCH="$(uname -m)"

echo -e "${GREEN}GitHub Actions CLI Installer${NC}"
echo "================================"
echo ""

# Check if Java is installed (required for JAR version)
if ! command -v java &> /dev/null; then
    echo -e "${YELLOW}Warning: Java not found. You'll need Java 11+ to run the JAR version.${NC}"
    echo "Install Java from: https://adoptium.net/"
    echo ""
fi

# Create install directory if it doesn't exist
mkdir -p "$INSTALL_DIR"

# Download the latest release
echo "Downloading GitHub Actions CLI..."
LATEST_RELEASE=$(curl -s "https://api.github.com/repos/$REPO/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')

if [ -z "$LATEST_RELEASE" ]; then
    echo -e "${YELLOW}No releases found. Building from source...${NC}"
    
    # Check if sbt is installed
    if ! command -v sbt &> /dev/null; then
        echo -e "${RED}Error: sbt not found. Please install sbt first.${NC}"
        echo "Visit: https://www.scala-sbt.org/download.html"
        exit 1
    fi
    
    # Clone and build
    TEMP_DIR=$(mktemp -d)
    cd "$TEMP_DIR"
    git clone "https://github.com/$REPO.git"
    cd langs/scala/github-actions-cli
    
    echo "Building fat JAR..."
    sbt "cli/assembly"
    
    # Copy the JAR
    cp cli/target/scala-3.5.0/github-actions-cli.jar "$INSTALL_DIR/"
    
    # Create wrapper script
    cat > "$INSTALL_DIR/$BINARY_NAME" << 'EOF'
#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec java -jar "$SCRIPT_DIR/github-actions-cli.jar" "$@"
EOF
    
    chmod +x "$INSTALL_DIR/$BINARY_NAME"
    
    # Cleanup
    cd ~
    rm -rf "$TEMP_DIR"
else
    echo "Latest release: $LATEST_RELEASE"
    
    # Try to download native binary first
    DOWNLOAD_URL="https://github.com/$REPO/releases/download/$LATEST_RELEASE/gh-actions-$OS-$ARCH"
    
    if curl -fsSL "$DOWNLOAD_URL" -o "$INSTALL_DIR/$BINARY_NAME" 2>/dev/null; then
        echo "Downloaded native binary"
        chmod +x "$INSTALL_DIR/$BINARY_NAME"
    else
        # Fall back to JAR
        echo "Native binary not available, downloading JAR..."
        JAR_URL="https://github.com/$REPO/releases/download/$LATEST_RELEASE/github-actions-cli.jar"
        
        curl -fsSL "$JAR_URL" -o "$INSTALL_DIR/github-actions-cli.jar"
        
        # Create wrapper script
        cat > "$INSTALL_DIR/$BINARY_NAME" << 'EOF'
#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec java -jar "$SCRIPT_DIR/github-actions-cli.jar" "$@"
EOF
        
        chmod +x "$INSTALL_DIR/$BINARY_NAME"
    fi
fi

echo ""
echo -e "${GREEN}Installation complete!${NC}"
echo ""
echo "The binary is installed at: $INSTALL_DIR/$BINARY_NAME"
echo ""

# Check if install directory is in PATH
if [[ ":$PATH:" != *":$INSTALL_DIR:"* ]]; then
    echo -e "${YELLOW}Warning: $INSTALL_DIR is not in your PATH${NC}"
    echo ""
    echo "Add it to your PATH by adding this line to your shell profile:"
    echo "  export PATH=\"\$PATH:$INSTALL_DIR\""
    echo ""
    
    # Detect shell and suggest profile file
    if [ -n "$BASH_VERSION" ]; then
        echo "For bash, add it to: ~/.bashrc or ~/.bash_profile"
    elif [ -n "$ZSH_VERSION" ]; then
        echo "For zsh, add it to: ~/.zshrc"
    fi
    echo ""
fi

echo "Next steps:"
echo "  1. Run: $BINARY_NAME init"
echo "  2. Edit ~/.github-actions-cli.conf and add your GitHub token"
echo "  3. Run: $BINARY_NAME dashboard -o <owner> -r <repo>"
echo ""
echo "For help, run: $BINARY_NAME --help"

