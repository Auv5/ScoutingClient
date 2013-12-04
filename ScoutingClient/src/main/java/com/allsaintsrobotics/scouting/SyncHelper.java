package com.allsaintsrobotics.scouting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jack on 12/2/13.
 */
public class SyncHelper {
    public static void addMatchFromJson(JSONObject matchJson) throws JSONException {
        int matchId = matchJson.getInt("id");
        int scout = matchJson.getInt("scout");

        JSONArray jsonRed = matchJson.getJSONArray("red");
        JSONArray jsonBlue = matchJson.getJSONArray("blue");

        if (jsonRed.length() != 3 || jsonBlue.length() != 3) {
            throw new JSONException("Red and blue alliances must have three teams.");
        }

        int[] red = new int[3];
        int[] blue = new int[3];

        for (int i = 0; i < jsonRed.length(); i ++)
        {
            red[i] = jsonRed.getInt(i);
            blue[i] = jsonBlue.getInt(i);
        }

        ScoutingDBHelper.getInstance().addMatch(matchId, scout, red, blue);
    }

    public static void addTeamFromJson(JSONArray teamJson) throws JSONException {
        int teamNum = teamJson.getInt(0);
        String teamNickname = teamJson.getString(1);

        ScoutingDBHelper.getInstance().addTeam(teamNum, teamNickname);
    }
}
