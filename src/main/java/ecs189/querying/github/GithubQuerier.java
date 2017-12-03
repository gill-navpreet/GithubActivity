package ecs189.querying.github;

import ecs189.querying.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Vincent on 10/1/2017.
 */
public class GithubQuerier {

    private static final String BASE_URL = "https://api.github.com/users/";

    public static String eventsAsHTML(String user) throws IOException, ParseException {
        List<JSONObject> response = getEvents(user);
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");

        for (int i = 0; i < response.size(); i++) {
            JSONObject event = response.get(i);
            // Get event type
            String type = event.getString("type");
            // Head Sha
            JSONObject payLoad = event.getJSONObject("payload");
            // Get the commit
            JSONArray commit = payLoad.getJSONArray("commits");

            // Get created_at date, and format it in a more pleasant style
            String creationDate = event.getString("created_at");
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM, yyyy");
            Date date = inFormat.parse(creationDate);
            String formatted = outFormat.format(date);

            // Add type of event as header
            sb.append("<h3 class=\"type\">");
            sb.append(type);
            sb.append("</h3>");
            // Add formatted date
            sb.append(" on ");
            sb.append(formatted);
            sb.append("<br />");
            // Add collapsible JSON textbox (don't worry about this for the homework; it's just a nice CSS thing I like)
            sb.append("<a data-toggle=\"collapse\" href=\"#event-" + i + "\">JSON</a>");
            sb.append("<div id=event-" + i + " class=\"collapse\" style=\"height: auto;\"> <pre>");
            sb.append(event.toString());
            sb.append("</pre> </div>");

            // Vertical list addition for adding name/email/sha/commit message
            for (int j=0; j<commit.length(); j++) {
                JSONObject tempRoot = commit.getJSONObject(j);
                String sha = tempRoot.getString("sha");
                JSONObject author = tempRoot.getJSONObject("author");
                String name = author.getString("name");
                String email = author.getString("email");
                String message = tempRoot.getString("message");

                if (j == 0) {//display name and email once
                    sb.append("<p>");
                    sb.append("name: ");
                    sb.append(name);
                    sb.append("</p>");

                    sb.append("<p>");
                    sb.append("email: ");
                    sb.append(email);
                    sb.append("</p>");

                    sb.append("Date: ");
                    sb.append(formatted);
                    sb.append("<br />");
                }
                sb.append("<p>");
                sb.append("<u8>");
                sb.append("<li>");
                sb.append("sha: ");
                sb.append(sha);
                sb.append("</li>");

                sb.append("<li>");
                sb.append("commit message: ");
                sb.append(message);
                sb.append("</li>");
                sb.append("</u8>");
                sb.append("</p>");

            }
        }
        sb.append("</div>");
        return sb.toString();
    }

    private static List<JSONObject> getEvents(String user) throws IOException {
        List<JSONObject> eventList = new ArrayList<JSONObject>();
        String url = BASE_URL + user + "/events";
        int page = 1;
        int counter = 0;
        boolean valid = true;

        while(valid){
            System.out.println(url);
            JSONObject json = Util.queryAPI(new URL(url+ "?page=" + page));
            System.out.println(json);
            JSONArray events = json.getJSONArray("root");

            int i=0;
            int loopIteration = 0;
            loopIteration = events.length(); // get the number of total events

//            for (int i = 0; i < events.length() && i < 10; i++) {
//                eventList.add(events.getJSONObject(i));
//            }
            while (loopIteration > 0 && counter < 10){
                JSONObject users = events.getJSONObject(i);
                String strType = users.getString("type");

                if (strType.equals("PushEvent")){
                    eventList.add(events.getJSONObject(i));
                    counter++;
                    if (counter >= 10){
                        valid = false;
                        break;
                    }
                }
                loopIteration--;
                i++;
            }
            page++;
        }
        return eventList;
    }
}