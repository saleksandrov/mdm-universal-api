package com.asv.unapi.service.annotation;

import java.lang.annotation.*;

/**
 * @author alexandrov
 * @since 25.07.2016
 */
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface MdmParent {

    String keyCode();

}
