package com.example.port.bundle.Controller;

import com.example.port.bundle.Model.MenuItem;
import com.example.port.bundle.Model.RequestDTO;
import com.example.port.bundle.Repository.MenuItemRepository;
import com.example.port.bundle.Service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

@RestController
@RequestMapping("/api/menu")
public class MenuController {
    @Autowired
    private MenuService menuService;
    @Autowired
    private MenuItemRepository menuItemRepository;
    // Simulated session to track user's current context
    private Map<String, Stack<Long>> userSessionContext = new HashMap<>();

    // Create a new menu item (with optional parent)
    @PostMapping
    public MenuItem createMenuItem(@RequestBody MenuItem menuItem,
                                   @RequestParam(required = false) Long parentId) {
        return menuService.createMenuItem(menuItem, parentId);
    }
    //    Return top-level menu items in formatted list
    @GetMapping
    public List<String> getAllTopLevelMenuItems() {
        return menuService.getFormattedTopLevelMenuItems();
    }

    // Create a submenu for a specific menu item
    @PostMapping("/{menuItemId}/submenu")
    public MenuItem createSubMenuItem(@PathVariable Long menuItemId, @RequestBody MenuItem subMenuItem) {
        return menuService.createSubMenuItem(menuItemId, subMenuItem);
    }

    @PostMapping("/{menuItemId}/submenu/{submenuItemId}")
    public MenuItem createSubSubMenuItem(@PathVariable Long menuItemId,
                                         @PathVariable Long submenuItemId,
                                         @RequestBody MenuItem subSubMenuItem) {
        return menuService.createSubMenuToSubMenu(menuItemId, submenuItemId, subSubMenuItem);
    }
    // Return submenu list in formatted form
    @GetMapping("/{menuItemId}/submenu")
    public List getSubMenuList(@PathVariable Long menuItemId) {
        return menuService.getSubMenuList(menuItemId);
    }
    @PostMapping("/process-request")
    public ResponseEntity<?> processRequest(@RequestBody RequestDTO request) {
        Integer newRequest = request.getNewRequest();
        Integer input = request.getInput();
        String userId = "defaultUser";

        // Retrieve or initialize the navigation stack for the user
        Stack<Long> navigationStack = userSessionContext.computeIfAbsent(userId, k -> new Stack<>());

        try {
            if (newRequest == 1) {
                // Reset to the main menu and clear navigation history
                navigationStack.clear();
                List<String> mainMenu = menuService.getFormattedTopLevelMenuItems();
                return ResponseEntity.ok(mainMenu);
            }

            if (newRequest == 0) {
                if (input == null || input < 0) {
                    return ResponseEntity.badRequest().body("Input cannot be null or negative.");
                }

                if (navigationStack.isEmpty()) {
                    // At the main menu
                    if (input == 0) {
                        // Navigate to the next submenu of index 0
                        MenuItem nextSubMenu = menuService.getMenuItemByIndex(0, null);
                        navigationStack.push(nextSubMenu.getId());
                        List<String> nextSubMenuList = menuService.getSubMenuList(nextSubMenu.getId());
                        return ResponseEntity.ok(nextSubMenuList);
                    } else {
                        // Navigate to the selected submenu
                        MenuItem selectedMenuItem = menuService.getMenuItemByIndex(input, null);
                        navigationStack.push(selectedMenuItem.getId());
                        List<String> subMenuList = menuService.getSubMenuList(selectedMenuItem.getId());
                        return ResponseEntity.ok(subMenuList);
                    }
                } else {
                    // In a submenu
                    if (input == 0) {
                        // Go back to the previous menu
                        navigationStack.pop();

                        if (navigationStack.isEmpty()) {
                            // If back to the main menu
                            List<String> mainMenu = menuService.getFormattedTopLevelMenuItems();
                            return ResponseEntity.ok(mainMenu);
                        } else {
                            // Return to the previous submenu
                            Long previousMenuId = navigationStack.peek();
                            List<String> previousSubMenu = menuService.getSubMenuList(previousMenuId);
                            return ResponseEntity.ok(previousSubMenu);
                        }
                    } else {
                        // Navigate to the deeper submenu
                        Long currentMenuId = navigationStack.peek();
                        MenuItem selectedSubMenuItem = menuService.getMenuItemByIndex(input - 1, currentMenuId);
                        navigationStack.push(selectedSubMenuItem.getId());
                        List<String> deeperSubMenuList = menuService.getSubMenuList(selectedSubMenuItem.getId());
                        return ResponseEntity.ok(deeperSubMenuList);
                    }
                }
            } else {
                return ResponseEntity.badRequest().body("Invalid newRequest value. Use 1 for main menu, 0 for submenu.");
            }
        } catch (RuntimeException e) {
            // Handle runtime exceptions, such as invalid input indices
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/{menuItemId}/submenu-with-debug")
    public List<MenuItem> getSubMenuItemsWithDebug(@PathVariable Long menuItemId) {
        return menuService.getSubMenuItemsWithDebug(menuItemId);
    }
}