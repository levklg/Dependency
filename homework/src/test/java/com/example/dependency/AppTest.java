package com.example.dependency;

import com.example.dependency.appcontainer.AppComponentsContainerImpl;
import com.example.dependency.appcontainer.api.AppComponent;
import com.example.dependency.appcontainer.api.AppComponentsContainerConfig;


import com.example.dependency.config.AppConfig;
import com.example.dependency.services.EquationPreparer;
import com.example.dependency.services.EquationPreparerImpl;
import com.example.dependency.services.IOService;
import com.example.dependency.services.IOServiceStreams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;



import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AppTest {

    @DisplayName("Из контекста тремя способами должен корректно доставаться компонент с проставленными полями")
    @ParameterizedTest(name = "Достаем по: {0}")
    @CsvSource(value = {

            "IOService, com.example.dependency.services.IOService",
            "IOServiceStreams, com.example.dependency.services.IOService",
            "ioService, com.example.dependency.services.IOService",

            "PlayerService, com.example.dependency.services.PlayerService",
            "PlayerServiceImpl, com.example.dependency.services.PlayerService",
            "playerService, com.example.dependency.services.PlayerService",

            "EquationPreparer, com.example.dependency.services.EquationPreparer",
            "EquationPreparerImpl, com.example.dependency.services.EquationPreparer",
            "equationPreparer, com.example.dependency.services.EquationPreparer"



    })
    public void shouldExtractFromContextCorrectComponentWithNotNullFields(String classNameOrBeanId, Class<?> rootClass) throws Exception {
        var ctx = new AppComponentsContainerImpl(AppConfig.class);

        assertThat(classNameOrBeanId).isNotEmpty();
        Object component;
        if (classNameOrBeanId.charAt(0) == classNameOrBeanId.toUpperCase().charAt(0)) {
            Class<?> gameProcessorClass = Class.forName("com.example.dependency.services." + classNameOrBeanId);
            assertThat(rootClass).isAssignableFrom(gameProcessorClass);

            component = ctx.getAppComponent(gameProcessorClass);
        } else {
            component = ctx.getAppComponent(classNameOrBeanId);
        }
      assertThat(component).isNotNull();

      assertThat(rootClass).isAssignableFrom(component.getClass());

        var fields = Arrays.stream(component.getClass().getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toList());

        for (var field: fields){
            var fieldValue = field.get(component);

          assertThat(fieldValue).isNotNull().isInstanceOfAny(com.example.dependency.services.IOService.class,com.example.dependency.services.PlayerService.class,
                  com.example.dependency.services.EquationPreparer.class, PrintStream.class, Scanner.class);
        }

    }

    @DisplayName("В контексте не должно быть компонентов с одинаковым именем")
    @Test
    public void shouldNotAllowTwoComponentsWithSameName() throws Exception {
        assertThatCode(()-> new AppComponentsContainerImpl(ConfigWithTwoComponentsWithSameName.class))
                .isInstanceOf(Exception.class);
    }

    @DisplayName("При попытке достать из контекста отсутствующий или дублирующийся компонент, должно выкидываться исключение")
    @Test
    public void shouldThrowExceptionWhenContainerContainsMoreThanOneOrNoneExpectedComponents() throws Exception {
        var ctx = new AppComponentsContainerImpl(ConfigWithTwoSameComponents.class);

        assertThatCode(()-> ctx.getAppComponent(com.example.dependency.services.EquationPreparer.class))
             .isInstanceOf(Exception.class);

        assertThatCode(()-> ctx.getAppComponent(com.example.dependency.services.PlayerService.class))
                .isInstanceOf(Exception.class);
    }

    @AppComponentsContainerConfig(order = 1)
    public static class ConfigWithTwoComponentsWithSameName {
        public ConfigWithTwoComponentsWithSameName() {
        }

        @AppComponent(order = 1, name = "equationPreparer")
        public EquationPreparer equationPreparer1() {
            return new EquationPreparerImpl();
        }

        @AppComponent(order = 1, name = "equationPreparer")
        public IOService ioService() {
            return new IOServiceStreams(System.out, System.in);
        }
    }

    @AppComponentsContainerConfig(order = 1)
    public static class ConfigWithTwoSameComponents{

        @AppComponent(order = 1, name = "equationPreparer1")
        public EquationPreparer equationPreparer1() {
            return new EquationPreparerImpl();
        }

        @AppComponent(order = 1, name = "equationPreparer2")
        public EquationPreparer equationPreparer2() {
            return new EquationPreparerImpl();
        }
    }
}