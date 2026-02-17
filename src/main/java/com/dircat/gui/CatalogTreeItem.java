package com.dircat.gui;

import com.dircat.model.CatalogEntry;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * Custom TreeItem for displaying CatalogEntry objects in a TreeView.
 */
public class CatalogTreeItem extends TreeItem<CatalogEntry> {

    private boolean isLeaf;
    private boolean isFirstTimeChildren = true;
    private boolean isFirstTimeLeaf = true;

    /**
     * Creates a new CatalogTreeItem.
     *
     * @param entry The catalog entry to display
     */
    public CatalogTreeItem(CatalogEntry entry) {
        super(entry);
    }

    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeaf) {
            isFirstTimeLeaf = false;
            CatalogEntry entry = getValue();
            isLeaf = entry != null && !entry.isDirectory();
        }
        return isLeaf;
    }

    @Override
    public ObservableList<TreeItem<CatalogEntry>> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;
            super.getChildren().setAll(buildChildren());
        }
        return super.getChildren();
    }

    /**
     * Builds the children tree items from the catalog entry's children.
     *
     * @return List of child tree items
     */
    private ObservableList<TreeItem<CatalogEntry>> buildChildren() {
        CatalogEntry entry = getValue();
        if (entry != null && entry.getChildren() != null && !entry.getChildren().isEmpty()) {
            return javafx.collections.FXCollections.observableArrayList(
                    entry.getChildren().stream()
                            .map(CatalogTreeItem::new)
                            .toArray(CatalogTreeItem[]::new)
            );
        }
        return javafx.collections.FXCollections.emptyObservableList();
    }
}
