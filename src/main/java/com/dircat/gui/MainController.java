package com.dircat.gui;

import com.dircat.model.Catalog;
import com.dircat.model.CatalogEntry;
import com.dircat.scanner.FileSystemScanner;
import com.dircat.scanner.ScanProgress;
import com.dircat.storage.GzipStorage;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

/**
 * Controller for the main JavaFX GUI.
 * Handles user interactions and coordinates scanning, saving, and loading operations.
 */
public class MainController {

    @FXML
    private TreeView<CatalogEntry> catalogTreeView;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label statusLabel;

    @FXML
    private Button scanButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button loadButton;

    @FXML
    private Button searchButton;

    private Catalog currentCatalog;
    private GzipStorage storage;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        try {
            storage = new GzipStorage();
        } catch (JAXBException e) {
            showError("Initialization Error", "Failed to initialize storage: " + e.getMessage());
        }

        // Configure tree view
        catalogTreeView.setCellFactory(tv -> new TreeCell<CatalogEntry>() {
            @Override
            protected void updateItem(CatalogEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Disable buttons initially
        saveButton.setDisable(true);
        searchButton.setDisable(true);

        updateStatus("Ready", 0, 0);
    }

    /**
     * Handles the "Scan Directory" button click.
     */
    @FXML
    private void handleScanDirectory() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Directory to Scan");
        File selectedDir = dirChooser.showDialog(scanButton.getScene().getWindow());

        if (selectedDir != null) {
            scanDirectory(selectedDir.toPath());
        }
    }

    /**
     * Scans a directory in a background thread.
     *
     * @param path Path to scan
     */
    private void scanDirectory(Path path) {
        Task<CatalogEntry> scanTask = new Task<CatalogEntry>() {
            @Override
            protected CatalogEntry call() throws Exception {
                FileSystemScanner scanner = new FileSystemScanner(false, Integer.MAX_VALUE);
                
                scanner.setProgressCallback(new ScanProgress() {
                    @Override
                    public void onProgress(String currentPath, int filesScanned, long totalSize) {
                        Platform.runLater(() -> {
                            updateProgress(filesScanned, -1);
                            updateStatus("Scanning: " + currentPath, filesScanned, totalSize);
                        });
                    }

                    @Override
                    public void onComplete(int filesScanned, long totalSize) {
                        Platform.runLater(() -> {
                            updateProgress(1, 1);
                            updateStatus("Scan complete", filesScanned, totalSize);
                        });
                    }

                    @Override
                    public void onError(String path, Exception error) {
                        Platform.runLater(() -> {
                            System.err.println("Error scanning " + path + ": " + error.getMessage());
                        });
                    }
                });

                return scanner.scan(path);
            }
        };

        scanTask.setOnSucceeded(e -> {
            CatalogEntry rootEntry = scanTask.getValue();
            currentCatalog = new Catalog(rootEntry.getName());
            currentCatalog.addEntry(rootEntry);
            
            displayCatalog(currentCatalog);
            saveButton.setDisable(false);
            searchButton.setDisable(false);
            enableButtons(true);
        });

        scanTask.setOnFailed(e -> {
            Throwable error = scanTask.getException();
            showError("Scan Error", "Failed to scan directory: " + error.getMessage());
            enableButtons(true);
        });

        progressBar.progressProperty().bind(scanTask.progressProperty());
        
        enableButtons(false);
        new Thread(scanTask).start();
    }

    /**
     * Handles the "Save Catalog" button click.
     */
    @FXML
    private void handleSaveCatalog() {
        if (currentCatalog == null) {
            showWarning("No Catalog", "Please scan a directory first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Catalog");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Catalog Files (*.cat.gz)", "*.cat.gz")
        );
        fileChooser.setInitialFileName(currentCatalog.getName() + ".cat.gz");
        
        File file = fileChooser.showSaveDialog(saveButton.getScene().getWindow());
        
        if (file != null) {
            saveCatalog(file);
        }
    }

    /**
     * Saves the catalog to a file.
     *
     * @param file File to save to
     */
    private void saveCatalog(File file) {
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                storage.save(currentCatalog, file);
                return null;
            }
        };

