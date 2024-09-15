package ru.qq;

public class Main {

    public static void main(String[] args) throws Exception {
        if(args.length != 2){
            throw new IllegalArgumentException("Both path to package and path to visualizer must be provided.");
        }

        DependencyVisualizer dependencyVisualizer = new DependencyVisualizer(args[0], args[1]);

        dependencyVisualizer.process();

        System.out.println("done!");
    }

}