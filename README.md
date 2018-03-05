# ScreenRecorder
Class for adding screen recording function to Selenium testcase.

## Install

Follow the steps below to make jar from class and commit to maven's local repository.

```
javac net/hoge2/ScreenRecorder/*.java
jar -cvf ScreenRecorder.jar net/hoge2/ScreenRecorder/ScreenRecorder.class
mvn install:install-file -Dfile=ScreenRecorder.jar -DgroupId=net.hoge2.ScreenRecorder -DartifactId=ScreenRecorder -Dversion=1.0.0 -Dpackaging=jar -DgeneratePom=true
```

or

```
mvn31 install
```

## Usage

Below is the procedure for implementing the screen recording function in your Selenium testcase using this class.

Import classes necessary for screen recording.

```
import java.io.File;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.monte.media.Format;
import org.monte.media.math.Rational;
import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;
import net.hoge2.ScreenRecorder.ScreenRecorder;
```

Define methods and rules for screen recording.

You can specify the folder where the recorded file is stored in the `File file = new File (" record ");` in the second line.

Encodings and compressors for recording by [Monte Media Library] (http://www.randelshofer.ch/monte/) can be specified with parameters `EncodingKey` and` CompressorNameKey` given to method `ScreenRecorder` from line 14 Compression method).

Monte Media Library seems to be able to use encoding and compressor as shown in the following reference URL, but since I could not find a description about which combination is valid, the neighborhood is trial and error.

In the result of trial and error by myself, it is the case that I used the [TechSmith Screen Capture Codec] (https://www.techsmith.com/codecs.html) (`ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE`) specifie like sample both encoding and compressor Although the size of the recording file became small.
you need play the file record with `ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE`, using dedicated player or install required Additional codec for QuickTime Player (https://www.techsmith.com/codecs. html).

In the case of macOS, you can play the recorded file of `TechSmith Screen Capture Codec` by installing the package according to your environment from` EnSharpen decoder` on this page.

Additional codec for QuickTime Player (TechSmith Screen Capture Codec)
[https://www.techsmith.com/codecs.html](https://www.techsmith.com/codecs.html)

JavaDoc page about Monte Media Library
[http://www.randelshofer.ch/monte/javadoc/index.html](http://www.randelshofer.ch/monte/javadoc/index.html)

If you specify `ENCODING_QUICKTIME_CINEPAK` as the encoding and `COMPRESSOR_NAME_QUICKTIME_CINEPAK` as the compressor, the recorded file is created with the Mac OS QuickTime Player ready to play it, but compared to using the TechSmith Screen Capture Codec As the size of it becomes quite large, please try devising encoding and compressor specifications at the right place.

```
  public void startRecording(String name) throws Exception {
    File file = new File("record");

    Dimension screenSize = driver.manage().window().getSize();
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
```

Add start screen record method to initialization process.

```
  @Before
  public void setUp() throws Exception {
    driver = new FirefoxDriver();
    baseUrl = "https://www.google.com";
    startRecording(testName.getMethodName());
  }
```

```
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
  }
```

Added ScreenRecorder dependency to maven project file (pom.xml).

```
  <dependencies>
    <dependency>
       ...
    </dependency>
    <dependency>
      <groupId>net.hoge2.ScreenRecorder</groupId>
      <artifactId>ScreenRecorder</artifactId>
      <version>1.0.0</version>
    </dependency>
  </dependencies>
```

