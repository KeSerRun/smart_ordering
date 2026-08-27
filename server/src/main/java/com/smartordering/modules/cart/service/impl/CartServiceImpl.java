package com.smartordering.modules.cart.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.framework.redis.RedisUtils;
import com.smartordering.modules.cart.dto.CartItemDTO;
import com.smartordering.modules.cart.service.CartService;
import com.smartordering.modules.cart.vo.CartItemVO;
import com.smartordering.modules.cart.vo.CartVO;
import com.smartordering.modules.dish.entity.Dish;
import com.smartordering.modules.dish.mapper.DishMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Cart service implementation
 * <p>
 * Cart data stored in Redis Hash, key: cart:{userId}:{tableId}, TTL 2 hours.
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisUtils redisUtils;
    private final ObjectMapper objectMapper;
    private final DishMapper dishMapper;

    private static final long CART_TTL_HOURS = 2;

    @Override
    public CartVO addItem(Long userId, Long tableId, CartItemDTO dto) {
        // Validate dish exists, on sale, and not sold out
        Dish dish = dishMapper.selectById(dto.getDishId());
        if (dish == null) {
            throw new BusinessException(404, "Dish not found");
        }
        if (dish.getStatus() != 1) {
            throw new BusinessException(2002, "Dish is off shelf");
        }
        if (dish.getSoldOut() != null && dish.getSoldOut() == 1) {
            throw new BusinessException(2001, "Dish is sold out");
        }

        String cartKey = buildCartKey(userId, tableId);
        String field = dto.getDishId().toString();

        CartItemVO existingItem = getCartItem(cartKey, field);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + dto.getQuantity());
            existingItem.setAmount(existingItem.getPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity())));
            saveCartItem(cartKey, field, existingItem);
        } else {
            CartItemVO item = new CartItemVO();
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setDishImage(dish.getImage() != null ? dish.getImage() : dish.getThumbnail());
            item.setPrice(dish.getPrice());
            item.setQuantity(dto.getQuantity());
            item.setRemark(dto.getRemark());
            item.setAmount(dish.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
            saveCartItem(cartKey, field, item);
        }

        redisUtils.expire(cartKey, CART_TTL_HOURS, TimeUnit.HOURS);
        log.info("Cart add item: userId={}, tableId={}, dishId={}, qty={}", userId, tableId, dto.getDishId(), dto.getQuantity());
        return getCart(userId, tableId);
    }

    @Override
    public CartVO updateQuantity(Long userId, Long tableId, Long dishId, int quantity) {
        String cartKey = buildCartKey(userId, tableId);
        String field = dishId.toString();
        if (quantity <= 0) {
            redisUtils.hDelete(cartKey, field);
        } else {
            CartItemVO item = getCartItem(cartKey, field);
            if (item == null) {
                throw new BusinessException("Item not found in cart");
            }
            item.setQuantity(quantity);
            item.setAmount(item.getPrice().multiply(BigDecimal.valueOf(quantity)));
            saveCartItem(cartKey, field, item);
        }
        redisUtils.expire(cartKey, CART_TTL_HOURS, TimeUnit.HOURS);
        return getCart(userId, tableId);
    }

    @Override
    public CartVO getCart(Long userId, Long tableId) {
        String cartKey = buildCartKey(userId, tableId);
        Map<Object, Object> entries = redisUtils.hGetAll(cartKey);

        List<CartItemVO> items = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            CartItemVO item = deserialize(entry.getValue());
            if (item != null) {
                items.add(item);
            }
        }

        int totalCount = items.stream().mapToInt(CartItemVO::getQuantity).sum();
        BigDecimal totalPrice = items.stream()
                .map(CartItemVO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartVO cartVO = new CartVO();
        cartVO.setTableId(tableId);
        cartVO.setItems(items);
        cartVO.setTotalCount(totalCount);
        cartVO.setTotalPrice(totalPrice);
        return cartVO;
    }

    @Override
    public void clearCart(Long userId, Long tableId) {
        redisUtils.delete(buildCartKey(userId, tableId));
    }

    @Override
    public void removeItem(Long userId, Long tableId, Long dishId) {
        redisUtils.hDelete(buildCartKey(userId, tableId), dishId.toString());
    }

    // ========== private ==========

    private String buildCartKey(Long userId, Long tableId) {
        return "cart:" + userId + ":" + tableId;
    }

    private CartItemVO getCartItem(String cartKey, String field) {
        return deserialize(redisUtils.hGet(cartKey, field));
    }

    private void saveCartItem(String cartKey, String field, CartItemVO item) {
        try {
            redisUtils.hSet(cartKey, field, objectMapper.writeValueAsString(item));
        } catch (JsonProcessingException e) {
            throw new BusinessException("Cart serialization failed");
        }
    }

    private CartItemVO deserialize(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.readValue(value.toString(), CartItemVO.class);
        } catch (Exception e) {
            return null;
        }
    }
}