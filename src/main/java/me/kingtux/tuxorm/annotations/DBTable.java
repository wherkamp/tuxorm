package me.kingtux.tuxorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 * Add this Annotation to the beginning of your class.
 * This annotation makes your class compatible with TuxORM
 * @author KingTux
 */
public @interface DBTable {
    /**
     * If you want to specify the table name.
     * @return Your custom name of your table.
     */
    String name() default "";
}
