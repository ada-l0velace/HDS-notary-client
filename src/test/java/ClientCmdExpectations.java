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
                        /*if (cmd.get(i).equals("Exit")) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }*/
                        return cmd.get(i++);
                    }
                };
                this.times = cmd.size();
            }
        };
        //new Expectations(System.class) {{ System.exit(anyInt); }};
    }
}