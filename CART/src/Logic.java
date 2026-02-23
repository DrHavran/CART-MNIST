import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);

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

            List<Double> weights = Collections.synchronizedList(new ArrayList<>());
            List<String> attributesFinal = Collections.synchronizedList(new ArrayList<>());
            List<Double> options = Collections.synchronizedList(new ArrayList<>());

            ArrayList<Future<?>> futures = new ArrayList<>();
            for(String attribute : attributes){
                futures.add(
                        executor.submit(() -> {
                            System.out.println("checking attribute: " + attribute);
                            ArrayList<Double> values = new ArrayList<>();
                            for(HashMap<String, String> point : current.getPoints()){
                                values.add(Double.parseDouble(point.get(attribute)));
                            }

                            values = values.stream().sorted().distinct().collect(Collectors.toCollection(ArrayList::new));
                            for(int i = 0; i < values.size()-1; i++){
                                double option = (values.get(i) + values.get(i+1)) / 2;
                                Check req = (j) -> (Double.parseDouble((String) j) < option);
                                double weight = count(current, attribute, req);

                                options.add(option);
                                attributesFinal.add(attribute);
                                weights.add(weight);
                            }
                        })
                );
            }


            for (Future<?> f : futures) {
                try{
                    f.get();
                }catch (Exception e){
                    e.fillInStackTrace();
                }
            }

            System.out.println(weights.size() + " weights");
            System.out.println(attributesFinal.size() + " attributes");
            System.out.println(options.size() + " options");

            for(int i = 0; i < weights.size(); i++){
                Double weight = weights.get(i);
                if(weight < bestWeight){
                    bestWeight = weight;
                    bestAttribute = attributesFinal.get(i);
                    bestOption = " < " + options.get(i);
                    bestReq = (j) -> (Double.parseDouble((String) j) < weight);
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
        executor.shutdown();
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