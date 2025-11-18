class GhActions < Formula
  desc "GitHub Actions workflow monitoring and management CLI"
  homepage "https://github.com/kwr14/langs"
  version "0.1.0"
  
  on_macos do
    if Hardware::CPU.intel?
      url "https://github.com/kwr14/langs/releases/download/v0.1.0/gh-actions-macos-x86_64"
      sha256 "REPLACE_WITH_ACTUAL_SHA256"
    elsif Hardware::CPU.arm?
      url "https://github.com/kwr14/langs/releases/download/v0.1.0/gh-actions-macos-aarch64"
      sha256 "REPLACE_WITH_ACTUAL_SHA256"
    end
  end

  on_linux do
    if Hardware::CPU.intel?
      url "https://github.com/kwr14/langs/releases/download/v0.1.0/gh-actions-linux-x86_64"
      sha256 "REPLACE_WITH_ACTUAL_SHA256"
    elsif Hardware::CPU.arm?
      url "https://github.com/kwr14/langs/releases/download/v0.1.0/gh-actions-linux-aarch64"
      sha256 "REPLACE_WITH_ACTUAL_SHA256"
    end
  end

  def install
    bin.install "gh-actions-#{OS.kernel_name.downcase}-#{Hardware::CPU.arch}" => "gh-actions"
  end

  test do
    assert_match "GitHub Actions CLI", shell_output("#{bin}/gh-actions version")
  end

  def caveats
    <<~EOS
      To get started:
        1. Run: gh-actions init
        2. Edit ~/.github-actions-cli.conf and add your GitHub token
        3. Run: gh-actions dashboard -o <owner> -r <repo>

      For help: gh-actions --help
    EOS
  end
end

