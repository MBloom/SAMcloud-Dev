
{{groovy}}
//allBrokenLinks 0.01
//By Caleb James DeLisle calebdelisle{{at}}lavabit_com
//queries database for all links which don't contain dollar signs or spaces (" ").
//removes links containing ' or multiple dots
//queries database for all document fullNames (even in non checked spaces), creates HashSet
//compares each link to HashSet, if no document found, link is reported.
int counter = 0;
ArrayList<String> spaces = xwiki.getSpaces();
String space;
if(request.get("s")!=null){
    long startTime = new Date().getTime();
    ArrayList<String> lookInSpaces = request.getParameterMap().get("s");
    ArrayList<String> links = new ArrayList<String>();
    ArrayList<String> fullNames = new ArrayList<String>();
    String sql = "select lnk.fullName, lnk.link from XWikiLink as lnk where lnk.link not like '%\$%' and lnk.link not like '% %' and "
    String out = "", fullName, link;

    if(lookInSpaces.size() > (spaces.size()/2)){
        while(counter < spaces.size()){
            space = spaces.get(counter);
            if(!lookInSpaces.contains(space)){
                sql += "lnk.fullName not like '"+space+".%' and ";
            }
            counter++;
        }
        sql = sql.substring(0,sql.length()-5);
    }else{
        sql += "(";
        while(counter < lookInSpaces.size()){
            sql += "lnk.fullName like '"+lookInSpaces.get(counter)+".%' or ";
            counter++;
        }
        sql = sql.substring(0,sql.length()-4)+")";
    }

    pairs = xwiki.search(sql,0,0);
    ArrayList<String> pair;

    counter = pairs.size();
    while(counter > 0){
        counter--;
        pair = pairs.get(counter);
        link = pair.get(1);
        if(link.indexOf("'")==-1 && link.indexOf(".")==link.lastIndexOf(".")){
            //out += pairs.get(0)+" "+fullName+"\n";
            links.add(link);
            fullNames.add(pair.get(0));
        }
    }

    HashSet<String> docNames = xwiki.search("select doc.fullName from XWikiDocument as doc",0,0);

    counter = links.size();
    while(counter > 0){
        counter--;
        if(!docNames.contains(links.get(counter))){
            out += "; **"+links.get(counter)+"**\n: [["+fullNames.get(counter)+"]]\n\n";
            if(links.get(counter).substring(0,6) == "xwiki:"){
                //links.get(counter) = links.get(counter).substring(6);
                if(xwiki.exists("jopes:"+links.get(counter).substring(6)))
                    println("* Fixed jopes:"+links.get(counter).substring(6))
            }
        }
    }

    println("allBrokenLinks 0.01\nLinks Examined: "+links.size()+"\nTotal Pages in wiki: "+docNames.size()+"\nTime taken: "+(Math.round((new Date().getTime()-startTime)/100)/10)+" seconds\nThe following broken links were found:\n\n\n; **Broken Link**\n: Page Linked From\n\n"+out);
    
}else{
    println("{{html}}allBrokenLinks 0.01<br/>By Caleb James DeLisle<br/>Look for broken links on all pages in spaces:<form action=\""+this.doc.name+"\"><br/>");
    while(counter < spaces.size()){
        space = spaces.get(counter);
        if(space.equals("XWiki") || space.equals("Stats") || space.equals("Panels")){
            println("<input type=\"checkbox\" name=\"s\" value=\""+space+"\" />"+space+"<br/>");
        }else{
            println("<input type=\"checkbox\" name=\"s\" value=\""+space+"\" checked=checked />"+space+"<br/>");
        }
        counter++;
    }
    println("<input type=submit value=\"Go!\"/></form>{{/html}}");
}
{{/groovy}}
