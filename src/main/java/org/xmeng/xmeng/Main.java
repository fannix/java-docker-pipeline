package org.xmeng.xmeng;


public class Main {

    public static void main(String [] args) {
        if (args.length != 1) {
            System.exit(-1);
        }

        switch (args[0]) {
            case "filter":
                Filter.filter();
                break;
            case "reader":
                Reader.read();
                break;
            default:
                System.exit(-1);
        }
    }
}