        saveTask.setOnSucceeded(e -> {
            updateStatus("Catalog saved to " + file.getName(), 
                    currentCatalog.getTotalFileCount(), 
                    currentCatalog.getTotalSize());
            showInfo("Success", "Catalog saved successfully.");
        });

        saveTask.setOnFailed(e -> {
            Throwable error = saveTask.getException();
            showError("Save Error", "Failed to save catalog: " + error.getMessage());
        });

        new Thread(saveTask).start();
    }

    /**
     * Handles the "Load Catalog" button click.
     */
    @FXML
    private void handleLoadCatalog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Catalog");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Catalog Files (*.cat.gz)", "*.cat.gz")
        );
        
        File file = fileChooser.showOpenDialog(loadButton.getScene().getWindow());
        
        if (file != null) {
            loadCatalog(file);
        }
    }

    /**
     * Loads a catalog from a file.
     *
     * @param file File to load from
     */
    private void loadCatalog(File file) {
        Task<Catalog> loadTask = new Task<Catalog>() {
            @Override
            protected Catalog call() throws Exception {
                return storage.load(file);
            }
        };

        loadTask.setOnSucceeded(e -> {
            currentCatalog = loadTask.getValue();
            displayCatalog(currentCatalog);
            saveButton.setDisable(false);
            searchButton.setDisable(false);
            updateStatus("Catalog loaded from " + file.getName(), 
                    currentCatalog.getTotalFileCount(), 
                    currentCatalog.getTotalSize());
        });

        loadTask.setOnFailed(e -> {
            Throwable error = loadTask.getException();
            showError("Load Error", "Failed to load catalog: " + error.getMessage());
        });

        new Thread(loadTask).start();
    }

    /**
     * Handles the "Search" button click.
     */
    @FXML
    private void handleSearch() {
        if (currentCatalog == null) {
            showWarning("No Catalog", "Please scan or load a catalog first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search");
        dialog.setHeaderText("Search in catalog");
        dialog.setContentText("Enter search term:");

        dialog.showAndWait().ifPresent(searchTerm -> {
            if (!searchTerm.isEmpty()) {
                performSearch(searchTerm);
            }
        });
    }

    /**
     * Performs a search in the current catalog.
     *
     * @param searchTerm Term to search for
     */
    private void performSearch(String searchTerm) {
        // TODO: Implement search functionality
        showInfo("Search", "Search functionality will be implemented in a future version.\nSearching for: " + searchTerm);
    }

    /**
     * Displays a catalog in the tree view.
     *
     * @param catalog Catalog to display
     */
    private void displayCatalog(Catalog catalog) {
        TreeItem<CatalogEntry> root = new TreeItem<>(null);
        root.setExpanded(true);

        if (catalog.getEntries() != null) {
            for (CatalogEntry entry : catalog.getEntries()) {
                CatalogTreeItem item = new CatalogTreeItem(entry);
                root.getChildren().add(item);
            }
        }

        catalogTreeView.setRoot(root);
        catalogTreeView.setShowRoot(false);
    }

    /**
     * Updates the status bar.
     *
     * @param message    Status message
     * @param fileCount  Number of files
     * @param totalSize  Total size in bytes
     */
    private void updateStatus(String message, int fileCount, long totalSize) {
        statusLabel.setText(String.format("%s | Files: %d | Size: %s",
                message, fileCount, formatSize(totalSize)));
    }

    /**
     * Formats a size in bytes to a human-readable string.
     *
     * @param bytes Size in bytes
     * @return Formatted string
     */
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Enables or disables buttons.
     *
     * @param enabled Whether to enable buttons
     */
    private void enableButtons(boolean enabled) {
        scanButton.setDisable(!enabled);
        loadButton.setDisable(!enabled);
        if (enabled && currentCatalog == null) {
            saveButton.setDisable(true);
            searchButton.setDisable(true);
        }
    }

    /**
     * Shows an error dialog.
     *
     * @param title   Dialog title
     * @param message Error message
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a warning dialog.
     *
     * @param title   Dialog title
     * @param message Warning message
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an information dialog.
     *
     * @param title   Dialog title
     * @param message Information message
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
