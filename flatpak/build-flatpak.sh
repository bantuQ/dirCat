#!/bin/bash
# Script to build Flatpak package for DirCat

set -e

echo "========================================="
echo "Building DirCat Flatpak Package"
echo "========================================="
echo ""

# Check if flatpak-builder is installed
if ! command -v flatpak-builder &> /dev/null; then
    echo "Error: flatpak-builder not found"
    echo "Install with: sudo apt install flatpak-builder"
    exit 1
fi

# Check if Flatpak runtime is installed
if ! flatpak list --runtime | grep -q "org.freedesktop.Platform.*23.08"; then
    echo "Installing required Flatpak runtime..."
    flatpak install -y flathub org.freedesktop.Platform//23.08 org.freedesktop.Sdk//23.08
    flatpak install -y flathub org.freedesktop.Sdk.Extension.openjdk17//23.08
fi

echo "Building Flatpak..."
echo ""

# Build the flatpak
flatpak-builder --force-clean --user --install-deps-from=flathub \
    build-dir com.github.bantuQ.DirCat.yml

echo ""
echo "Creating Flatpak bundle..."
flatpak-builder --repo=repo --force-clean build-dir com.github.bantuQ.DirCat.yml
flatpak build-bundle repo dircat.flatpak com.github.bantuQ.DirCat

echo ""
echo "========================================="
echo "Build completed successfully!"
echo "========================================="
echo ""
echo "Flatpak bundle: dircat.flatpak"
echo ""
echo "To install, run:"
echo "  flatpak install dircat.flatpak"
echo ""
echo "To run:"
echo "  flatpak run com.github.bantuQ.DirCat"
echo ""
