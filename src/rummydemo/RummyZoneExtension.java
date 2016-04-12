package rummydemo;

import com.shephertz.app42.server.idomain.BaseZoneAdaptor;
import com.shephertz.app42.server.idomain.HandlingResult;
import com.shephertz.app42.server.idomain.IRoom;
import com.shephertz.app42.server.idomain.IUser;
import com.shephertz.app42.server.idomain.IZone;
import com.shephertz.app42.server.message.WarpResponseResultCode;
import com.shephertz.app42.server.util.Util;

import java.awt.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.smartcardio.CardChannel;

import org.json.JSONObject;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author shephertz
 */
public class RummyZoneExtension extends BaseZoneAdaptor {
    
    private IZone izone;
    
    RummyZoneExtension(IZone izone){
        this.izone = izone;
//        System.out.println(">ZoneCreated");
//        System.out.println("Time: "+today+". USERINFO...: "+ sender.getCustomData());
        ///
    }

    @Override
    public void handleAddUserRequest(final IUser user, final String authData, final HandlingResult result)
    {
    	// Code for checking below update request.
    	
//    	if (authData.isEmpty())
//    	{
//    		System.out.println("OLD USER SEND REQUEST");
//    		result.code = WarpResponseResultCode.AUTH_ERROR;
//    		result.description= "{\"status\":2,\"Message\":\"You are using old verions of app, request to update app.\"}";
//    		return;
//    	}
//    	
//    	Float reqVersion = Float.parseFloat(authData);
//    	System.out.println("Request version "+reqVersion);
//    	
//    	if (reqVersion < CardsConstants.APIVersion)	
//    	{
//    		System.out.println("OLD USER SEND REQUEST");
//    		result.code = WarpResponseResultCode.AUTH_ERROR;
//    		result.description= "{\"status\":2,\"Message\":\"You are using old verions of app, request to update app.\"}";
//    		return;
//    	}
    	
    }
    
    /** remove created rooms if need */
    void checkIfNeedRemoveRoom()
    {
    	Collection<IRoom> roomsDetails = izone.getRooms();
    	
    	for(IRoom room : roomsDetails)
    	{
    		if(room.getJoinedUsers().size() <=0)
    		{
    			Map<String, Object> properties = room.getProperties();
    			if (properties.get(CardsConstants.kRoomOwner)!= null)
    			{
    				room.setAdaptor(null);
    				izone.deleteRoom(room.getId());    				
//    	    		System.out.println("**************************");
//    				System.out.println("Following room will deleted..");
//    				System.out.println("roomname :: "+room.getName());
//    				System.out.println("roomid :: "+room.getId());
    			}
    		}
    	}
    }
    
    String getRandomName()
    {
    	// class variable
    	final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";

    	final java.util.Random rand = new java.util.Random();
    	
    	 StringBuilder builder = new StringBuilder();
    	    while(builder.toString().length() == 0) {
    	        int length = rand.nextInt(5)+5;
    	        for(int i = 0; i < length; i++)
    	            builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
    	    }
    	    return builder.toString();
    	    
    }
    @Override
    public void onUserRemoved(IUser user)
    {
    	
    	final IUser outUser = user;    
    	System.out.println("onUserRemoved type of location :"+outUser.getLocation());    	
    	if (user.getLocation() != null)
    	{
    		if (outUser.getLocation().getAdaptor() instanceof RoomExtension)
    		{
//    			System.out.println("type of location :"+outUser.getLocation());    			
        		//user.getLocation().getAdaptor()
    			RoomExtension ext = (RoomExtension)  outUser.getLocation().getAdaptor();
    			System.out.println("onDisconnectUser calling externally...for "+user.getName());
        		ext.onDisconnectUser(user);
    		}
		}
//    	java.util.Date today = new java.util.Date();
//    	System.out.println("USERINFO...: Time: "+today+"... "+user.getCustomData());
//    	System.out.println("userout>>>"+user.getName());   
    	this.checkIfNeedRemoveRoom();
    }

    @Override
    public void onAdminRoomAdded(IRoom room) 
    {
    	 try
    	 {
    		 RoomExtension extRoom = new RoomExtension(izone,room);
    		 room.setAdaptor(extRoom);
    	 }
    	 catch(Exception e)
    	 {
    		 System.out.println("admin addRoom Error");
    		 System.out.println(e.getStackTrace());
             System.out.println("error: "+e.getMessage());
    	 }
    }
    /*
     * This function invoked when server receive create room request. 
     * we set adapter to room by checking maxUsers in room.
     */
    
    @Override
    public void handleCreateRoomRequest(IUser user, IRoom room, HandlingResult result)
    {
    	if (!room.isTurnBased()) 
    	{
//    		System.out.println("handleCreateRoomRequest");    		
    		RoomExtension extRoom = new RoomExtension(izone,room);
    		room.setAdaptor(extRoom);
//        	System.out.println("result .... "+result.code);
//        	System.out.println("room.... "+room.getName());
		}
    } 

    @Override
    public void onUserPaused(IUser user)
    {
//    	System.out.println("User CustomData on Paused: "+ user.getCustomData());
        if(user.getLocation() == null){
            return;
        }
    }
    
    /*
     * This function invoked when the given user recovers its connection from an intermittent
     * connection failure. (Using Connection Resiliency feature)
     */    
    @Override
    public void handleResumeUserRequest(IUser user, String authData, HandlingResult result)
    {
//    	System.out.println("User CustomData on Resume: "+ user.getCustomData());

    	if (user.getLocation() != null)
    	{
    		if (user.getLocation().getAdaptor() instanceof RoomExtension)
    		{
    			RoomExtension ext = (RoomExtension)  user.getLocation().getAdaptor();
        		ext.userResume(user);
    		}
    	}
    }   
    /**
     * Invoked when a private chat request is received from an online user
     * for another user who is also online. By default the server will send
     * a success response back to the sender and a private chat notification 
     * to the receiver.
     * 
     * @param sender
     * @param toUser
     * @param result 
     */
    public void handlePrivateChatRequest(IUser sender, IUser toUser, HandlingResult result)
    {
//    	System.out.println("chat sender : "+sender.getName());
//    	System.out.println("chat rec  : "+ toUser.getName());
//    	System.out.println("result: "+result);
//    	System.out.println("result.description: "+result.description);
//    	System.out.println("result.code: "+result.code);
    }
    
    HashMap<String, Object> getRoomPropertiesForType(String type)
    {
    	HashMap<String, Object> properties = new HashMap<String, Object>();
    	properties.put(CardsConstants.kRoomType, type);
    	properties.put(CardsConstants.kRoomMaxStack, this.getMaxAmountFor(type));
    	properties.put(CardsConstants.kRoomMinStack, this.getMinAmountFor(type));
    	properties.put(CardsConstants.kRoomDefaultAmount, this.getCallAmountFor(type));
    	return properties;
    }
    
    Integer getMaxAmountFor(String type)
    {
    	return null;
    }
    
    Integer getMinAmountFor(String type)
    {
    	return null;
    }
    Integer getCallAmountFor(String type)
    {
    	return null;
    }    
}