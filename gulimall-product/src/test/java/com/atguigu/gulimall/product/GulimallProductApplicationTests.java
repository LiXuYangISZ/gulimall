package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    public void contextLoads() {
        BrandEntity brand = new BrandEntity();
        // brand.setName("华为");
        // brandService.save(brand);
        // System.out.println("保存成功...");

        // brand.setBrandId(1L);
        // brand.setDescript("哈哈哈哈我爱华为");
        // brandService.updateById(brand);
        // System.out.println("修改数据成功...");

        List <BrandEntity> list = brandService.list(new QueryWrapper <BrandEntity>().eq("brand_id", 1L));
        list.forEach((item)->{
            System.out.println(item);
        });
    }

}
