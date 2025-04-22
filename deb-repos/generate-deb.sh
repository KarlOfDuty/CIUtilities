#!/usr/bin/bash -e

# Usage: Run this script from the root of the repository.

PACKAGE_VERSION="1.0.1"
PACKAGE_NAME="karlofduty-repo"

export REPO_ROOT="$PWD"

# Check what the package build dir should be called, mostly exists so parallel CI jobs don't clash
if [[ -z "$PACKAGE_ROOT" ]]; then
  PACKAGE_ROOT="$REPO_ROOT/.dpkg-deb"
fi

# Export various environment variables so the packaging scripts can use them
export PACKAGE_VERSION
export PACKAGE_NAME
export PACKAGE_ROOT

export FULL_PACKAGE_NAME="${PACKAGE_NAME}_$PACKAGE_VERSION"
export PACKAGE_DIR="$PACKAGE_ROOT/$FULL_PACKAGE_NAME"

# Remove old package build dir if it exists
rm -rf "$PACKAGE_ROOT"

# Create source code tarball as the debian packaging system likes to have one
git archive --format=tar.gz HEAD > "${FULL_PACKAGE_NAME}.orig.tar.gz"

# Create the package build directory and extract the source code into it
mkdir -p "$PACKAGE_DIR"
cd "$PACKAGE_DIR" || exit 1
mv "$REPO_ROOT/$FULL_PACKAGE_NAME.orig.tar.gz" "$PACKAGE_ROOT/"
tar -xzf "$PACKAGE_ROOT/$FULL_PACKAGE_NAME.orig.tar.gz"

# Copy the debian package files into the package build directory and replace variables
cp -r "$REPO_ROOT/deb-repos/debian" "$PACKAGE_DIR/"
sed -i 's/PACKAGE_NAME/'"$PACKAGE_NAME"'/' "$PACKAGE_DIR/debian/control"
sed -i 's/PACKAGE_NAME/'"$PACKAGE_NAME"'/' "$PACKAGE_DIR/debian/changelog"
sed -i 's/PACKAGE_VERSION/'"$PACKAGE_VERSION"'/' "$PACKAGE_DIR/debian/changelog"
sed -i 's/DIST/'"release"'/' "$PACKAGE_DIR/debian/changelog"

# Set packager name and email if not explicitly set
if [[ -z "$DEBEMAIL" || -z "$DEBEMAIL" ]]; then
  echo -e "You must set DEBFULLNAME and DEBEMAIL. Example:\nexport DEBFULLNAME=\"Karl Essinger\"\nexport DEBEMAIL=\"xkaess22@gmail.com\""
  exit 1
fi

# Build the .deb package
dpkg-buildpackage -us -uc