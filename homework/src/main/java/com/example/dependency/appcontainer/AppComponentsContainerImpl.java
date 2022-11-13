package com.example.dependency.appcontainer;


import com.example.dependency.App;
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
    private List<String> errrorList = new ArrayList<>();

    public AppComponentsContainerImpl(Class<?> initialConfigClass) throws Exception {

        processConfig(initialConfigClass);

    }

    private void processConfig(Class<?> configClass) throws Exception {
        checkConfigClass(configClass);
        // You code here...
        Object object = getClassObject(configClass);
        Method[] methods = configClass.getDeclaredMethods();

        Map<AppComponent, Method> listComponent = Arrays.stream(methods).filter(method -> method.isAnnotationPresent(AppComponent.class))
                .collect(Collectors.toMap(method -> method.getAnnotation(AppComponent.class), method -> method));

        TreeMap<AppComponent, Method> to = new TreeMap<>(Comparator.comparing(AppComponent::order));
        to.putAll(listComponent);

        for (Map.Entry<AppComponent, Method> entry : to.entrySet()) {
            AppComponent appComponent = entry.getKey();
            Method method = entry.getValue();
            Object objectMethod = runMethod(object, appComponent, method);
            appComponents.add(objectMethod);
            if (appComponentsByName.containsKey(appComponent.name())) {
                throw new Exception();
            } else {
                appComponentsByName.put(appComponent.name(), objectMethod);
            }
        }
        if (listComponent.size() != appComponents.size()) {
            errrorList.add("duplicates");
        }

    }


    private void checkConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException(String.format("Given class is not config %s", configClass.getName()));
        }
    }

    @Override
    public <C> C getAppComponent(Class<C> componentClass) throws Exception {

        if (this.errrorList.size() > 0) {
            throw new Exception();
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

        if (this.errrorList.size() > 0) {
            throw new Exception();
        }
        Object o = null;
        for (Map.Entry<String, Object> entry : appComponentsByName.entrySet()) {
            String key = entry.getKey();
            if (key.contains(componentName)) {
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
                Object to = getAppComponent(clzz);
                args[index] = to;

            } catch (Exception e) {
                e.printStackTrace();
            }
            index++;
        }


        Object returnObject = null;
        try {
            returnObject = method.invoke(objectClass, args);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
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
        } catch (InvocationTargetException e) {
            System.out.println("Ошибка при создании объекта " + e);
        } catch (InstantiationException e) {
            System.out.println("Ошибка при создании объекта " + e);
        } catch (IllegalAccessException e) {
            System.out.println("Ошибка при создании объекта " + e);
        }
        return object;
    }


}
