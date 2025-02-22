package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMaper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName ShoppingCartServiceImpl
 * @Description 购物车实现类
 * @Author 12459
 * @Date 2025/2/22 11:16
 **/
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMaper shoppingCartMaper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /*
    * 添加购物车
    * */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断要加入购物车中的商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> cartList = shoppingCartMaper.list(shoppingCart);

        //若存在则数量加一
        if (cartList != null && cartList.size() > 0) {

            ShoppingCart cart = cartList.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMaper.updateNumberById(cart);
        }else {
            //否则insert

            //判断本次添加到购物车是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
                //添加菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setName(dish.getName());

            }else {
                //添加套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setName(setmeal.getName());

            }
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);

            shoppingCartMaper.insert(shoppingCart);
        }


    }
}
