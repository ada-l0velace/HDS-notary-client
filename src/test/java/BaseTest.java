import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class BaseTest extends DBTestCase {
    protected int serverPort = 19999;

    public BaseTest (String name) {
        super( name );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, "org.sqlite.JDBC" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, "jdbc:sqlite:../HDS-notary-server/db/hds.db" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, "" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, "" );
    }

    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(new FileInputStream("dbunitData.xml"));
    }

    protected DatabaseOperation getSetUpOperation() throws Exception {
        return DatabaseOperation.CLEAN_INSERT;
    }

    protected DatabaseOperation getTearDownOperation() throws Exception {
        return DatabaseOperation.CLEAN_INSERT;
    }

    protected Boolean serverIsUp() {
        try {
            Socket s = new Socket("localhost", serverPort);
            s.close();
            return true;
        }
        catch (ConnectException e) {
            return false;
        }
        catch (Exception e) {
            return false;
        }
    }

    public String sendTo(String hostname, int port, String payload) {
        boolean sent = false;

        try {
            // getting localhost ip
            InetAddress ip = InetAddress.getByName(hostname);

            // establish the connection with server port 5056
            Socket s = new Socket(ip, port);

            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(payload);

            String value = dis.readUTF();
            s.close();
            dis.close();
            dos.close();
            return value;
        } catch (UnknownHostException e) {
            // TODO
        } catch (IOException e) {
            // TODO
        }
        return "";
    }


}
