package com.example.port.bundle.Model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private MenuItem parentMenu;

    @OneToMany(mappedBy = "parentMenu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> subMenuItems;
}