package net.in.rrrekin.ittoolbox.gui.commands;

import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.base.MoreObjects;
import javafx.beans.property.IntegerProperty;
import javafx.scene.control.TreeItem;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/** @author michal.rudewicz@gmail.com */
public class ModifyNodeCommand extends OperationCommand {

  private final @NotNull TreeItem<NetworkNode> root;
  private final @NotNull int[] itemLocation;
  private final @NotNull NetworkNode modifiedNode;
  private final @NotNull NetworkNode oldNode;

  public ModifyNodeCommand(
      final @NotNull IntegerProperty stateSerialNumber,
      final @NotNull TreeItem<NetworkNode> item,
      final @NotNull NetworkNode modifiedNode) {
    super(stateSerialNumber);
    this.itemLocation = getItemLocation(requireNonNull(item,"item must not be null"));
    this.modifiedNode = requireNonNull(modifiedNode,"modifiedNode must not be null");
    this.oldNode = requireNonNull(item.getValue(),"Existing node must not be null");
    this.root = findRoot(item);
  }

  @Override
  public String getDescription() {
    return StringUtils.abbreviate(localMessage("CMD_EDIT",oldNode.getName()),MAX_DESCRIPTION_LEN);
  }

  @Override
  public TreeItem<NetworkNode> execute() {
    super.execute();
    final TreeItem<NetworkNode> item = locateItem(itemLocation,root);
    item.setValue(modifiedNode);
    item.setGraphic(modifiedNode.getIconDescriptor().getIcon());
    return item;
  }

  @Override
  public void revoke() {
    super.revoke();
    final TreeItem<NetworkNode> item = locateItem(itemLocation,root);
    item.setValue(oldNode);
    item.setGraphic(oldNode.getIconDescriptor().getIcon());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("path", printablePath(itemLocation))
      .add("modifiedNode", modifiedNode)
      .add("oldNode", oldNode)
      .toString();
  }
}
