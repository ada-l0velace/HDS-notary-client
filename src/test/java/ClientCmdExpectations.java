import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public final class ClientCmdExpectations extends Expectations {
    protected ClientCmdExpectations(List<String> cmd, @Mocked final Scanner scn) {
        new Expectations() {
            {
                //scn = new Scanner(System.in);
                scn.nextLine();
                this.result = new Delegate() {
                    int i = 0;

                    public String delegate() {
                        if (i != cmd.size()-1)
                            return cmd.get(i++);
                        else {
                            try {
                                TimeUnit.MILLISECONDS.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return cmd.get(i++);
                        }
                    }
                };
                this.times = cmd.size();
            }
        };
        new Expectations(System.class) {{ System.exit(anyInt); }};
    }
}