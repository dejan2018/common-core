package com.github.misterchangray.service.user.impl;

import com.github.misterchangray.common.enums.ErrorEnum;
import com.github.misterchangray.common.NormalResponse;
import com.github.misterchangray.common.PageInfo;
import com.github.misterchangray.dao.entity.User;
import com.github.misterchangray.dao.entity.UserQuery;
import com.github.misterchangray.dao.entity.UserRoleMap;
import com.github.misterchangray.dao.entity.UserRoleMapQuery;
import com.github.misterchangray.dao.mapper.UserMapper;
import com.github.misterchangray.common.enums.DBEnum;
import com.github.misterchangray.dao.mapper.UserRoleMapMapper;
import com.github.misterchangray.service.user.RoleService;
import com.github.misterchangray.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Miste on 3/26/2018.
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService{
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserRoleMapMapper userRoleMapMapper;
    @Autowired
    RoleService roleService;


    /**
     * 更新用户下的角色信息
     * @param userId 被更新的用户ID
     * @param roles 更新的角色信息
     * @return
     */
    public NormalResponse updateRole(Integer userId, List<Integer> roles) {
        NormalResponse normalResponse = NormalResponse.newInstance();
        if(null == userId) return normalResponse.setErrorCode(ErrorEnum.INVALID_REQUEST);
        if(null == roles) return normalResponse.setErrorCode(ErrorEnum.INVALID_REQUEST);

        //判断ID是否都存在
        if(((Boolean)roleService.exist(roles).getData())) {
            //老数据标记为无效
            UserRoleMap userRoleMap= new UserRoleMap();
            userRoleMap.setIsdel(DBEnum.TRUE.getCode());

            UserRoleMapQuery userRoleMapQuery = new UserRoleMapQuery();
            UserRoleMapQuery.Criteria criteria = userRoleMapQuery.createCriteria();
            criteria.andUserIdEqualTo(userId);
            userRoleMapMapper.updateByQuerySelective(userRoleMap, userRoleMapQuery);

            //插入新的映射数据
            List<UserRoleMap> userRoleMaps = new ArrayList<UserRoleMap>();
            UserRoleMap tmp;
            for(Integer i=roles.size(); i<0; i--) {
                tmp = new UserRoleMap();
                tmp.setUserId(userId);
                tmp.setRoleId(roles.get(i));
                userRoleMaps.add(tmp);
            }
            userRoleMapMapper.batchInsert(userRoleMaps);
            return normalResponse;
        }
        return normalResponse.setErrorCode(ErrorEnum.INVALID);
    }


    public NormalResponse checkUserInfo(String username, String email, String phone, String idcard) {
        UserQuery userQuery = new UserQuery();
        UserQuery.Criteria criteria = userQuery.createCriteria();

        if(null != email) userQuery.or(criteria.andEmailEqualTo(email));
        if(null != phone) userQuery.or(criteria.andPhoneEqualTo(phone));
        if(null != idcard) userQuery.or(criteria.andIdcardEqualTo(idcard));
        if(null != username) userQuery.or(criteria.andUsernameEqualTo(username));

        return NormalResponse.newInstance().setData(userMapper.selectByQuery(userQuery));
    }

    public NormalResponse exist(List<Integer> ids) {
        if(null == ids) NormalResponse.newInstance().setErrorCode(ErrorEnum.INVALID_REQUEST);

        UserQuery userQuery = new UserQuery();
        UserQuery.Criteria criteria = userQuery.createCriteria();
        criteria.andIdIn(ids);
        if(ids.size() == userMapper.countByQuery(userQuery)) {
           return NormalResponse.newInstance().setData(true);
        } else {
           return NormalResponse.newInstance().setData(false);
        }
    }

    public NormalResponse getById(Integer id) {
        if(null == id) return NormalResponse.newInstance().setErrorCode(ErrorEnum.INVALID_REQUEST);
        return NormalResponse.newInstance().setData(userMapper.selectByPrimaryKey(id));
    }

    public NormalResponse getByIds(List<Integer> ids) {
        if(null == ids) NormalResponse.newInstance().setErrorCode(ErrorEnum.INVALID_REQUEST);

        UserQuery userQuery = new UserQuery();
        userQuery.createCriteria().andIdIn(ids);
        return NormalResponse.newInstance().setData(userMapper.selectByQuery(userQuery));
    }

    public NormalResponse list(User user, PageInfo pageInfo) {
        if(null == pageInfo) pageInfo = new PageInfo();

        UserQuery userQuery = new UserQuery();
        userQuery.page(pageInfo.getPage(), pageInfo.getLimit());

        UserQuery.Criteria criteria = userQuery.createCriteria();
        criteria.andIsdelEqualTo(DBEnum.FALSE.getCode());
        criteria.andEnableEqualTo(DBEnum.TRUE.getCode());
        if(null != user) {
            if(null != user.getName())criteria.andNameLike(user.getName());
            if(null != user.getUsername())criteria.andUsernameLike(user.getUsername());
            if(null != user.getPhone())criteria.andPhoneLike(user.getPhone());
            if(null != user.getSex())criteria.andSexEqualTo(user.getSex());
        }

        return NormalResponse.newInstance().setData(userMapper.selectByQuery(userQuery));
    }

    public NormalResponse add(User user) {
        if(null == user) return NormalResponse.newInstance().setErrorCode(ErrorEnum.INVALID_REQUEST);

        user.setEnable(DBEnum.TRUE.getCode());
        user.setIsdel(DBEnum.FALSE.getCode());
        userMapper.insert(user);
        return NormalResponse.newInstance().setData(user);
    }

    public NormalResponse batchInsert(List<User> users) {
        if(null == users) return NormalResponse.newInstance().setErrorCode(ErrorEnum.INVALID_REQUEST);

        return NormalResponse.newInstance().setData(userMapper.batchInsert(users));
    }

    public NormalResponse update(User user) {
        if(null == user || null == user.getId()) return NormalResponse.newInstance().setErrorCode(ErrorEnum.INVALID_REQUEST);

        return NormalResponse.newInstance().setData(userMapper.updateByPrimaryKeySelective(user));
    }

    public NormalResponse delete(User user) {
        if(null == user || null == user.getId()) return NormalResponse.newInstance().setErrorCode(ErrorEnum.INVALID_REQUEST);

        user.setIsdel(DBEnum.TRUE.getCode());
        return NormalResponse.newInstance().setData(userMapper.updateByPrimaryKeySelective(user));
    }
}