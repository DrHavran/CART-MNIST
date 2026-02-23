import java.util.*;
import java.util.stream.Collectors;

public class Logic {
    private final Data data;

    public Logic() {
        this.data = new Data();

        for(Node root : data.getRoots()){
            generateATree(root);
        }

        printATree();

        testTheTree(data.getRoots());
    }

    private void generateATree(Node root) {
        ArrayList<Node> queue = new ArrayList<>();
        queue.add(root);

        ArrayList<String> attributes  = new ArrayList<>(data.getAttributes());
        attributes.remove(Settings.type);

        while(!queue.isEmpty()) {
            Node current = queue.removeFirst();

            double bestWeight = Double.MAX_VALUE;
            String bestOption = "";
            String bestAttribute = "";
            Check bestReq = null;

            for(String attribute : attributes){
                System.out.println("checking attribute: " + attribute);
                ArrayList<Double> values = new ArrayList<>();
                for(HashMap<String, String> point : current.getPoints()){
                    values.add(Double.parseDouble(point.get(attribute)));
                }

                values = values.stream().sorted().distinct().collect(Collectors.toCollection(ArrayList::new));
                ArrayList<Double> options = new ArrayList<>();
                for(int i = 0; i < values.size()-1; i++){
                    options.add((values.get(i) + values.get(i+1)) / 2);
                }

                for(Double option : options){
                    Check req = (i) -> (Double.parseDouble((String) i) < option);
                    double weight = count(current, attribute, req);
                    if(weight < bestWeight){
                        bestWeight = weight;
                        bestAttribute = attribute;
                        bestOption = " < " + option;
                        bestReq = req;
                    }
                }
            }
            Node left = new Node();
            Node right = new Node();

            current.setLeftBranch(left);
            current.setRightBranch(right);
            current.setOption(bestOption);
            current.setCheckReq(bestReq);
            current.setCheckString(bestAttribute);

            System.out.println("Best split on: " + bestAttribute + bestOption);
            for(HashMap<String, String> point : current.getPoints()){
                if(current.check(point)){
                    left.addPoint(point);
                }else{
                    right.addPoint(point);
                }
            }

            if(countGini(left.getPoints()) != 0){
                queue.add(left);
            }
            if(countGini(right.getPoints()) != 0){
                queue.add(right);
            }
        }
    }

    private double count(Node current, String attribute, Check req){
        ArrayList<HashMap<String, String>> leftBranch = new ArrayList<>();
        ArrayList<HashMap<String, String>> rightBranch = new ArrayList<>();

        for(HashMap<String, String> point : current.getPoints()){
            if(req.check(point.get(attribute))){
                leftBranch.add(point);
            }else{
                rightBranch.add(point);
            }
        }

        return countWeight(leftBranch, rightBranch);
    }
    private double countWeight(ArrayList<HashMap<String, String>> left, ArrayList<HashMap<String, String>> right){
        double total = left.size() + right.size();
        return (left.size()/total) * countGini(left) + (right.size()/total) * countGini(right);
    }
    private double countGini(ArrayList<HashMap<String, String>> list){
        String type = Settings.type;
        HashMap<String, Double> types = new HashMap<>();
        for(HashMap<String, String> point : list){
            if(types.containsKey(point.get(type))){
                types.replace(point.get(type), types.get(point.get(type)) + 1);
            }else{
                types.put(point.get(type), 1.0);
            }
        }

        double total = 0;
        for(Double number : types.values()){
            total += (Math.pow(number/list.size(), 2));
        }

        return 1 - total;
    }

    private void printATree() {
        System.out.println();
        for(Node root : data.getRoots()){
            printNode(root, "", true);
        }
    }
    private void printNode(Node node, String prefix, boolean isLast) {
        if (node == null) return;

        System.out.print(prefix);

        if(isLast){
            if(!prefix.isEmpty()){
                System.out.print("└── no  ");
            }else {
                System.out.print("└── ");
            }
        }else{
            System.out.print("├── yes ");
        }

        if (node.getLeftBranch() != null) {
            System.out.println("[" + node.getCheckString() + node.getOption() + "]");

            String childPrefix = prefix + (isLast ? "    " : "│   ");
            printNode(node.getLeftBranch(), childPrefix, false);
            printNode(node.getRightBranch(), childPrefix, true);
        } else {
            System.out.print("[");
            System.out.print(node.getPoints().size() + "x - " + node.getPoints().getFirst().get(Settings.type));
            System.out.println("]");
        }
    }

    private void testTheTree(ArrayList<Node> roots){
        System.out.println();
        double correctCount = 0;
        for(HashMap<String, String> point : data.getTestPoints()){

            HashMap<String, Integer> stringResults = new HashMap<>();
            for(Node root : roots){
                Node leaf = getLeaf(root, point);

                String type = leaf.getPoints().getFirst().get(Settings.type);
                if(roots.size() != 1){
                    System.out.println("subtree guess: " + type);
                }
                if(stringResults.containsKey(type)){
                    stringResults.replace(type, stringResults.get(type) + 1);
                }else{
                    stringResults.put(type, 1);
                }
            }

            int maxNumb = 0;
            String maxString = "";

            for(Map.Entry<String, Integer> entry : stringResults.entrySet()){
                if(entry.getValue() > maxNumb){
                    maxString = entry.getKey();
                    maxNumb = entry.getValue();
                }
            }

            String predicted = maxString;
            String actual = point.get(Settings.type);
            if (Objects.equals(predicted, actual)) {
                correctCount++;
            } else {
                System.out.print("The tree guessed that " + point.get(Settings.type) + " is " + predicted);
                System.out.println(" and it's false!, correct option is " + actual);
            }
        }
        System.out.println("Success rate is " + (correctCount/data.getTestPoints().size())*100 + "%");
    }

    private Node getLeaf(Node node, HashMap<String, String> point){
        Node current = node;

        while(current.getLeftBranch() != null){
            if(current.check(point)){
                current = current.getLeftBranch();
            }else {
                current = current.getRightBranch();
            }
        }

        return current;
    }
}