package Tests;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Jocopa3
 */
public class Perspective {
    private Vector3f position = null;
    private float yaw = 0.0f;
    private float pitch = 0.0f;
	
	public Perspective(float x, float y, float z){
		position = new Vector3f(x, y, z);
	}
	
	public void yaw(float amount){
		yaw += amount;
	}
 
	public void pitch(float amount){
		pitch += amount;
		if(pitch > 90)
			pitch = 90;
		else if(pitch < -90)
			pitch = -90;
	}
	
	public void moveForward(float distance){
	    position.x -= distance * (float)Math.sin(Math.toRadians(yaw));
	    position.z += distance * (float)Math.cos(Math.toRadians(yaw));
	}
 
	public void moveBackward(float distance){
	    position.x += distance * (float)Math.sin(Math.toRadians(yaw));
	    position.z -= distance * (float)Math.cos(Math.toRadians(yaw));
	}
 
	public void moveLeft(float distance){
	    position.x -= distance * (float)Math.sin(Math.toRadians(yaw-90));
	    position.z += distance * (float)Math.cos(Math.toRadians(yaw-90));
	}
 
	public void moveRight(float distance){
	    position.x -= distance * (float)Math.sin(Math.toRadians(yaw+90));
	    position.z += distance * (float)Math.cos(Math.toRadians(yaw+90));
	}
	
	public void moveUp(float distance){
	    position.y -= distance;
	}
	
	public void moveDown(float distance){
	    position.y += distance;
	}
	
	public void lookThrough(){
        glRotatef(pitch, 1.0f, 0.0f, 0.0f);
        glRotatef(yaw, 0.0f, 1.0f, 0.0f);
        glTranslatef(position.x, position.y, position.z);
    }
}
