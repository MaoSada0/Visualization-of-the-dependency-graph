package ru.qq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class TreeNode {
    private String depName;
    private String version;
    List<TreeNode> children;

    public TreeNode(String part, String version) {
        this.depName = part;
        this.version = version;
        children = new ArrayList<>();
    }

    public String getVersion() {
        return version;
    }

    public void getDeps(Set<String> arr, String prev, String prevVersion){
        if(prev.isEmpty()){
            for(TreeNode t: children){
                t.getDeps(arr, depName, prevVersion);
            }

            return;
        }
        arr.add(prev + " -down-> " + depName);

        if(children == null || children.isEmpty()){
            return;
        }


        for(TreeNode t: children){
            t.getDeps(arr, depName, version);
        }
    }


    @Override
    public String toString() {
        return "TreeNode{" +
                "part='" + depName + '\'' +
                '}';
    }

    public void insert(String[] parts, String version) {
        if (parts.length == 0) return;

        if (parts.length == 1) {
            this.depName = parts[0];
            this.version = version;
            return;
        }

        for (TreeNode child : children) {
            if (child.getPart().equals(parts[1])) {
                child.insert(Arrays.copyOfRange(parts, 1, parts.length), version);
                return;
            }
        }


        TreeNode newChild = new TreeNode(parts[1], version);
        children.add(newChild);
        newChild.insert(Arrays.copyOfRange(parts, 1, parts.length), version);
    }

    public String getPart() {
        return depName;
    }
}

