package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.entity.Result;
import com.pinyougou.entityGroup.Cart;
import com.pyg.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pyg.utils.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * （1）	从 cookie 中取出购物车
 * （2）	向购物车添加商品
 * （3）	将购物车存入 cookie
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    //注入request，springMvc里面已经有内置了
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Reference(timeout = 6000)
    private CartService cartService;


    /**
     * 查询购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //当前登录人账号
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人："+username);
        //不管有没有登录最后都需要合并没登录的加上本地登录的
        //本地的也不会变
        //获取cookie字符串取出购物车列表
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        //判断是否为空
        if (cartListString == null || cartListString.equals("")) {
            //如果为空就返回一个空集合给
            cartListString = "[]";
        }
        //如果有的话就把这个cookie、串转换为购物车对象
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if (username.equals("anonymousUser")){
            return  cartList_cookie;
        }else {
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            //从redis中提取
            if (cartList_cookie.size() > 0) {//判断当本地购物车中存在数据
                //得到合并后的购物车
                List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
                //将合并后的购物车存入redis
                cartService.saveCartListToRedis(username, cartList);
                //本地购物车清除，不删除的话下次还会再合并一次
                CookieUtil.deleteCookie(request, response, "cartList");
                System.out.println("执行了合并购物车的逻辑");
                return cartList;
            }

            return cartList_redis;

        }
    }

    /**
     * 添加商品到购物车
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin("http://localhost:9100")
    public Result addGoodsToCartList(Long itemId, Integer num) {
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        //设置可以进行跨域调用这个方法
        // response.setHeader("Access-Control-Allow-Origin", "*"); * 代表所有的地址都可以调用这个方法
//注意：这里的端口取决于商品详细页所在tomcat容器的端口，我的商品详细页在端口为9100的tomcat

//        response.setHeader("Access-Control-Allow-Credentials", "true");
//是否允许操作cookie，如果不允许，不用写下面这句，如果允许，上面是不可以写*的。

        //当前登录人账号
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人："+username);
        try {
            //从cookie获取购物车列表
            List<Cart> cartList = findCartList();//获取购物车列表
            //调用CartService里面的添加购物车列表的方法,然后给这个从cookie里面查询出来的
            //重新赋值
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            if (username.equals("anonymousUser")){
                //存入cookie中,要先转为一个json串，对象不能直接设置cookie
                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),3600 * 24, "UTF-8");
                System.out.println("向cookie存入数据");
            }else {//如果是已登录，保存到redis
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }

    }
}
