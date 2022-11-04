package com.example.dependency.appcontainer;


import com.example.dependency.appcontainer.api.AppComponent;
import com.example.dependency.appcontainer.api.AppComponentsContainer;
import com.example.dependency.appcontainer.api.AppComponentsContainerConfig;
import com.example.dependency.services.IOService;

import java.lang.reflect.*;
import java.text.Annotation;
import java.util.*;

public class AppComponentsContainerImpl implements AppComponentsContainer {

    private final List<Object> appComponents = new ArrayList<>();
    private final Map<String, Object> appComponentsByName = new HashMap<>();
    private Map<AppComponent, Method> componentMap = new HashMap<>();

    public AppComponentsContainerImpl(Class<?> initialConfigClass) throws Exception {

        processConfig(initialConfigClass);

    }

    private void processConfig(Class<?> configClass) throws Exception {
        checkConfigClass(configClass);
        // You code here...
        Object object = null;
        Constructor<?> constructor = null;
        try {
            constructor = configClass.getDeclaredConstructor();
            object = constructor.newInstance();


        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Method[] methods = configClass.getDeclaredMethods();
        TreeMap<Integer, Method> treeMethod = new TreeMap<>();

        for (Method method : methods) {
            if (method.isAnnotationPresent(AppComponent.class)) {
                AppComponent appComponent = method.getAnnotation(AppComponent.class);
                if (componentMap.containsKey(appComponent)) {
                    throw new Exception();
                } else {
                    componentMap.put(appComponent, method);
                }

            }
        }

        for (Map.Entry<AppComponent, Method> entry : componentMap.entrySet()) {
            treeMethod.put(entry.getKey().order(), entry.getValue());

        }

        for (Map.Entry<Integer, Method> entry : treeMethod.entrySet()) {//componentMap.entrySet()
            runMethod(object, entry.getValue(), this.appComponents);
        }

    }

    private void checkConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException(String.format("Given class is not config %s", configClass.getName()));
        }
    }

    @Override
    public <C> C getAppComponent(Class<C> componentClass) throws Exception {


        duplicatesFind((Map<AppComponent, Method>) this.componentMap);

        int index = this.appComponents.size();
        List<Object> duplicates = new ArrayList<>();
        for (Object ol : this.appComponents) {
            if (duplicates.contains(ol)) {
                throw new Exception();
            } else duplicates.add(ol);
        }

        for (Object objectReturn : this.appComponents) {

            if (componentClass.isInstance(objectReturn)) return (C) objectReturn;
        }

        return null;
    }

    @Override
    public <C> C getAppComponent(String componentName) {
        Object o = null;
        for (Map.Entry<String, Object> entry : appComponentsByName.entrySet()) {
            String key = entry.getKey();
            if (key.toLowerCase().contains(componentName.toLowerCase())) {
                o = entry.getValue();
            }
        }
        return (C) o;
    }

    public void runMethod(Object object, Method method, List<Object> appComponents) {

        Class<?>[] parametrs = method.getParameterTypes();
        List<Object> listParam = new ArrayList<>();
        for (Object componentObject : appComponents) {
            for (Class<?> arg : parametrs) {
                if (arg.isInstance(componentObject)) {
                    listParam.add(componentObject);
                }
            }
        }
        Object[] args = listParam.toArray();
        try {
            Object o = method.invoke(object, args);
            appComponents.add(o);
            appComponentsByName.put(o.getClass().getSimpleName(), o);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public boolean duplicatesFind(Map<AppComponent, Method> objectList) throws Exception {
        List<Class<?>> duplicates = new ArrayList<>();

        for (Map.Entry<AppComponent, Method> entry : componentMap.entrySet()) {

            if (duplicates.contains(entry.getValue().getReturnType())) {
                throw new Exception();
            } else duplicates.add(entry.getValue().getReturnType());
        }

        return false;
    }


}
