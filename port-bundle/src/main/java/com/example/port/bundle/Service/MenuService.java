package com.example.port.bundle.Service;

import com.example.port.bundle.Model.MenuItem;
import com.example.port.bundle.Repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class MenuService {
    @Autowired
    private MenuItemRepository menuItemRepository;
    // Create a new menu item with optional parent
    public MenuItem createMenuItem(MenuItem menuItem, Long parentId) {
        if (parentId != null) {
            MenuItem parentMenu = menuItemRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent menu item not found"));
            menuItem.setParentMenu(parentMenu);
        }
        return menuItemRepository.save(menuItem);
    }
    // Get all top-level menu items (without parent)
    public List<MenuItem> getAllTopLevelMenuItems() {
        return menuItemRepository.findByParentMenuIsNull();
    }
    // Create a submenu for a specific menu item
    public MenuItem createSubMenuItem(Long menuItemId, MenuItem subMenuItem) {
        MenuItem parentMenuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Parent menu item not found with id: " + menuItemId));

        subMenuItem.setParentMenu(parentMenuItem);
        return menuItemRepository.save(subMenuItem);
    }
private List<String> formatMenuItems(List<MenuItem> menuItems, boolean isSubMenu) {
    if (isSubMenu) {
        List<MenuItem> backMenuItems = menuItems.stream()
                .filter(menuItem -> "Gusubira Inyuma".equals(menuItem.getName()))
                .collect(Collectors.toList());
        List<MenuItem> otherMenuItems = menuItems.stream()
                .filter(menuItem -> !"Gusubira Inyuma".equals(menuItem.getName()))
                .collect(Collectors.toList());
        AtomicInteger index = new AtomicInteger(1);
        List<String> formattedMenuItems = otherMenuItems.stream()
                .map(menuItem -> index.getAndIncrement() + ") " + menuItem.getName())
                .collect(Collectors.toList());
        if (!backMenuItems.isEmpty()) {
            formattedMenuItems.add("0) " + backMenuItems.get(0).getName());
        }
        return formattedMenuItems;
    } else {
        AtomicInteger index = new AtomicInteger();
        return menuItems.stream()
                .map(menuItem -> index.getAndIncrement() + ") " + menuItem.getName())
                .collect(Collectors.toList());
    }
}
    // Get submenus for a specific menu item
    public List<MenuItem> getSubMenuItems(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        return menuItem.getSubMenuItems();
    }
    // Get submenus for a specific menu item as a formatted list
    public List<String> getSubMenuList(Long menuItemId) {
        MenuItem parentMenuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + menuItemId));
        List<MenuItem> subMenuItems = parentMenuItem.getSubMenuItems();
        List<String> submenu = subMenuItems.stream()
                .map(item -> item.getId() + ") " + item.getName())
                .collect(Collectors.toList());
        return reformatMenu(submenu);

    }
    public List<String> reformatMenu(List<String> submenu) {
        // Find the item containing "Gusubira Inyuma"
        String backOption = submenu.stream()
                .filter(item -> item.contains("Gusubira Inyuma"))
                .findFirst()
                .orElse(null);
        List<String> reorderedMenu = submenu.stream()
                .filter(item -> !item.contains("Gusubira Inyuma"))
                .collect(Collectors.toList());
        AtomicInteger index = new AtomicInteger(1);
        List<String> formattedMenu = reorderedMenu.stream()
                .map(item -> index.getAndIncrement() + ") " + item.split("\\)", 2)[1].trim())
                .collect(Collectors.toList());
        if (backOption != null) {
            formattedMenu.add("0) " + backOption.split("\\)", 2)[1].trim());
        }
        return formattedMenu;
    }

    //  Return formatted top-level menu items
    public List<String> getFormattedTopLevelMenuItems() {
        List<MenuItem> topMenuItems = getAllTopLevelMenuItems();
        return formatMenuItems(topMenuItems, false); // Start from 0
    }

    public MenuItem getMenuItemByIndex(int index, Long parentId) {
        List<MenuItem> menuItems = (parentId == null) ? getAllTopLevelMenuItems()
                : getSubMenuItems(parentId);
        if (index < 0 || index >= menuItems.size()) {
            throw new RuntimeException("Menu index out of bounds.");
        }

        return menuItems.get(index);
    }

    public MenuItem createSubMenuToSubMenu(Long menuItemId, Long submenuItemId, MenuItem subSubMenuItem) {
        // Verify the main parent menu exists
        MenuItem parentMenu = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Parent menu item not found with id: " + menuItemId));
        MenuItem subMenuItem = menuItemRepository.findById(submenuItemId)
                .orElseThrow(() -> new RuntimeException("Submenu item not found with id: " + submenuItemId));
        subSubMenuItem.setParentMenu(subMenuItem);
        return menuItemRepository.save(subSubMenuItem);
    }

    public List<MenuItem> getSubMenuItemsWithDebug(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + menuItemId));

        List<MenuItem> subMenuItems = menuItem.getSubMenuItems();
        System.out.println("SubMenu IDs and Names: ");
        subMenuItems.forEach(subMenu ->
                System.out.println("ID: " + subMenu.getId() + ", Name: " + subMenu.getName()));

        return subMenuItems;
    }
}