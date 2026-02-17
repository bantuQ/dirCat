#!/bin/bash
# Helper script to install DirCat Flatpak

set -e

if [ ! -f "dircat.flatpak" ]; then
    echo "Error: dircat.flatpak not found"
    echo "Please run flatpak/build-flatpak.sh first"
    exit 1
fi

echo "Installing DirCat Flatpak..."
flatpak install --user dircat.flatpak

echo ""
echo "Installation complete!"
echo ""
echo "Run with: flatpak run com.github.bantuQ.DirCat"
echo "Or find 'DirCat' in your applications menu"
