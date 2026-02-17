# DirCat - Disk Catalog

A JavaFX application for cataloging file systems. DirCat allows you to scan directories, create catalogs of your disk contents, and save them in compressed format for later reference.

![DirCat Screenshot](screenshot.png)
*Screenshot placeholder - application interface*

## Features

### Current Features (v1.0)
- ✅ **Recursive Directory Scanning** - Scan any directory and build a complete catalog
- ✅ **TreeView Display** - Browse scanned directories in an intuitive tree structure
- ✅ **Compressed Storage** - Save catalogs in GZIP-compressed XML format (.cat.gz)
- ✅ **Load Saved Catalogs** - Load and browse previously saved catalogs
- ✅ **File Metadata** - Captures file name, size, path, and modification date
- ✅ **Progress Tracking** - Real-time progress bar during scanning
- ✅ **Status Information** - Shows file count and total size

### Planned Features
- 🔜 **Search Functionality** - Search for files and folders within catalogs
- 🔜 **MP3 Tag Reading** - Extract and display ID3 tags from MP3 files
- 🔜 **File Content Storage** - Store file contents for text files
- 🔜 **Duplicate Detection** - Find duplicate files across catalogs
- 🔜 **Compare Catalogs** - Compare two catalogs to find differences
- 🔜 **Export to HTML/CSV** - Export catalog data in various formats

## Technology Stack

- **Java 17** - Modern Java features
- **JavaFX 17** - Rich desktop GUI framework
- **JAXB** - XML serialization/deserialization
- **Maven** - Build and dependency management
- **JUnit 5** - Unit testing framework

## Building and Running

### Prerequisites

- Java Development Kit (JDK) 17 or newer
- Apache Maven 3.6 or newer

### Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/bantuQ/dirCat.git
   cd dirCat
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```

### Running Tests

```bash
mvn test
```

## Usage

1. **Scan a Directory**
   - Click "Scan Directory" button
   - Select the folder you want to catalog
   - Wait for the scan to complete

2. **Browse the Catalog**
   - Expand/collapse folders in the tree view
   - View file information in the tree

3. **Save a Catalog**
   - Click "Save Catalog" button
   - Choose location and filename (will be saved as .cat.gz)

4. **Load a Catalog**
   - Click "Load Catalog" button
   - Select a previously saved .cat.gz file
   - Browse the loaded catalog

## Project Structure

```
src/
├── main/
│   ├── java/com/dircat/
│   │   ├── Main.java                    # Application entry point
│   │   ├── gui/
│   │   │   ├── MainController.java      # JavaFX controller
│   │   │   └── CatalogTreeItem.java     # Custom TreeItem
│   │   ├── scanner/
│   │   │   ├── FileSystemScanner.java   # Directory scanning
│   │   │   └── ScanProgress.java        # Progress callback interface
│   │   ├── model/
│   │   │   ├── CatalogEntry.java        # File/folder model
│   │   │   ├── Catalog.java             # Root catalog model
│   │   │   └── LocalDateTimeAdapter.java # JAXB date adapter
│   │   └── storage/
│   │       ├── XmlStorage.java          # XML serialization
│   │       └── GzipStorage.java         # GZIP compression
│   └── resources/
│       ├── fxml/
│       │   └── main.fxml                # JavaFX layout
│       └── css/
│           └── style.css                # Application styles
└── test/
    └── java/com/dircat/
        └── scanner/
            └── FileSystemScannerTest.java # Scanner tests
```

## File Format

Catalogs are saved as GZIP-compressed XML files with the `.cat.gz` extension:

```xml
<catalog name="My Disk" created="2026-02-17T10:30:00">
  <entry name="Documents" path="/home/user/Documents" isDirectory="true" size="0" modified="2026-02-15T14:20:00">
    <entry name="file1.txt" path="/home/user/Documents/file1.txt" isDirectory="false" size="1024" modified="2026-02-10T09:15:30"/>
    <entry name="file2.pdf" path="/home/user/Documents/file2.pdf" isDirectory="false" size="2048576" modified="2026-02-12T16:45:12"/>
  </entry>
</catalog>
```

## License

GNU General Public License v3.0 (GPL-3.0)

This project is inspired by the classic CDcat application and follows the same GPL license philosophy.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## Acknowledgments

Inspired by CDcat and similar disk catalog applications that help manage and catalog large file collections. 
