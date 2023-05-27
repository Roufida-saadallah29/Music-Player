package dz.esi.tdm.myapplication;

public class favorisSongClass {

    private String name;
    private int id;

    public int getId() {
        return id;
    }

    public favorisSongClass(String name, int id) {
        this.name = name;
        this.id = id;
    }

    private int imageResId=R.drawable.music_icon;


    public favorisSongClass(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}

