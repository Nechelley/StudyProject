package com.study_project.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="enemy")
@Getter
@Setter
@NoArgsConstructor
public class Enemy extends Unit {



}
