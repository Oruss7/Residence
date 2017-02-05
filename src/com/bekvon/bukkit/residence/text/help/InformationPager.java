package com.bekvon.bukkit.residence.text.help;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.rent.RentableLand;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.GetTime;
import com.bekvon.bukkit.residence.utils.RawMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InformationPager {
    Residence plugin;

    public InformationPager(Residence plugin) {
	this.plugin = plugin;
    }

    public void printInfo(CommandSender sender, String command, String title, String[] lines, int page) {
	printInfo(sender, command, title, Arrays.asList(lines), page);
    }

    public void printInfo(CommandSender sender, String command, String title, List<String> lines, int page) {
	int perPage = 6;
	int start = (page - 1) * perPage;
	int end = start + perPage;
	int pagecount = (int) Math.ceil((double) lines.size() / (double) perPage);
	if (pagecount == 0)
	    pagecount = 1;
	if (page > pagecount) {
	    sender.sendMessage(ChatColor.RED + plugin.msg(lm.Invalid_Page));
	    return;
	}
	plugin.msg(sender, lm.InformationPage_TopLine, title);
	plugin.msg(sender, lm.InformationPage_Page, plugin.msg(lm.General_GenericPages, String.format("%d", page),
	    pagecount, lines.size()));
	for (int i = start; i < end; i++) {
	    if (lines.size() > i)
		sender.sendMessage(ChatColor.GREEN + lines.get(i));
	}

	RawMessage rm = new RawMessage();
	if (page > 1)
	    rm.add(plugin.msg(lm.General_PrevInfoPage), plugin.msg(lm.General_PrevInfoPage), command + " " + (page - 1));
	else
	    rm.add(plugin.msg(lm.General_PrevInfoPage));
	if (pagecount > page)
	    rm.add(plugin.msg(lm.General_NextInfoPage), plugin.msg(lm.General_NextInfoPage), command + " " + (page + 1));
	else
	    rm.add(plugin.msg(lm.General_NextInfoPage));

	if (pagecount != 1)
	    rm.show(sender);

//	if (pagecount > page)
//	    plugin.msg(sender, lm.InformationPage_NextPage, plugin.msg(lm.General_NextPage));
//	else
//	    plugin.msg(sender, lm.InformationPage_NoNextPage);
    }

    public void printListInfo(CommandSender sender, String targetPlayer, TreeMap<String, ClaimedResidence> ownedResidences, int page, boolean resadmin) {

	int perPage = 20;
	if (sender instanceof Player)
	    perPage = 6;
	int start = (page - 1) * perPage;
	int end = start + perPage;

	int pagecount = (int) Math.ceil((double) ownedResidences.size() / (double) perPage);
	if (page == -1) {
	    start = 0;
	    end = ownedResidences.size();
	    page = 1;
	    pagecount = 1;
	}

	if (pagecount == 0)
	    pagecount = 1;
	if (page > pagecount) {
	    sender.sendMessage(ChatColor.RED + plugin.msg(lm.Invalid_Page));
	    return;
	}
	if (targetPlayer != null)
	    plugin.msg(sender, lm.InformationPage_TopLine, plugin.msg(lm.General_Residences) + " - " + targetPlayer);
	plugin.msg(sender, lm.InformationPage_Page, plugin.msg(lm.General_GenericPages, String.format("%d", page),
	    pagecount, ownedResidences.size()));

	String cmd = "res";
	if (resadmin)
	    cmd = "resadmin";

	if (ownedResidences.size() < end)
	    end = ownedResidences.size();

	if (!(sender instanceof Player)) {
	    printListWithDelay(sender, ownedResidences, start, resadmin);
	    return;
	}

	List<String> linesForConsole = new ArrayList<String>();
	int y = 0;

	for (Entry<String, ClaimedResidence> resT : ownedResidences.entrySet()) {
	    y++;
	    if (ownedResidences.size() < y)
		break;

	    if (y <= start)
		continue;
	    if (y > end)
		break;

	    ClaimedResidence res = resT.getValue();
	    StringBuilder StringB = new StringBuilder();
	    StringB.append(" " + plugin.msg(lm.General_Owner, res.getOwner()));
	    String worldInfo = "";

	    if (res.getPermissions().has("hidden", FlagCombo.FalseOrNone) && res.getPermissions().has("coords", FlagCombo.TrueOrNone) || resadmin) {
		worldInfo += "&6 (&3";
		CuboidArea area = res.getAreaArray()[0];
		worldInfo += plugin.msg(lm.General_CoordsTop, area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc()
		    .getBlockZ());
		worldInfo += "&6; &3";
		worldInfo += plugin.msg(lm.General_CoordsBottom, area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc()
		    .getBlockZ());
		worldInfo += "&6)";
		worldInfo = ChatColor.translateAlternateColorCodes('&', worldInfo);
		StringB.append("\n" + worldInfo);
	    }

	    StringB.append("\n " + plugin.msg(lm.General_CreatedOn, GetTime.getTime(res.getCreateTime())));

	    String ExtraString = "";
	    if (res.isForRent()) {
		if (res.isRented()) {
		    ExtraString = " " + plugin.msg(lm.Residence_IsRented);
		    StringB.append("\n " + plugin.msg(lm.Residence_RentedBy, res.getRentedLand().player));
		} else {
		    ExtraString = " " + plugin.msg(lm.Residence_IsForRent);
		}
		RentableLand rentable = res.getRentable();
		StringB.append("\n " + plugin.msg(lm.General_Cost, rentable.cost, rentable.days));
		StringB.append("\n " + plugin.msg(lm.Rentable_AllowRenewing, rentable.AllowRenewing));
		StringB.append("\n " + plugin.msg(lm.Rentable_StayInMarket, rentable.StayInMarket));
		StringB.append("\n " + plugin.msg(lm.Rentable_AllowAutoPay, rentable.AllowAutoPay));
	    }

	    if (res.isForSell()) {
		ExtraString = " " + plugin.msg(lm.Residence_IsForSale);
		StringB.append("\n " + plugin.msg(lm.Economy_LandForSale) + " " + res.getSellPrice());
	    }

	    String tpFlag = "";
	    String moveFlag = "";
	    if (sender instanceof Player && !res.isOwner(sender)) {
		tpFlag = res.getPermissions().playerHas((Player) sender, Flags.tp, true) ? ChatColor.DARK_GREEN + "T" : ChatColor.DARK_RED + "T";
		moveFlag = res.getPermissions().playerHas(sender.getName(), Flags.move, true) ? ChatColor.DARK_GREEN + "M" : ChatColor.DARK_RED + "M";
	    }

	    String msg = plugin.msg(lm.Residence_ResList, y, res.getName(), res.getWorld(), tpFlag + moveFlag, ExtraString);

	    if (sender instanceof Player)
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + plugin.getResidenceManager().convertToRaw(null, msg,
		    StringB.toString(), cmd + " tp " + res.getName()));
	    else {
		linesForConsole.add(msg + " " + StringB.toString().replace("\n", ""));
	    }
	}

	if (targetPlayer != null)
	    ShowPagination(sender, pagecount, page, cmd + " list " + targetPlayer);
	else
	    ShowPagination(sender, pagecount, page, cmd + " listall");
    }

    private void printListWithDelay(final CommandSender sender, final TreeMap<String, ClaimedResidence> ownedResidences, final int start, final boolean resadmin) {

	int i = start;
	for (Entry<String, ClaimedResidence> resT : ownedResidences.entrySet()) {
	    i++;
	    if (i >= start + 100)
		break;
	    if (ownedResidences.size() <= i)
		break;

	    ClaimedResidence res = resT.getValue();
	    StringBuilder StringB = new StringBuilder();
	    StringB.append(" " + plugin.msg(lm.General_Owner, res.getOwner()));
	    String worldInfo = "";

	    if (res.getPermissions().has("hidden", FlagCombo.FalseOrNone) && res.getPermissions().has("coords", FlagCombo.TrueOrNone) || resadmin) {
		worldInfo += "&6 (&3";
		CuboidArea area = res.getAreaArray()[0];
		worldInfo += plugin.msg(lm.General_CoordsTop, area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc()
		    .getBlockZ());
		worldInfo += "&6; &3";
		worldInfo += plugin.msg(lm.General_CoordsBottom, area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc()
		    .getBlockZ());
		worldInfo += "&6)";
		worldInfo = ChatColor.translateAlternateColorCodes('&', worldInfo);
		StringB.append("\n" + worldInfo);
	    }

	    StringB.append("\n " + plugin.msg(lm.General_CreatedOn, GetTime.getTime(res.getCreateTime())));

	    String ExtraString = "";
	    if (res.isForRent()) {
		if (res.isRented()) {
		    ExtraString = " " + plugin.msg(lm.Residence_IsRented);
		    StringB.append("\n " + plugin.msg(lm.Residence_RentedBy, res.getRentedLand().player));
		} else {
		    ExtraString = " " + plugin.msg(lm.Residence_IsForRent);
		}
		RentableLand rentable = res.getRentable();
		StringB.append("\n " + plugin.msg(lm.General_Cost, rentable.cost, rentable.days));
		StringB.append("\n " + plugin.msg(lm.Rentable_AllowRenewing, rentable.AllowRenewing));
		StringB.append("\n " + plugin.msg(lm.Rentable_StayInMarket, rentable.StayInMarket));
		StringB.append("\n " + plugin.msg(lm.Rentable_AllowAutoPay, rentable.AllowAutoPay));
	    }

	    if (res.isForSell()) {
		ExtraString = " " + plugin.msg(lm.Residence_IsForSale);
		StringB.append("\n " + plugin.msg(lm.Economy_LandForSale) + " " + res.getSellPrice());
	    }

	    String msg = plugin.msg(lm.Residence_ResList, (i + 1), res.getName(), res.getWorld(), "", ExtraString);

	    msg = ChatColor.stripColor(msg + " " + StringB.toString().replace("\n", ""));
	    msg = msg.replaceAll("\\s{2}", " ");
	    sender.sendMessage(msg);
	}

//	if (ownedResidences.size() > 100) {
//	    i = 0;
//	    while (i < 100) {
//		i++;
//		ownedResidences.remove(ownedResidences.firstKey());
//	    }
//	}

	if (ownedResidences.isEmpty()) {
	    return;
	}

	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    @Override
	    public void run() {
		printListWithDelay(sender, ownedResidences, start + 100, resadmin);
		return;
	    }
	}, 5L);

    }

    public void ShowPagination(CommandSender sender, int pageCount, int CurrentPage, String cmd) {
	if (!cmd.startsWith("/"))
	    cmd = "/" + cmd;
	String separator = ChatColor.GOLD + "";
	String simbol = "\u25AC";
	for (int i = 0; i < 10; i++) {
	    separator += simbol;
	}

	if (pageCount == 1)
	    return;

	int NextPage = CurrentPage + 1;
	NextPage = CurrentPage < pageCount ? NextPage : CurrentPage;
	int Prevpage = CurrentPage - 1;
	Prevpage = CurrentPage > 1 ? Prevpage : CurrentPage;

	RawMessage rm = new RawMessage();
	rm.add(separator + " " + plugin.msg(lm.General_PrevInfoPage), CurrentPage > 1 ? "<<<" : null, CurrentPage > 1 ? cmd + " " + Prevpage : null);
	rm.add(plugin.msg(lm.General_NextInfoPage) + " " + separator, pageCount > CurrentPage ? ">>>" : null, pageCount > CurrentPage ? cmd + " " + NextPage : null);
	if (pageCount != 0)
	    rm.show(sender);
    }
}
