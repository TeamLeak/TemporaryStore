package com.github.saintedlittle.command.annotations;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandSpec {
    String name();
    String[] aliases() default {};
    String permission() default "";
    String description() default "";
    String usage() default "";
}
