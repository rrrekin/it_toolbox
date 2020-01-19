package net.in.rrrekin.ittoolbox.services;

import java.io.IOException;
import java.io.InputStream;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.Nullable;

/** @author michal.rudewicz@gmail.com */
public enum ServiceType {
  PING("ping.png"),
  TRACEROUTE("traceroute.png"),
  EXECUTE("execute.png"),
  SSH("ssh.png"),
  TELNET("telnet.png"),
  NETCAT("netcat.png"),
  NMAP("nmap.png");

  private final @Nullable Image icon;

  ServiceType(final @Nullable String name) {
    Image image = null;
    if (name != null) {
      try (final InputStream input = getClass().getResourceAsStream("/icons/services/" + name)) {;
        image = new Image(input);
      } catch (final IOException ignore) {
        // ignore
      }
    }
    icon = image;
  }

  public @Nullable Node getIcon() {
    if (icon != null) {
      return new ImageView(icon);
    } else {
      return null;
    }
  }
}
