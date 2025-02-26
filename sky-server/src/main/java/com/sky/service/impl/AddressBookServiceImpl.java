package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AddressBookServiceImpl
 * @Description 地址簿管理实现
 * @Author 12459
 * @Date 2025/2/23 15:32
 **/
@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    /*
    * 新增地址
    * */
    @Transactional
    public void addAddressBook(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(StatusConstant.DISABLE);
        addressBookMapper.insert(addressBook);
    }

    /*
    * 查询当前登录用户的所有地址信息
    * */
    public List<AddressBook> list(AddressBook addressBook) {
        List<AddressBook> list = addressBookMapper.list(addressBook);
        return list;
    }

    /*
    * 根据id修改地址
    * */
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    /*
    *设置默认地址
    * */
    @Transactional
    public void setDefault(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBookBefore = addressBookMapper.getDefault(userId);
        if(addressBookBefore != null) {
            addressBookBefore.setIsDefault(StatusConstant.DISABLE);
            addressBookMapper.update(addressBookBefore);
        }
        addressBook.setIsDefault(StatusConstant.ENABLE);
        addressBookMapper.update(addressBook);
    }

    /*
    * 根据id查询地址
    * */
    public AddressBook getById(Integer id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        return addressBook;
    }

    /*
    * 根据id删除地址
    * */
    public void delete(Long id) {
        addressBookMapper.deleteById(id);
    }

    /*
    * 查询默认地址
    * */
    @Transactional
    public List<AddressBook> getDefault() {
        AddressBook addressBook = addressBookMapper.getDefault(BaseContext.getCurrentId());
        List<AddressBook> list = new ArrayList<>();
        list.add(addressBook);
        return list;
    }
}
