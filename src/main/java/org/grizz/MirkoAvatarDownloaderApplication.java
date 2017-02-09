package org.grizz;

import com.crozin.wykop.sdk.Application;
import com.crozin.wykop.sdk.Command;
import com.crozin.wykop.sdk.Session;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@SpringBootApplication
public class MirkoAvatarDownloaderApplication {
  private static final String PUBLIC = "aNd401dAPp";
  private static final String SECRET = "3lWf1lCxD6";
  private static final String AVATARS_DIR_STR = "./avatars/";
  private static final Path AVATARS_DIR_PATH = Paths.get(AVATARS_DIR_STR);
  private Application app = new Application(PUBLIC, SECRET);
  private Session session = app.openSession();
  private Gson gson = new Gson();

  private Command entryCmd = new Command("entries", "index");

  public static void main(String[] args) throws IOException {
    SpringApplication application = new SpringApplication(MirkoAvatarDownloaderApplication.class);
    application.setBannerMode(Banner.Mode.OFF);
    ConfigurableApplicationContext context = application.run(args);
    context.getBean(MirkoAvatarDownloaderApplication.class).start(args);
  }

  private void start(String[] args) throws IOException {
    if (args == null || args.length == 0) {
      throw new IllegalArgumentException("\n" +
          "Nie podano ID wpisu! Uruchom aplikację poprzez:\n" +
          "java -jar nazwa_pliku.jar 12435678\n" +
          "gdzie: 12345678 to ID wpisu");
    }
    cleanUpBefore();

    log.info("Downloading entry...");
    String id = args[0];
    entryCmd.addArgument(id);
    Entry entry = gson.fromJson(session.execute(entryCmd), Entry.class);
    log.info("Downloaded with {} voters", entry.getVoters().size());

    entry.getVoters().stream().map(v -> getUser(v.getUsername())).forEach(this::saveAvatar);
    log.info("Done!");
  }

  private void cleanUpBefore() throws IOException {
    if (Files.exists(AVATARS_DIR_PATH)) {
      Files.list(AVATARS_DIR_PATH)
          .forEach(f -> {
            try {
              Files.deleteIfExists(f);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
    } else {
      Files.createDirectory(AVATARS_DIR_PATH);
    }
  }

  private User getUser(String username) {
    Command userCmd = new Command("profile", "index", username);
    return gson.fromJson(session.execute(userCmd), User.class);
  }

  private void saveAvatar(User user) {
    try {
      log.info("Downloading avatar of {}", user.getLogin());
      Path file = Files.createFile(Paths.get("avatars/" + user.getLogin() + ".jpg"));
      byte[] avatarBytes = downloadAvatar(user.getAvatar());
      Files.write(file, avatarBytes);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private byte[] downloadAvatar(String avatar) {
    try {
      if (StringUtils.isEmpty(avatar)) {
        avatar = "http://xR.wykop.pl/cdn/c3397992/bart_212_R5BbiAqRQP,q150.jpg";
      }
      URL avatarUrl = new URL(avatar);
      URLConnection conn = avatarUrl.openConnection();
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);
      conn.connect();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      IOUtils.copy(conn.getInputStream(), baos);

      return baos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
