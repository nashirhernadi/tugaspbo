/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pbo_2310010545;

/**
 *
 * @author Anomali
 */
public class Session {
    private static int idUser;
    private static String username;
    private static String level;

    public static void setSession(int id, String user, String lvl){
        idUser = id;
        username = user;
        level = lvl;
    }

    public static int getIdUser() {
        return idUser;
    }

    public static String getUsername() {
        return username;
    }

    public static String getLevel() {
        return level;
    }

    public static void clear(){
        idUser = 0;
        username = null;
        level = null;
    }
}
