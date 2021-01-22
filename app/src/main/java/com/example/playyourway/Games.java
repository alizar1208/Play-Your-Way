package com.example.playyourway;

class Games {
    public String date,time,teamname;

    public Games(){


    }

    public  Games(String teamname,String date,String time){
        this.teamname = teamname;
        this.date = date;
        this.time = time;
    }
    public String getTeamname(){return teamname;}

    public void setTeamname(){this.teamname = teamname;}

    public String getDate(){return date;}

    public void setDate(){this.date = date;}

    public String getTime(){return time;}

    public void setTime(){this.time = time;}
}
