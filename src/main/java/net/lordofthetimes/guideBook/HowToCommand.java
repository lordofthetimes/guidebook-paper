package net.lordofthetimes.guideBook;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HowToCommand implements CommandExecutor, TabCompleter {


    private JavaPlugin plugin;

    public HowToCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player player) {
            if (!player.hasPermission("guidebook.howto")) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>[GuideBook] You don't have permission for this command!</bold></red>"));
                return true;
            }
            if(args.length != 1){
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red><bold>[GuideBook] Wrong amount of arguments, correct format /howto <name> !</bold></red>"));
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                openBook(player, args[0]);
            });
            return true;
        }
        return false;
    }

    private void openBook(Player player, String id) {
        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) bookItem.getItemMeta();

        meta.setTitle("GuideBook");
        meta.setAuthor("GuideBook");


        ConfigurationSection book = this.plugin.getConfig().getConfigurationSection("books."+id);

        if (book == null) {
            // Book doesn't exist at all
            player.sendMessage(
                    MiniMessage.miniMessage().deserialize("<red><bold>[GuideBook] The book has not exist!</bold></red>"));
        } else if (book.getKeys(false).isEmpty()) {
            // Book exists but has no pages
            player.sendMessage(
                    MiniMessage.miniMessage().deserialize("<red><bold>[GuideBook] The book has no pages!</bold></red>"));
        }else{
            //loops over every page
            for(String page : book.getKeys(false)){

                List<?> content = book.getList(page);
                Component bookContent;
                List<Component> parts = new ArrayList<>();

                //if page has content set
                if (content != null) {
                    for (Object obj : content) {
                        if (obj instanceof String text) {
                            parts.add(MiniMessage.miniMessage().deserialize(text));
                        } else if (obj instanceof Map<?, ?> map && map.containsKey("text")) {

                            Component part = MiniMessage.miniMessage().deserialize(map.get("text").toString());

                            if (map.containsKey("url"))
                                part = part.clickEvent(ClickEvent.openUrl(map.get("url").toString()));

                            if (map.containsKey("hover"))
                                part = part.hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize(map.get("hover").toString())));

                            parts.add(part);

                        }
                    }
                    bookContent = Component.join(Component.newline(), parts);
                    meta.addPages(bookContent);
                }
            }

            bookItem.setItemMeta(meta);

            player.openBook(bookItem);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if(commandSender instanceof Player player){
            if(args.length == 1){
                List<String> list = new ArrayList<>();
                Set<String> books = this.plugin.getConfig().getConfigurationSection("books").getKeys(false);
                for(String id : books){
                    if(player.hasPermission("guidebook.howto."+id)){
                        list.add(id);
                    }
                }
                return list;
            }
        }
        return List.of();
    }
}
