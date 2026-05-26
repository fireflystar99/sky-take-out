package com.sky.controller.admin;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Tag(name = "菜品管理")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */
    @Operation(summary = "新增菜品")
    @PostMapping
    public Result<String> save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        String key = "dish_" + dishDTO.getCategoryId();
        clearRedis(key);
        return Result.success();
    }

    /**
     * 分页查询菜品
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Operation(summary = "分页查询菜品")
    @GetMapping("/page") // query
    public Result<PageResult<DishVO>> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品：{}", dishPageQueryDTO);
        PageResult<DishVO> querySet = dishService.queryPage(dishPageQueryDTO);
        return Result.success(querySet);
    }

    /**
     * 删除菜品
     *
     * @param ids
     * @return
     */
    @Operation(summary = "删除菜品")
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("删除菜品：{}", ids);
        dishService.deleteBatch(ids);
        clearRedis("dish_*");
        return Result.success();
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @Operation(summary = "根据id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 更新菜品
     *
     * @param dishDTO
     * @return
     */
    @Operation(summary = "修改菜品")
    @PutMapping
    public Result<String> update(@RequestBody DishDTO dishDTO) {
        log.info("更新菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        clearRedis("dish_*");
        return Result.success();
    }

    /**
     * 启用或停用菜品
     *
     * @param status
     * @param id
     * @return
     */
    @Operation(summary = "启用或停用菜品")
    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        // 1. 启用 0. 停用
        log.info("启用或停用菜品：{}", id);
        dishService.startOrStop(status, id);
        clearRedis("dish_*");
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @Operation(summary = "根据分类id查询菜品")
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> dishList = dishService.list(categoryId);
        return Result.success(dishList);
    }

    private void clearRedis(String keys) {
        Set<String> cacheKeys = redisTemplate.keys(keys);
        redisTemplate.delete(cacheKeys);
    }
}
