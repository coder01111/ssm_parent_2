package com.pyg.page.service;

import java.util.List;

public interface ItemPageService {
    /**
     * 生成商品详细页
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);

    boolean deleteItemHtml(List<Long> goodsIds);
}
