package com.computershop.main.services;

import com.computershop.main.entities.Role;
import com.computershop.main.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;
    
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    public Optional<Role> getRoleById(Integer roleId) {
        return roleRepository.findById(roleId);
    }
    
    public Optional<Role> getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }
    
    public Optional<Role> getRoleByNameIgnoreCase(String roleName) {
        return roleRepository.findByRoleNameIgnoreCase(roleName);
    }
    
    public Role createRole(Role role) {
        if (roleRepository.existsByRoleName(role.getRoleName())) {
            throw new RuntimeException("Role with name '" + role.getRoleName() + "' already exists");
        }
        return roleRepository.save(role);
    }
    
    public Role updateRole(Integer roleId, Role roleDetails) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        
        if (!role.getRoleName().equals(roleDetails.getRoleName()) && 
            roleRepository.existsByRoleName(roleDetails.getRoleName())) {
            throw new RuntimeException("Role with name '" + roleDetails.getRoleName() + "' already exists");
        }
        
        role.setRoleName(roleDetails.getRoleName());
        return roleRepository.save(role);
    }
    
    public void deleteRole(Integer roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Role not found with id: " + roleId);
        }
        roleRepository.deleteById(roleId);
    }
    
    public boolean existsByRoleName(String roleName) {
        return roleRepository.existsByRoleName(roleName);
    }
    
    public Role getCustomerRole() {
        return roleRepository.findByRoleName("customer")
                .orElseThrow(() -> new RuntimeException("Customer role not found"));
    }
    
    public Role getAdminRole() {
        return roleRepository.findByRoleName("admin")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));
    }
    
    public Optional<Role> findByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }
}