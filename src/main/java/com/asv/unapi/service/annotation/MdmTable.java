package com.asv.unapi.service.annotation;

import java.lang.annotation.*;

/**
 * Created by manager on 29.03.2016.
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface MdmTable {
    String value();
}
