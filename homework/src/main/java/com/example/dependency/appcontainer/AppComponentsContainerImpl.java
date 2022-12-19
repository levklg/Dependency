package com.example.dependency.appcontainer;


import com.example.dependency.App;
import com.example.dependency.appcontainer.api.AppComponent;
import com.example.dependency.appcontainer.api.AppComponentsContainer;
import com.example.dependency.appcontainer.api.AppComponentsContainerConfig;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppComponentsContainerImpl implements AppComponentsContainer {

    private final List<Object> appComponents = new ArrayList<>();
    private final Map<String, Object> appComponentsByName = new HashMap<>();
    private Object object;
    private TreeMap<AppComponent, Method> mapForSortingComponents;

    public AppComponentsContainerImpl(Class<?> initialConfigClass) throws Exception {


        processConfig(initialConfigClass);

    }

    private void processConfig(Class<?> configClass) throws Exception {
        checkConfigClass(configClass);
        // You code here...
        object = getClassObject(configClass);
        Method[] methods = configClass.getDeclaredMethods();

        Map<AppComponent, Method> mapComponent = Arrays.stream(methods).filter(method -> method.isAnnotationPresent(AppComponent.class))
                .collect(Collectors.toMap(method -> method.getAnnotation(AppComponent.class), method -> method));

        mapForSortingComponents = new TreeMap<>(Comparator.comparing(AppComponent::order));
        mapForSortingComponents.putAll(mapComponent);

        List<String> listToSearchForTheSameName = new ArrayList<>();
        for (Map.Entry<AppComponent, Method> entry : mapForSortingComponents.entrySet()) {
            AppComponent appComponent = entry.getKey();

            if (listToSearchForTheSameName.contains(appComponent.name())) {
                System.err.println("В контексте не должно быть компонентов с одинаковым именем");
                throw new Exception();
            } else {
                listToSearchForTheSameName.add(appComponent.name());
            }
            listToSearchForTheSameName.add(appComponent.name());
        }

    }

    private void checkConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException(String.format("Given class is not config %s", configClass.getName()));
        }
    }

    @Override
    public <C> C getAppComponent(Class<C> componentClass) throws Exception {

        for (Map.Entry<AppComponent, Method> entry : mapForSortingComponents.entrySet()) {
            AppComponent appComponent = entry.getKey();
            Method method = entry.getValue();
            var component = runMethod(object, appComponent, method);

            for(var cmp : appComponents){
              if(cmp.getClass().getSimpleName().equals(component.getClass().getSimpleName())){
                  System.err.println("В контексте не должно быть дублирующих  компонентов");
                  throw new Exception();
              }
            }
              appComponents.add(component);

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

        for (Map.Entry<AppComponent, Method> entry : mapForSortingComponents.entrySet()) {
            AppComponent appComponent = entry.getKey();
            Method method = entry.getValue();
            var obj = object;
            var component = runMethod(object, appComponent, method);

            for (Map.Entry<String, Object> entrySecond : appComponentsByName.entrySet()) {
                String keyAppComponent = entrySecond.getKey();
                Object valueCompanent = entrySecond.getValue();
               if(valueCompanent.getClass().getSimpleName().equals(component.getClass().getSimpleName())){
                   System.err.println("В контексте не должно быть дублирующих  компонентов");
                   throw new Exception();
               }

            }
             appComponentsByName.put(appComponent.name(),component);
        }

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
                    if (appComponents.size() > 0) {
                        for (Object classСomponent : appComponents) {
                            if (clzz.isInstance(classСomponent)) {
                                args[index] = classСomponent;
                            }
                        }
                    }

                    if (appComponentsByName.size() > 0) {
                        for (Map.Entry<String, Object> entry : appComponentsByName.entrySet()) {
                            Object valueCompanent = entry.getValue();
                            if (clzz.isInstance(valueCompanent)) {
                                args[index] = valueCompanent;
                            }
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
