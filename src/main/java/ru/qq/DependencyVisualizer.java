package ru.qq;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class DependencyVisualizer {

    private final String pathToVisualizer; // "C:\\Users\\user\\Downloads\\plantuml-1.2024.7.jar"
    private final String pathToPackage; // "C:\\Users\\user\\Downloads\\newtonsoft.json.13.0.3.nupkg"

    private final TreeNode tree;

    public DependencyVisualizer(String pathToPackage, String pathToVisualizer) throws Exception {
        this.pathToPackage = pathToPackage;
        this.pathToVisualizer = pathToVisualizer;
        tree = new TreeNode("this", "1");
    }


    public void process() throws Exception {
        Path tempDir = Files.createTempDirectory("nupkg");

        System.out.println(tempDir.toString());

        unzipNupkg(pathToPackage, tempDir.toString());

        File nuspecFile = findNuspecFile(tempDir);
        parseNuspec(nuspecFile, tree);

        String plantUMLContent = generatePlantUML(tree);

        Path plantUMLFile = Files.createTempFile("graph", ".puml");
        Files.write(plantUMLFile, plantUMLContent.getBytes());


        if(pathToVisualizer.endsWith(".jar"))
            visualizeGraphJar(plantUMLFile, pathToVisualizer);
        else
            visualizeGraphExe(plantUMLFile, pathToVisualizer);
    }



    private static void unzipNupkg(String nupkgPath, String destDir) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(nupkgPath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                File file = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!file.mkdirs() && !file.isDirectory()) {
                        throw new IOException("Failed to create directory: " + file);
                    }
                } else {
                    File parentDir = file.getParentFile();
                    if (!parentDir.exists() && !parentDir.mkdirs() && !parentDir.isDirectory()) {
                        throw new IOException("Failed to create directory: " + parentDir);
                    }
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = zipIn.read(buffer)) != -1) {
                            bos.write(buffer, 0, read);
                        }
                    }
                }
                zipIn.closeEntry();
            }
        }
    }


    private static File findNuspecFile(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files
                    .filter(path -> path.toString().endsWith(".nuspec"))
                    .map(Path::toFile)
                    .findFirst()
                    .orElseThrow(() -> new FileNotFoundException("nuspec file not found"));
        }
    }

    private static void parseNuspec(File nuspecFile, TreeNode tree) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(nuspecFile);
        NodeList groupNodes = doc.getElementsByTagName("group");

        if(groupNodes.getLength() != 0){
            for(int i = 0; i < groupNodes.getLength(); i++){
                Element group = (Element) groupNodes.item(i);
                String groupName = group.getAttribute("targetFramework");
                groupName = trimToFirstDigit(groupName);

                NodeList dn = group.getElementsByTagName("dependency");

                for(int j = 0; j < dn.getLength(); j++){
                    Element dependency = (Element) dn.item(j);
                    String depName = dependency.getAttribute("id");
                    String version = dependency.getAttribute("version");
                    String temp = ("this|" + groupName + "|" + depName.replace(".", "|"));
                    tree.insert((temp).split("\\|"), version);
                }
            }
        } else {
            NodeList deps = doc.getElementsByTagName("dependency");

            for(int i = 0; i < deps.getLength(); i++){
                Element dependency = (Element) deps.item(i);
                String depName = dependency.getAttribute("id");
                String version = dependency.getAttribute("version");
                String temp = ("this|" + depName.replace(".", "|"));
                tree.insert((temp).split("\\|"), version);
            }
        }


    }

    public static String trimToFirstDigit(String input) {
        OptionalInt firstDigitIndex = IntStream.range(0, input.length())
                .filter(i -> Character.isDigit(input.charAt(i)))
                .findFirst();

        return firstDigitIndex.isPresent() ? input.substring(0, firstDigitIndex.getAsInt()) : input;
    }

    private static String generatePlantUML(TreeNode tree) {

        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");

        Set<String> deps = new HashSet<>();
        tree.getDeps(deps, "", "1");

        deps.forEach(x -> sb.append(x.trim()).append("\n"));

        sb.append("@enduml\n");

        System.out.println(sb);
        return sb.toString();
    }

    private static void visualizeGraphExe(Path plantUMLFile, String pathToVisualizer) throws IOException {
        new ProcessBuilder(pathToVisualizer, plantUMLFile.toString())
                .inheritIO()
                .start();
    }

    private static void visualizeGraphJar(Path plantUMLFile, String pathToJar) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", pathToJar, plantUMLFile.toString());
        processBuilder.inheritIO();
        Process process = processBuilder.start();

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("JAR process exited with non-zero status code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("JAR process interrupted", e);
        }

        openImage(plantUMLFile.toString().substring(0, plantUMLFile.toString().lastIndexOf(".")) + ".png");

    }

    public static void openImage(String imagePath) {
        File imageFile = new File(imagePath);

        if (!imageFile.exists()) {
            System.err.println("no file: " + imagePath);
            return;
        }

        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(imageFile);
            } else {
                System.err.println("not support open file");
            }
        } catch (IOException e) {
            System.err.println("err with opening file: " + e.getMessage());
        }
    }

}
