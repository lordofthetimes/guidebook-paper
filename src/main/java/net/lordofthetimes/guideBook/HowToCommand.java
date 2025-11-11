package net.lordofthetimes.guideBook;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HowToCommand implements CommandExecutor {


    private JavaPlugin plugin;

    public HowToCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player player){
            if (!player.hasPermission("guidebook.open")) {
                player.sendMessage("You don't have permission to use this command!");
                return true;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                openBook(player);
            });
            return true;
        }
        return false;
    }

    private void openBook(Player player){
        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) bookItem.getItemMeta();

        meta.setTitle("HowTo");
        meta.setAuthor("Server");


        List<?> content = this.plugin.getConfig().getList("howto.content");
        Component bookContent;
        List<Component> parts = new ArrayList<>();
        if(content != null){
            for(Object obj : content){
                Component part = null;
                if(obj instanceof String text){
                    parts.add(MiniMessage.miniMessage().deserialize(text));
                } else if(obj instanceof Map<?, ?> map){
                    if (map.containsKey("link")) {
                        Object maybeLink = map.get("link");
                        if (maybeLink instanceof Map<?, ?> link) {
                            if (link.containsKey("text") && link.containsKey("url")) {
                                parts.add(
                                        MiniMessage.miniMessage().deserialize(link.get("text").toString())
                                                .clickEvent(ClickEvent.openUrl(link.get("url").toString()))
                                );
                            }
                        }
                    }
                }
            }
            bookContent = Component.join(Component.newline(), parts);
        }
        else{
            bookContent = Component.text("The book content was not configured");
        }

        meta.addPages(bookContent);

        bookItem.setItemMeta(meta);

        player.openBook(bookItem);
    }
}
