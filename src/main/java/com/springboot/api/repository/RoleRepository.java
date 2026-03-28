package com.springboot.api.repository;

import com.springboot.api.entity.roles.Role;
import com.springboot.api.entity.roles.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByUserRole(RoleName role);
}