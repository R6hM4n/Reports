package az.projectdailyreport.projectdailyreport.service.impl;

import az.projectdailyreport.projectdailyreport.model.Role;
import az.projectdailyreport.projectdailyreport.repository.RoleRepository;
import az.projectdailyreport.projectdailyreport.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;




    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }
}
