package com.ai.service;

import com.ai.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface UserService {

    Page<User> page(Integer pageNum, Integer pageSize, String username, Integer status);

    User getById(String id);

    String add(User user);

    String update(User user);

    String delete(String id);

    String updateStatus(String id, Integer status);
}
