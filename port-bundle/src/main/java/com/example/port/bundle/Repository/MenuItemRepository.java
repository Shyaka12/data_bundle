package com.example.port.bundle.Repository;

import com.example.port.bundle.Model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByParentMenuIsNull();

}