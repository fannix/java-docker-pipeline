public class Main {

    public static void main(String [] args) {
        if (args.length != 1) {
            System.exit(-1);
        }

        switch (args[0]) {
            case "reader":
                Reader.read();
                break;
            case "filter":
                Filter.filter();
                break;
        }
        System.exit(-1);
    }
}
