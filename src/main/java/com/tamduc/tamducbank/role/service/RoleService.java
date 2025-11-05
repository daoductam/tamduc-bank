package com.tamduc.tamducbank.role.service;

import com.tamduc.tamducbank.res.Response;
import com.tamduc.tamducbank.role.entity.Role;

import java.util.List;

public interface RoleService {

    Response<Role> createRole(Role roleRequest);

    Response<Role> updateRole(Role roleRequest);

    Response<List<Role>> getAllRoles();

    Response<?> deleteRole(Long id);

}
