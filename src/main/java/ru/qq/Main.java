package ru.qq;

public class Main {

    public static void main(String[] args) throws Exception {
//        if(args.length != 2){
//            throw new IllegalArgumentException("Both path to package and path to visualizer must be provided.");
//        }

        DependencyVisualizer dependencyVisualizer =
                new DependencyVisualizer("C:\\Users\\user\\Downloads\\azure.core.1.43.0.nupkg",
                "C:\\Users\\user\\Downloads\\plantuml-1.2024.7.jar");

        dependencyVisualizer.process();

        System.out.println("done!");
    }

}