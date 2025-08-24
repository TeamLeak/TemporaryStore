package com.github.saintedlittle.command.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubcommandSpec {
    String name();                    // имя сабкоманды, например "reload"
    String permission() default "";   // если пусто — берём из корневого @CommandSpec
    String description() default "";
    String usage() default "";
    String[] aliases() default {};
}