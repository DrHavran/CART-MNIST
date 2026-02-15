import java.io.File;
import java.util.*;

public class Data {
    private final ArrayList<String> attributes;
    private final ArrayList<HashMap<String, String>> points;
    private final ArrayList<HashMap<String, String>> testPoints;
    private final ArrayList<Node> roots;

    public Data() {
        this.points = new ArrayList<>();
        this.testPoints = new ArrayList<>();
        this.attributes = new ArrayList<>();
        this.roots = new ArrayList<>();
        loadData(points, "train_" + Settings.dataset + ".csv");
        loadData(testPoints, "test_" + Settings.dataset +" .csv");
        System.out.println(points.size() + " points loaded");
        System.out.println(testPoints.size() + " test points loaded");

        loadRoots();
    }

    private void loadData(ArrayList<HashMap<String, String>> list, String fileName) {
        try{
            Scanner sc = new Scanner(new File("data/" + fileName));
            String line = sc.nextLine();

            if(attributes.isEmpty()){
                attributes.addAll(List.of(line.split(",")));
                System.out.println(Arrays.toString(attributes.toArray()));
            }

            while(sc.hasNextLine()) {
                HashMap<String, String> point = new HashMap<>();
                line = sc.nextLine();
                String[] data = line.split(",");
                for(int i = 0; i < data.length; i++) {
                    point.put(attributes.get(i), data[i]);
                }
                list.add(point);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void loadRoots(){
        if(Settings.bagging){
            for(int i = 0; i < Settings.trees; i++){
                Node root = new Node();
                roots.add(root);
            }
            for(int i = 0; i < Settings.trees; i++){
                for(HashMap<String, String> point : points){
                    roots.get((int) (Math.random() * roots.size())).addPoint(point);
                }
            }
        }else{
            Node root = new Node();
            root.setPoints(points);
            roots.add(root);
        }
    }

    public boolean answer(){
        try{
            double check = Double.parseDouble(points.getFirst().get(Settings.type));
            System.out.println(check);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }
    public ArrayList<Node> getRoots(){ return roots; }
    public ArrayList<HashMap<String, String>> getTestPoints() {
        return testPoints;
    }
    public ArrayList<String> getAttributes() {
        return attributes;
    }
}