import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Data {

    private final ArrayList<File> images;

    public Data() {
        this.images = new ArrayList<>();
        loadImages();
    }

    private void loadImages() {
        File folder = new File("data");
        File[] subfolders = folder.listFiles();
        assert subfolders != null;
        for (File sub : subfolders) {
            File[] files = sub.listFiles();
            assert files != null;
            images.addAll(Arrays.asList(files));
        }
    }

    public ArrayList<File> getImages() {
        return images;
    }
}
