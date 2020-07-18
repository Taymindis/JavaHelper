import com.github.taymindis.NonSuckLockRequest;
import com.github.taymindis.NonSuckSSLIssueHttp;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnitTests {
    private static final Logger logger = Logger.getLogger(UnitTests.class.getName());
    private void testRequest(String custid, boolean checkLock) {

        NonSuckLockRequest lockRequest = new NonSuckLockRequest(custid);

        try {
            boolean isLock = lockRequest.tryLock();
            if(checkLock) {
                Assert.assertFalse("It should be locked", isLock);
            }
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lockRequest.unlock();
        }

    }

    @Test
    public void testLockRequest() throws InterruptedException {
        logger.log(Level.INFO, "testLockRequest!!!");
        final String testName = "John#@!@!";
        Thread first = new Thread() {
            public void run() {
                testRequest(testName, false);
            }
        };
        Thread second = new Thread() {
            public void run() {
                testRequest(testName, true);

            }
        };

        first.start();

        Thread.sleep(500);

        second.start();

        first.join();
        second.join();


        NonSuckLockRequest lockRequest = new NonSuckLockRequest(testName);

        Assert.assertTrue("It should not locked", lockRequest.tryLock());

        lockRequest.unlock();
    }

    private void testGetCall(String url) {
        logger.log(Level.INFO, "testNonSuckSSLRequest - " + url);

        try {
            NonSuckSSLIssueHttp http = new NonSuckSSLIssueHttp(true);

            http.getRequest(url, NonSuckSSLIssueHttp.useXFormUrlEncoded());

            Assert.assertEquals(200, http.getStatusCode());
            Assert.assertNotNull("No Response found", http.getResponse());
            Assert.assertNull("error while calling ", http.getErrMessage());
        } catch (IOException e) {
            Assert.fail("Error while calling");
        }
    }

    @Test
    public void testNonSuckSSLRequest() {
        testGetCall("https://www.google.com.sg");
        testGetCall("http://www.google.com.sg");
    }

}
