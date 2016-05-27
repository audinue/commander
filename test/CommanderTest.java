
import io.github.audinue.commander.Command;
import io.github.audinue.commander.Commander;
import io.github.audinue.commander.Option;

public class CommanderTest {

    public static void main(String... args) {
        Commander c = new Commander(new Addition());

        try {
            c.execute("1", "2");
        } catch (Exception e) {
            c.printException(e);
        }
        // 1 + 2 = 3

        try {
            c.execute("not a number");
        } catch (Exception e) {
            c.printException(e);
        }
        // Error: For input string: "not a number"
        //
        // Usage: add <number> <number> [options]
        //
        // Options:
        //
        // -h, --help                 Show this help.
        // -a, --additional-number    Set the additional number.

        try {
            c.execute("-h");
        } catch (Exception e) {
            c.printException(e);
        }
        // Usage: add <number> <number> [options]
        //
        // Options:
        //
        // -h, --help                 Show this help.
        // -a, --additional-number    Set the additional number.
        
        try {
            c.execute("1", "2", "-a", "3");
        } catch (Exception e) {
            c.printException(e);
        }
        // 1 + 2 + 3= 6
    }

}

class Addition {

    int additionalNumber;

    @Option(name = "-a", alias = "--additional-number", description = "Set the additional number.")
    void setAdditionalNumber(String additionalNumber) {
        this.additionalNumber = Integer.parseInt(additionalNumber);
    }

    @Option(name = "-h", alias = "--help", description = "Show this help.")
    void help() {
        throw new RuntimeException();
    }

    @Command(usage = "add <number> <number> [options]")
    void add(String... args) {
        int a = Integer.parseInt(args[0]);
        int b = Integer.parseInt(args[1]);
        int c = a + b + additionalNumber;
        if (additionalNumber == 0) {
            System.out.printf("%d + %d = %d\n", a, b, c);
        } else {
            System.out.printf("%d + %d + %d = %d\n", a, b, additionalNumber, c);
        }
    }
}
