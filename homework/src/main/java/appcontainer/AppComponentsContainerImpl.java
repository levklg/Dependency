package appcontainer;


import appcontainer.api.AppComponent;
import appcontainer.api.AppComponentsContainer;
import appcontainer.api.AppComponentsContainerConfig;
import org.apache.commons.lang3.StringUtils;
import services.*;

import java.lang.reflect.*;
import java.util.*;

public class AppComponentsContainerImpl implements AppComponentsContainer {

    private final List<Object> appComponents = new ArrayList<>();
    private final Map<String, Object> appComponentsByName = new HashMap<>();

    public AppComponentsContainerImpl(Class<?> initialConfigClass) {
        processConfig(initialConfigClass);
    }

    private void processConfig(Class<?> configClass) {
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
                treeMethod.put(appComponent.order(), method);

            }

        }

        for (Map.Entry<Integer, Method> entry : treeMethod.entrySet()) {
            runMethod(object, entry.getValue());
        }


    }

    private void checkConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException(String.format("Given class is not config %s", configClass.getName()));
        }
    }

    @Override
    public <C> C getAppComponent(Class<C> componentClass) {

        int index = this.appComponents.size();
        return (C) this.appComponents.get(index - 1);
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

    public void runMethod(Object object, Method method) {

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

}
