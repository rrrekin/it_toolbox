package net.in.rrrekin.ittoolbox.gui.model;

import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;
import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.Map;
import java.util.stream.Collectors;
import net.in.rrrekin.ittoolbox.configuration.AppPreferences;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.Server;
import net.in.rrrekin.ittoolbox.gui.model.NodeForest.Node;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.JavaUnicodeEscaper;
import org.apache.commons.text.translate.LookupTranslator;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author michal.rudewicz@gmail.com */
public class NodeForestConverter {

  private static final Map<CharSequence, CharSequence> LINE_END_UNIFYING_MAPPING =
      ImmutableMap.<CharSequence, CharSequence>builder()
          .put("\b", "\\b")
          .put("\r\n", "\\n")
          .put("\n", "\\n")
          .put("\t", "\\t")
          .put("\f", "\\f")
          .put("\r", "\\n")
          .build();

  private static final CharSequenceTranslator SINGLE_LINE_ESCAPER =
      new AggregateTranslator(
          new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE), JavaUnicodeEscaper.below(32));

  private final @NotNull AppPreferences appPreferences;
  // HTML conversion semi-constants regenerated on each toHtml call
  private String font;
  private int fontSize;
  @Nullable private String std;
  @Nullable private String head;
  private String name;
  private String sub;

  @Inject
  public NodeForestConverter(final @NotNull AppPreferences appPreferences) {
    this.appPreferences = requireNonNull(appPreferences, "appPreferences must not be null");
  }

  public @NotNull String toPlainText(final @NotNull NodeForest selection) {
    return selection.getRoots().stream()
        .map(NodeForestConverter::toPlainText)
        .collect(Collectors.joining("\n"));
  }

  private static CharSequence toPlainText(final Node node) {
    return toPlainText(node, 0);
  }

  private static CharSequence toPlainText(final Node node, final int depth) {
    final String prefix = Strings.repeat("  ", depth);
    final String descriptionHeadPrefix = prefix + "    ";
    final String descriptionPrefix = prefix + "      ";
    final StringBuilder builder = new StringBuilder(512);
    final NetworkNode networkNode = node.value;
    builder
        .append(prefix)
        .append(networkNode.getLocalNodeTypeName())
        .append(": ")
        .append(networkNode.getName())
        .append("\n");

    if (networkNode instanceof Server) {
      builder
          .append(descriptionHeadPrefix)
          .append(localMessage("NODE_LABEL_ADDRESS"))
          .append(" ")
          .append(indent(((Server) networkNode).getAddress(), descriptionPrefix))
          .append("\n");
    }

    builder
        .append(descriptionHeadPrefix)
        .append(localMessage("NODE_LABEL_DESCRIPTION"))
        .append("\n")
        .append(descriptionPrefix)
        .append(indent(networkNode.getDescription(), descriptionPrefix));

    if (!networkNode.getProperties().isEmpty()) {
      builder
          .append("\n")
          .append(descriptionHeadPrefix)
          .append(localMessage("NODE_LABEL_PROPERTIES"))
          .append("\n");

      builder.append(
          networkNode.getProperties().keySet().stream()
              .sorted()
              .map(
                  property ->
                      descriptionPrefix
                          + singleLine(property)
                          + ": "
                          + singleLine(networkNode.getProperties().get(property)))
              .collect(Collectors.joining("\n")));
    }

    if (!networkNode.getServiceDescriptors().isEmpty()) {
      builder
          .append("\n")
          .append(descriptionHeadPrefix)
          .append(localMessage("NODE_LABEL_SERVICES"))
          .append("\n");
      builder.append(
          networkNode.getServiceDescriptors().stream()
              .sorted()
              .map(service -> descriptionPrefix + singleLine(service))
              .collect(Collectors.joining("\n")));
    }

    if (node.hasChildren()) {
      builder
          .append("\n")
          .append(
              node.getChildren().stream()
                  .map(child -> toPlainText(child, depth + 1))
                  .collect(Collectors.joining("\n")));
    }
    return builder;
  }

  private static String singleLine(final String text) {
    if (text == null) {
      return "";
    }
    return SINGLE_LINE_ESCAPER.translate(text);
  }

  private static String indent(final String text, final String prefix) {
    if (text == null) {
      return "";
    }
    final Map<CharSequence, CharSequence> whiteCharsRrestoreMapping =
        ImmutableMap.of("\\n", "\n" + prefix, "\\t", "\t");
    final CharSequenceTranslator escaper =
        new AggregateTranslator(
            new LookupTranslator(LINE_END_UNIFYING_MAPPING),
            JavaUnicodeEscaper.below(32),
            new LookupTranslator(whiteCharsRrestoreMapping));
    return escaper.translate(text);
  }

  public @NotNull String toHtml(final @NotNull NodeForest selection) {
    font = escapeHtml4(appPreferences.getFontFamily());
    fontSize = appPreferences.getFontSize();
    std = String.format("style=\"font-family:'%s';font-size:%dpt;\"", font, fontSize);
    head =
        String.format(
            "style=\"font-family:'%s';font-size:%dpt;font-weight:bold;\"",
            font, (int) (fontSize * 1.3));
    name =
        String.format(
            "style=\"font-family:'%s';font-size:%dpt;font-weight:bold;\"",
            font, (int) (fontSize * 1.2));
    sub =
        String.format(
            "style=\"font-family:'%s';font-size:%dpt;font-style: italic;\"",
            font, (int) (fontSize * 0.8));

    @NonNls
    final String htmlHead =
        String.format(
            "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />"
                + "</head><body %s><div %s>%s</div><div %s>",
            std, head, localMessage("COPY_HTML_TITLE"), std);
    @NonNls final String htmlFoot = "</div></body></html>";

    return htmlHead
        + selection.getRoots().stream()
            .map(this::toHtml)
            .collect(Collectors.joining("</li><li>", "<ul><li>", "</li></ul>"))
        + htmlFoot;
  }

  private CharSequence toHtml(final Node node) {
    final StringBuilder builder = new StringBuilder(512);
    final NetworkNode networkNode = node.value;
// TODO: add icon
    builder
        .append("<div ")
        .append(name)
        .append(">")
        .append(escapeHtml4(networkNode.getName()))
        .append("</div><div ")
        .append(sub)
        .append(">")
        .append(escapeHtml4(networkNode.getLocalNodeTypeName()))
        .append("</div>");

    if (networkNode instanceof Server) {
      builder
          .append("<div><b>")
          .append(localMessage("NODE_LABEL_ADDRESS"))
          .append("</b> ")
          .append(escapeHtml4(((Server) networkNode).getAddress()))
          .append("</div>");
    }

    builder
        .append("<div><b>")
        .append(localMessage("NODE_LABEL_DESCRIPTION"))
        .append("</b></div><div>")
        .append(escapeHtml4(networkNode.getDescription()))
        .append("</div>");

    if (!networkNode.getProperties().isEmpty()) {
      builder
          .append("<div><b>")
          .append(localMessage("NODE_LABEL_PROPERTIES"))
          .append("</b></div><table>");

      builder.append(
          networkNode.getProperties().keySet().stream()
              .sorted()
              .map(
                  property ->
                      "<tr><td "
                          + std
                          + ">"
                          + escapeHtml4(singleLine(property))
                          + "</td><td "
                          + std
                          + ">"
                          + escapeHtml4(singleLine(networkNode.getProperties().get(property)))
                          + "</td></tr>")
              .collect(Collectors.joining()));
      builder.append("</table>");
    }

    if (!networkNode.getServiceDescriptors().isEmpty()) {
      builder
          .append("<div><b>")
          .append(localMessage("NODE_LABEL_SERVICES"))
          .append("</b></div>");
      builder.append(
          networkNode.getServiceDescriptors().stream()
              .sorted()
              .map(service -> "<div>" + escapeHtml4(singleLine(service)) + "</div>")
              .collect(Collectors.joining()));
    }

    if (node.hasChildren()) {
      builder.append("<ul>");
      node.getChildren()
          .forEach(
              child -> {
                builder.append("<li>");
                builder.append(toHtml(child));
                builder.append("</li>");
              });
      builder.append("</ul>");
    }
    return builder;
  }
}
