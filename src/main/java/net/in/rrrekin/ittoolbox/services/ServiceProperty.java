package net.in.rrrekin.ittoolbox.services;

import com.google.common.base.MoreObjects;
import java.util.Map;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.services.exceptions.ServiceExecutionException;
import net.in.rrrekin.ittoolbox.utilities.StringUtils;
import net.in.rrrekin.ittoolbox.utilities.exceptions.TemplateException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author michal.rudewicz@gmail.com */
@NonNls
public class ServiceProperty {

  @NonNls private static final Logger log = LoggerFactory.getLogger(ServiceProperty.class);

  private @NotNull String name;
  private @NotNull String rawValue;
  private boolean evaluate;
  private @NotNull PropertyType type;

  public ServiceProperty(
      final @NotNull String name,
      final @NotNull String rawValue,
      final boolean evaluate,
      final @NotNull PropertyType type) {
    this.name = name;
    this.rawValue = rawValue;
    this.evaluate = evaluate;
    this.type = PropertyType.valueOf(String.valueOf(type));
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull final String name) {
    this.name = name;
  }

  @NotNull
  public String getRawValue() {
    return rawValue;
  }

  public void setRawValue(@NotNull final String rawValue) {
    this.rawValue = rawValue;
  }

  public boolean isEvaluate() {
    return evaluate;
  }

  public void setEvaluate(final boolean evaluate) {
    this.evaluate = evaluate;
  }

  @NotNull
  public PropertyType getType() {
    return type;
  }

  public void setType(final @NotNull PropertyType type) {
    this.type = type;
  }

  public @NotNull Object getValueFor(final @NotNull NetworkNode node)
      throws ServiceExecutionException {

    final String stringValue;
    if (evaluate) {
      final Map<String, Object> variables = Map.of("server", node, "node", node);
      log.debug(
          "Evaluating property '{}', templatee: {}, variables: {}", name, rawValue, variables);

      try {
        stringValue = StringUtils.applyTemplate(rawValue, variables);
      } catch (final TemplateException e) {
        throw new ServiceExecutionException("ERR_CANNOT_APPLY_TEMPLATE", e, rawValue, variables);
      }
    } else {
      stringValue = rawValue;
    }

    log.debug("Property '{}' ({}) value: {}", name, type, stringValue);
    switch (type) {
      case INT:
        try {
          return Integer.valueOf(stringValue);
        } catch (final Exception e) {
          log.error("Cannot convert {} to integer: {}", stringValue, e.getMessage());
          return 0;
        }
      case DOUBLE:
        try {
          return Double.valueOf(stringValue);
        } catch (final Exception e) {
          log.error("Cannot convert {} to double: {}", stringValue, e.getMessage());
          return 0;
        }
      case STRING:
        return stringValue;
      case BOOLEAN:
        final String lcValue = stringValue.trim().toLowerCase();
        return "true".equals(lcValue)
            || "yes".equals(lcValue)
            || "1".equals(lcValue)
            || "on".equals(lcValue)
            || "+".equals(lcValue);
      default:
        throw new IllegalStateException("Unknown property type");
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("rawValue", rawValue)
        .add("evaluate", evaluate)
        .add("type", type)
        .toString();
  }
}
