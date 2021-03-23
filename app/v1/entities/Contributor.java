package v1.entities;

public class Contributor {

    public Contributor(String name , int contributions){
        this.name = name;
        this.contributions = contributions;
    }

    private String name;

    private int contributions;

    public String getName() {
        return name;
    }

    public int getContributions() {
        return contributions;
    }


    public int sumContributions(int contribution){

        return this.contributions + contribution;
    }

    @Override
    public String toString() {
        return "Contributor{" +
                "name='" + name + '\'' +
                ", contributions=" + contributions +
                '}';
    }
}
