package com.asv.unapi.service.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alexandrov
 * @since 05.09.2016
 */
public class HierarchyItem extends Item {

    private List<Item> childs = new ArrayList<Item>();

    private Item parent;

    public void addChild(HierarchyItem item) {
        item.parent = this;
        childs.add(item);
    }

    public List<Item> getChildren() {
        return childs;
    }

    public Item getParent() {
        return parent;
    }

}
