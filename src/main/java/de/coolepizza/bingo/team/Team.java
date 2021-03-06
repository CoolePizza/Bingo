package de.coolepizza.bingo.team;

import org.bukkit.Material;


public enum Team {
    WHITE(Material.WHITE_BED , 1),
    RED(Material.RED_BED , 2),
    BLUE(Material.BLUE_BED , 3),
    YELLOW(Material.YELLOW_BED , 4),
    GREEN(Material.GREEN_BED , 5),
    CYAN(Material.CYAN_BED , 6),
    BLACK(Material.BLACK_BED , 7),
    BROWN(Material.BROWN_BED , 8),
    SPECTATOR(Material.BEDROCK , 9);

    int teamid;
    Material mat;
    Team(Material selectionitems , int teamid){

        this.teamid = teamid;
        mat = selectionitems;
    }

    public String getScoreboardPrefix() {
        if (this == SPECTATOR){
            return "Spec | ";
        }
        return "T" + teamid + " | ";
    }

    public Material getMat() {
        return mat;
    }

    public int getTeamid() {
        return teamid;
    }
}
