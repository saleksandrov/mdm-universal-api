package com.asv.unapi.service.annotation;

import com.asv.unapi.service.model.Item;
import com.asv.unapi.service.model.Item.Type;

import java.lang.annotation.*;

/**
 * @author alexandrov
 * @since 24.03.2016
 */
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface MdmField {

    String code();

    String tableName() default "unknown";

    Type type() default Type.SIMPLE;

    Class<? extends Item> implClass() default Item.class;

    boolean updatable() default true;

}


