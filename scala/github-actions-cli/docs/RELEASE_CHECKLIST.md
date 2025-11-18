# Release Checklist

Checklist for releasing a new version of GitHub Actions CLI.

## Pre-Release

### Code Quality

- [x] All tests passing
- [x] Code formatted with scalafmt
- [x] No compiler warnings (except known deprecations)
- [x] Documentation updated
- [x] CHANGELOG.md updated

### Testing

- [x] Unit tests pass (30+ tests)
- [ ] Manual testing with real GitHub repository
- [ ] Test on multiple platforms (Linux, macOS)
- [ ] Test JAR version
- [ ] Test native image (if available)
- [ ] Test installation script
- [ ] Test Docker image

### Documentation

- [x] README.md updated
- [x] USER_GUIDE.md complete
- [x] API.md complete
- [x] QUICKSTART.md complete
- [x] CONTRIBUTING.md complete
- [x] CHANGELOG.md updated with release notes
- [x] LICENSE file present

### Build Artifacts

- [x] Fat JAR builds successfully
- [ ] Native image builds (Linux x86_64)
- [ ] Native image builds (macOS x86_64)
- [ ] Docker image builds
- [ ] All artifacts tested

### CI/CD

- [x] CI workflow configured
- [x] Release workflow configured
- [ ] CI passing on main branch
- [ ] Release workflow tested (dry run)

## Release Process

### 1. Version Bump

Update version in:
- [ ] `build.sbt` (if versioned there)
- [ ] `README.md` badges
- [ ] `CHANGELOG.md` (move from Unreleased to version)
- [ ] Any other version references

### 2. Final Commit

```bash
git add .
git commit -m "chore: prepare for v0.1.0 release"
git push origin main
```

### 3. Create Tag

```bash
git tag -a v0.1.0 -m "Release v0.1.0

First beta release of GitHub Actions CLI.

Features:
- Interactive terminal dashboard
- GitHub API client
- CLI commands (dashboard, list, show, rerun, cancel)
- Configuration management
- Auto-refresh functionality
- Keyboard navigation

See CHANGELOG.md for full details."

git push origin v0.1.0
```

### 4. Monitor Release Build

- [ ] GitHub Actions release workflow triggered
- [ ] All jobs complete successfully
- [ ] Artifacts uploaded to release
- [ ] Checksums generated
- [ ] Release notes generated

### 5. Verify Release

- [ ] Release appears on GitHub Releases page
- [ ] All artifacts present:
  - [ ] github-actions-cli.jar
  - [ ] gh-actions-linux-x86_64
  - [ ] gh-actions-macos-x86_64
  - [ ] SHA256 checksums for all
- [ ] Download and test each artifact
- [ ] Verify checksums match

### 6. Update Documentation

- [ ] Update README.md with release links
- [ ] Update installation instructions if needed
- [ ] Update Homebrew formula with new SHA256s (if publishing to tap)

### 7. Announce Release

- [ ] Create GitHub Discussion announcement
- [ ] Tweet/social media (optional)
- [ ] Blog post (optional)
- [ ] Update project website (if exists)

## Post-Release

### Verification

- [ ] Installation script works with new release
- [ ] Quick start guide works end-to-end
- [ ] No broken links in documentation
- [ ] GitHub Release page looks good

### Homebrew (Optional)

If publishing to Homebrew tap:

1. [ ] Update formula with new version and SHA256s
2. [ ] Test formula installation
3. [ ] Submit PR to tap repository
4. [ ] Verify formula works after merge

### Next Version

- [ ] Create new "Unreleased" section in CHANGELOG.md
- [ ] Plan next milestone
- [ ] Create GitHub milestone for next version
- [ ] Label issues for next version

## Rollback Plan

If critical issues are found after release:

1. **Immediate**: Add warning to README.md
2. **Quick Fix**: 
   - Fix the issue
   - Release patch version (v0.1.1)
   - Update release notes
3. **Major Issue**:
   - Mark release as pre-release
   - Add warning to release notes
   - Work on fix for next version

## Version Numbering

Follow [Semantic Versioning](https://semver.org/):

- **MAJOR** (1.0.0): Breaking changes
- **MINOR** (0.1.0): New features, backwards compatible
- **PATCH** (0.1.1): Bug fixes, backwards compatible

## Release Notes Template

```markdown
## GitHub Actions CLI v0.1.0

First beta release! 🎉

### Features

- Interactive terminal dashboard with real-time monitoring
- GitHub API client with rate limit tracking
- CLI commands: dashboard, list, show, rerun, cancel
- Configuration via file or environment variables
- Auto-refresh functionality
- Keyboard navigation with vim-style bindings
- Color-coded status indicators

### Installation

**Quick Install:**
```bash
curl -fsSL https://raw.githubusercontent.com/kwr14/langs/main/scala/github-actions-cli/scripts/install.sh | bash
```

**Manual Download:**
- [JAR (Universal)](https://github.com/kwr14/langs/releases/download/v0.1.0/github-actions-cli.jar)
- [Linux x86_64](https://github.com/kwr14/langs/releases/download/v0.1.0/gh-actions-linux-x86_64)
- [macOS x86_64](https://github.com/kwr14/langs/releases/download/v0.1.0/gh-actions-macos-x86_64)

### Documentation

- [Quick Start Guide](docs/QUICKSTART.md)
- [User Guide](docs/USER_GUIDE.md)
- [API Documentation](docs/API.md)

### Known Issues

- Native image requires GraalVM (not included in standard JDK)
- Output formatting for list/show commands uses placeholder implementation
- No ARM64 binaries yet

### What's Next

- Enhanced output formatting (JSON, table)
- ARM64 native binaries
- Additional filtering options
- Workflow logs viewing

### Contributors

Thank you to all contributors!

**Full Changelog**: https://github.com/kwr14/langs/compare/v0.0.1...v0.1.0
```

## Checklist Summary

**Pre-Release:**
- [x] Tests passing
- [x] Documentation complete
- [ ] Manual testing done
- [ ] Artifacts built and tested

**Release:**
- [ ] Version bumped
- [ ] Tag created and pushed
- [ ] Release workflow completed
- [ ] Artifacts verified

**Post-Release:**
- [ ] Announcement made
- [ ] Documentation updated
- [ ] Next version planned

---

**Note:** This checklist should be reviewed and updated for each release.

