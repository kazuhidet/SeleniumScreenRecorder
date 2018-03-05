package net.hoge2.ScreenRecorder;

import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import net.hoge2.PreventMultiInstance.PreventMultiInstance;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;

import java.io.File;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.monte.media.Format;
import org.monte.media.math.Rational;
import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class ScreenRecorderTest {
  private WebDriver driver;
  private String baseUrl;
  private boolean acceptNextAlert = true;
  private StringBuffer verificationErrors = new StringBuffer();
  private Integer defaultWaitSeconds = 10;
  private PreventMultiInstance pmi = null;

  public void startRecording(String name) throws Exception {
    File file = new File("record");

    Dimension screenSize = driver.manage().window().getSize();;
    int width = screenSize.width;
    int height = screenSize.height;

    java.awt.Rectangle captureSize = new java.awt.Rectangle(0,0, width, height);

    GraphicsConfiguration gc = GraphicsEnvironment
      .getLocalGraphicsEnvironment()
      .getDefaultScreenDevice()
      .getDefaultConfiguration();

    this.screenRecorder = new ScreenRecorder(gc, captureSize,
      new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_QUICKTIME),
      new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
        CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
        DepthKey, 24, FrameRateKey, Rational.valueOf(15),
        QualityKey, 1.0f,
        KeyFrameIntervalKey, 15 * 60),
      new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black",
        FrameRateKey, Rational.valueOf(30)),
      null, file, name);
    this.screenRecorder.start();
  }

  public void stopRecording() throws Exception {
    this.screenRecorder.stop();
  }

  private ScreenRecorder screenRecorder;

  @Rule
  public TestName testName = new TestName();

  @Rule
  public TestWatcher watchman = new TestWatcher() {

    @Override
    protected void succeeded(Description d) {
      System.out.println("I am succeeded() method. name -> " + d.getMethodName());
      // Comment out the next line if leaving the recorded file even if the test is successful.
      screenRecorder.getCreatedMovieFiles().stream().filter(a -> a.toPath().toString().contains(testName.getMethodName())).forEach(a -> a.deleteOnExit());
    }

    @Override
    protected void failed(Throwable th, Description d) {
      System.out.println("I am failed() method. name -> " + d.getMethodName());
      System.out.println(th.toString());
    }
  };

  @Before
  public void setUp() throws Exception {
    try {
      pmi = new PreventMultiInstance(new File("/tmp/"));
      while (!pmi.tryLock()) {
        Thread.sleep(1000);
      }
    }
    catch (Exception e) {
      System.out.println(e);
      pmi.close();
      throw new RuntimeException(e.toString());
    }

    FirefoxOptions options = new FirefoxOptions();
    // It does not work with headless.
    //options.addArguments("-headless");
    driver = new FirefoxDriver(options);

    baseUrl = "https://www.google.com";
    startRecording(testName.getMethodName());
  }

  @Test
  public void testScreenRecorderTest() throws Exception {
    WebDriverWait wait = new WebDriverWait(driver, defaultWaitSeconds);
    driver.get(baseUrl);
    Thread.sleep(1000);
    WebElement element;
    element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@title='Search' and @type='text']")));
    element.clear();
    element.sendKeys("Search keyword here");
    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='button' and @value='Google Search']"))).click();
    Thread.sleep(10000);
  }

  @After
  public void tearDown() throws Exception {
    // If you stop recording immediately after the test, the results may not be recorded until the end so sleep.
    Thread.sleep(5000);
    stopRecording();
    driver.quit();
    String verificationErrorString = verificationErrors.toString();
    if (!"".equals(verificationErrorString)) {
      fail(verificationErrorString);
    }
    pmi.release();
    pmi.close();
  }

  private boolean isElementPresent(By by) {
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  private boolean isAlertPresent() {
    try {
      driver.switchTo().alert();
      return true;
    } catch (NoAlertPresentException e) {
      return false;
    }
  }

  private String closeAlertAndGetItsText() {
    try {
      Alert alert = driver.switchTo().alert();
      String alertText = alert.getText();
      if (acceptNextAlert) {
        alert.accept();
      } else {
        alert.dismiss();
      }
      return alertText;
    } finally {
      acceptNextAlert = true;
    }
  }

  private boolean isElementPresentAndDisplayed(By by) {
    return isElementPresentAndDisplayed(by, defaultWaitSeconds);
  }

  private boolean isElementPresentAndDisplayed(By by, Integer secWait) {
    try {
      WebDriverWait wait = new WebDriverWait(driver, secWait);
      wait.until(ExpectedConditions.visibilityOfElementLocated(by));
      return true;
    } catch (TimeoutException e) {
      return false;
    }
  }
}
