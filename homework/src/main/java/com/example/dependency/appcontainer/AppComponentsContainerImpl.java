package com.example.dependency.appcontainer;


import com.example.dependency.appcontainer.api.AppComponent;
import com.example.dependency.appcontainer.api.AppComponentsContainer;
import com.example.dependency.appcontainer.api.AppComponentsContainerConfig;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppComponentsContainerImpl implements AppComponentsContainer {

    private final List<Object> appComponents = new ArrayList<>();
    private final Map<String, Object> appComponentsByName = new HashMap<>();

    public AppComponentsContainerImpl(Class<?> initialConfigClass) throws Exception {

        processConfig(initialConfigClass);

    }

    private void processConfig(Class<?> configClass) throws Exception {
        checkConfigClass(configClass);
        // You code here...
        Object object = getClassObject(configClass);
        Method[] methods = configClass.getDeclaredMethods();

        Map<AppComponent, Method> mapComponent = Arrays.stream(methods).filter(method -> method.isAnnotationPresent(AppComponent.class))
                .collect(Collectors.toMap(method -> method.getAnnotation(AppComponent.class), method -> method));

        Map<AppComponent, Method> mapForSortingComponents = new HashMap<AppComponent, Method>();
        Stream<Map.Entry<AppComponent, Method>> sorted = mapComponent.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(AppComponent::order)));
        var listSortingComponents = sorted.collect(Collectors.toList());

        for (var value : listSortingComponents) {
            AppComponent appComponent = value.getKey();
            Method method = value.getValue();
            var obj = object;
            var component = runMethod(object, appComponent, method);

            for (Map.Entry<String, Object> entrySecond : appComponentsByName.entrySet()) {
                if (appComponentsByName.containsKey(appComponent.name())) {
                    System.err.println("В контексте не должно быть компонентов с одинаковым именем");
                    throw new Exception();
                }

            }
            appComponents.add(component);
            appComponentsByName.put(appComponent.name(), component);
        }

    }

    private void checkConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException(String.format("Given class is not config %s", configClass.getName()));
        }
    }

    @Override
    public <C> C getAppComponent(Class<C> componentClass) throws Exception {
        List<String> listForFindingDuplicates = new ArrayList<>();
        for (Object o : this.appComponents) {
            if (listForFindingDuplicates.contains(o.getClass().getSimpleName())) {
                System.err.println("В контексте не должно быть дублирующих  компонентов");
                throw new RuntimeException();
            } else {
                listForFindingDuplicates.add(o.getClass().getSimpleName());
            }
        }

        for (Object objectReturn : this.appComponents) {

            if (componentClass.isInstance(objectReturn)) {
                return (C) objectReturn;
            }
        }

        return null;
    }

    @Override
    public <C> C getAppComponent(String componentName) throws Exception {

        Object o = null;

        for (Map.Entry<String, Object> entry : appComponentsByName.entrySet()) {
            String key = entry.getKey();
            Object component = entry.getValue();
            if (key.equals(componentName)) {
                o = entry.getValue();
            }
        }
        return (C) o;
    }

    public Object runMethod(Object objectClass, AppComponent appComponent, Method method) {

        Class<?>[] parametrs = method.getParameterTypes();
        Object[] args = new Object[parametrs.length];
        int index = 0;

        for (Class<?> arg : parametrs) {
            try {
                Class<?> clzz = arg;
                if (arg == null) {
                    System.err.println("Get null parametrs");
                    throw new RuntimeException();
                } else {
                    for (Object classСomponent : appComponents) {
                        if (clzz.isInstance(classСomponent)) {
                            args[index] = classСomponent;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
            index++;
        }
        Object returnObject = null;
        try {

            returnObject = method.invoke(objectClass, args);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return returnObject;
    }


    public Object getClassObject(Class<?> configClass) {
        Constructor<?> constructor = null;
        Object object = null;
        try {
            constructor = configClass.getDeclaredConstructor();
            object = constructor.newInstance();


        } catch (NoSuchMethodException e) {
            System.out.println("Ошибка при создании объекта " + e);
            throw new RuntimeException();
        } catch (InvocationTargetException e) {
            System.out.println("Ошибка при создании объекта " + e);
            throw new RuntimeException();
        } catch (InstantiationException e) {
            System.out.println("Ошибка при создании объекта " + e);
            throw new RuntimeException();
        } catch (IllegalAccessException e) {
            System.out.println("Ошибка при создании объекта " + e);
            throw new RuntimeException();
        }
        return object;
    }


}
