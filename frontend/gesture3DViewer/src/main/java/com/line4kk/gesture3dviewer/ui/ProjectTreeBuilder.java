package com.line4kk.gesture3dviewer.ui;

import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class ProjectTreeBuilder {
    public static TreeItem<File> buildTree(File rootDir) {
        TreeItem<File> rootItem = new TreeItem<>(rootDir);
        rootItem.setExpanded(true);
        populateTree(rootItem, rootDir);
        return rootItem;
    }

    private static void populateTree(TreeItem<File> parent, File dir) {
        File[] children = dir.listFiles();
        if (children == null) return;

        // Сначала папки, потом файлы
        Arrays.sort(children, Comparator
                .comparing(File::isFile)  // false (папки) < true (файлы)
                .thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        for (File child : children) {
            TreeItem<File> item = new TreeItem<>(child);
            parent.getChildren().add(item);

            if (child.isDirectory()) {
                populateTree(item, child);
            }
        }
    }
}