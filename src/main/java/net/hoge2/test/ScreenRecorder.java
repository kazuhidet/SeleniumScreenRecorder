package net.hoge2.ScreenRecorder;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.monte.media.Format;
import org.monte.media.Registry;

public class ScreenRecorder extends org.monte.screenrecorder.ScreenRecorder {
  private String name;

  public ScreenRecorder(GraphicsConfiguration cfg,
    Rectangle captureArea, Format fileFormat, Format screenFormat,
    Format mouseFormat, Format audioFormat, File movieFolder,
    String name) throws IOException, AWTException {
    super(cfg, captureArea, fileFormat, screenFormat, mouseFormat,
      audioFormat, movieFolder);
    this.name = name;
  }

  @Override
  protected File createMovieFile(Format fileFormat) throws IOException {
    if (!movieFolder.exists()) {
      movieFolder.mkdirs();
    }
    else if (!movieFolder.isDirectory()) {
      throw new IOException("\"" + movieFolder + "\" is not a directory.");
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    return new File(movieFolder,
      name + "-" + dateFormat.format(new Date()) + "."
      + Registry.getInstance().getExtension(fileFormat));
  }
}
